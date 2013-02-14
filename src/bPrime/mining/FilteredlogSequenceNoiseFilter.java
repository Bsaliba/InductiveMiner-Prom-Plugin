package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public class FilteredlogSequenceNoiseFilter {
	
	private class Bucket {
		public Set<XEventClass> sigma;
		private int length;
		private List<XEventClass> subtrace;
		
		public Bucket(XEventClass event, Set<XEventClass> sigma) {
			subtrace = new LinkedList<XEventClass>();
			subtrace.add(event);
			this.sigma = sigma;
			length = 1;
		}
		
		public boolean add(XEventClass event) {
			if (sigma.contains(event)) {
				subtrace.add(event);
				length++;
				return true;
			}
			return false;
		}
		
		public List<XEventClass> getSubtrace() {
			return subtrace;
		}
	}
	
	private List<Set<XEventClass>> sigmas;
	private HashMap<XEventClass, Set<XEventClass>> mapEvent2sigma;
	private HashMap<Set<XEventClass>, Integer> mapSigma2order;
	private MultiSet<XEventClass> noise;
	private List<MultiSet<List<XEventClass>>> sublogs;
	private HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog;
	private HashMap<Set<XEventClass>, Integer> mapSigma2eventSize;
	
	private List<Bucket> optimalBuckets;
	private int minCost;
	
	public FilteredlogSequenceNoiseFilter(List<Set<XEventClass>> sigmas) {
		this.sigmas = sigmas;
		noise = new MultiSet<XEventClass>();
		sublogs = new ArrayList<MultiSet<List<XEventClass>>>();
		
		//initialise the sublogs, make a hashmap of activities
		mapEvent2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		mapSigma2order = new HashMap<Set<XEventClass>, Integer>();
		mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		int i = 0;
		for (Set<XEventClass> sigma : sigmas) {
			for (XEventClass activity : sigma) {
				mapEvent2sigma.put(activity, sigma);
			}
			mapSigma2eventSize.put(sigma, 0);
			mapSigma2order.put(sigma, i);
			MultiSet<List<XEventClass>> sublog = new MultiSet<List<XEventClass>>();
			sublogs.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			i++;
		}
	}
	
	/*
	 * Add a trace to be filtered and to be added to its sublog
	 */
	
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		minCost = trace.size();
		
		//divide the trace into buckets
		List<Bucket> buckets = new ArrayList<Bucket>();
		for (XEventClass event : trace) {
			if (buckets.size() == 0) {
				buckets.add(new Bucket(event, mapEvent2sigma.get(event)));
			} else {
				Bucket bucket = buckets.get(buckets.size()-1);
				if (!bucket.add(event)) {
					buckets.add(new Bucket(event, mapEvent2sigma.get(event)));
				} else {
					buckets.set(buckets.size()-1, bucket);
				}
			}
		}
		
		//find the optimal combination of buckets
		filterBuckets(buckets, 0);
		
		//put the subtraces of the buckets into their respective sublogs
		List<List<XEventClass>> result = new LinkedList<List<XEventClass>>();
		Iterator<Bucket> it = optimalBuckets.iterator();
		Bucket currentBucket = null;
		if (it.hasNext()) {
			currentBucket = it.next();
		}
		for (Set<XEventClass> sigma : sigmas) {
			List<XEventClass> sigmaSubTrace = new LinkedList<XEventClass>();
			while (currentBucket != null && currentBucket.sigma == sigma) {
				sigmaSubTrace.addAll(currentBucket.subtrace);
				if (it.hasNext()) {
					currentBucket = it.next();
				} else {
					currentBucket = null;
				}
			}
			mapSigma2sublog.get(sigma).add(sigmaSubTrace, cardinality);
			mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + (sigmaSubTrace.size() * cardinality));
			result.add(sigmaSubTrace);
		}
		
		//for statistical purposes, collect noisy traces
		for (Bucket bucket : buckets) {
			if (!optimalBuckets.contains(bucket)) {
				//this bucket was removed, add it to the noise multiset
				noise.addAll(bucket.getSubtrace());
			}
		}
	}
	
	public List<Filteredlog> getSublogs() {
		//make a copy of the arguments and the new filtered sublogs
		List<Filteredlog> result = new ArrayList<Filteredlog>();
		Iterator<MultiSet<List<XEventClass>>> it = sublogs.iterator();
		for (Set<XEventClass> sigma : sigmas) {
			result.add(new Filteredlog(it.next(), new HashSet<XEventClass>(sigma), mapSigma2eventSize.get(sigma)));
		}
		return result;
	}
	
	public MultiSet<XEventClass> getNoise() {
		return noise;
	}
	/*
	private int findOptimalSplit(List<XEventClass> trace, ) {
		
	}
	*/
	
	/*
	 * Apply a brute-force approach to find the least-costing bucket list that represents a valid trace.
	 * Use pruning to shorten search times, but we suspect it is still NP-hard.
	 * 
	 */
	private void filterBuckets(List<Bucket> buckets, int cost) {
		//check whether this list of buckets is a valid sequence
		if (checkValidity(buckets)) {
			
			//the bucketlist we found is valid. remember it
			if (cost < minCost) {
				minCost = cost;
				optimalBuckets = buckets;
				debug("minCost " + minCost);
			}
			return;
		}
		
		for (Bucket bucket : buckets) {
			//drop this bucket from the list and recurse if it would save us anything
			if (cost + bucket.length < minCost) {
				List<Bucket> newBuckets = new ArrayList<Bucket>(buckets);
				newBuckets.remove(bucket);
				filterBuckets(newBuckets, cost + bucket.length);
			}
		}
	}
	
	private boolean checkValidity(List<Bucket> buckets) {
		Iterator<Bucket> it = buckets.iterator();
		if (!it.hasNext()) {
			return true;
		}
		Bucket lastBucket = it.next();
		Bucket newBucket;
		while (it.hasNext()) {
			newBucket = it.next();
			
			if (mapSigma2order.get(lastBucket.sigma) > mapSigma2order.get(newBucket.sigma)) {
				return false;
			}
			
			lastBucket = newBucket;
		}
		return true;
	}
	
	private void debug(String x) {
		System.out.println(x);
	}
}
