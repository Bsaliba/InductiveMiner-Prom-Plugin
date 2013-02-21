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
	
	private List<Set<XEventClass>> sigmas;
	private MultiSet<XEventClass> noise;
	private List<MultiSet<List<XEventClass>>> sublogs;
	private HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog;
	private HashMap<Set<XEventClass>, Integer> mapSigma2eventSize;
	
	public FilteredlogSequenceNoiseFilter(List<Set<XEventClass>> sigmas) {
		this.sigmas = sigmas;
		noise = new MultiSet<XEventClass>();
		sublogs = new ArrayList<MultiSet<List<XEventClass>>>();
		
		//initialise the sublogs, make a hashmap of activities
		mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2eventSize.put(sigma, 0);
			MultiSet<List<XEventClass>> sublog = new MultiSet<List<XEventClass>>();
			sublogs.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
		}
	}
	
	/*
	 * Add a trace to be filtered and to be added to its sublogs
	 */
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		int atPosition = 0;
		int lastPosition = 0;
		List<XEventClass> newTrace;
		Set<XEventClass> ignore = new HashSet<XEventClass>(); 
		
		int i = 0;
		for (Set<XEventClass> sigma : sigmas) {
			
			if (i < sigmas.size() - 1) {
				atPosition = findOptimalSplit(trace, sigma, atPosition, ignore);
			} else {
				//if this is the last sigma, there's no point in splitting
				atPosition = trace.size();
			}
			
			ignore.addAll(sigma);
			
			//split the trace
			newTrace = new LinkedList<XEventClass>();
			for (XEventClass event : trace.subList(lastPosition, atPosition)) {
				if (sigma.contains(event)) {
					newTrace.add(event);
				} else {
					noise.add(event, cardinality);
				}
			}

			MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
			sublog.add(newTrace, cardinality);
			mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + newTrace.size() * cardinality);
			
			lastPosition = atPosition;
			i++;
		}
		
	}
	
	
	/*
	 * Return the result of the filtering as a list of Filteredlogs.
	 */
	public List<Filteredlog> getSublogs() {
		//make a copy of the arguments and return the new filtered sublogs
		List<Filteredlog> result = new ArrayList<Filteredlog>();
		Iterator<MultiSet<List<XEventClass>>> it = sublogs.iterator();
		for (Set<XEventClass> sigma : sigmas) {
			result.add(new Filteredlog(it.next(), new HashSet<XEventClass>(sigma), mapSigma2eventSize.get(sigma)));
		}
		return result;
	}
	
	/*
	 * Return a multiset of the events that were classified as noise up till now
	 */
	public MultiSet<XEventClass> getNoise() {
		return noise;
	}
	
	private static int findOptimalSplit(List<XEventClass> trace, 
			Set<XEventClass> sigma, 
			int startPosition,
			Set<XEventClass> ignore) {
		int positionLeastCost = 0;
		int leastCost = 0;
		int cost = 0;
		int position = 0;
		
		Iterator<XEventClass> it = trace.iterator();
		
		//debug("find optimal split in " + trace.toString() + " for " + sigma.toString());
		
		//move to the start position
		while (position < startPosition && it.hasNext()) {
			position = position + 1;
			positionLeastCost = positionLeastCost + 1;
			it.next();
		}
		
		XEventClass event;
		while (it.hasNext()) {
			event = it.next();
			if (ignore.contains(event)) {
				//skip
			} else if (sigma.contains(event)) {
				cost = cost - 1;
			} else {
				cost = cost + 1;
			}
			
			position = position + 1;
			
			if (cost < leastCost) {
				leastCost = cost;
				positionLeastCost = position;
			}
		}
		
		//debug(String.valueOf(positionLeastCost));
		return positionLeastCost;
	}
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
