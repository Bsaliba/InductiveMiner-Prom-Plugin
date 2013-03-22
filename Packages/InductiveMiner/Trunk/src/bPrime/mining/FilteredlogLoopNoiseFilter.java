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

public class FilteredlogLoopNoiseFilter {
	
	private List<Set<XEventClass>> sigmas;
	private MultiSet<XEventClass> noise;
	private List<MultiSet<List<XEventClass>>> sublogs;
	private HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog;
	private HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma;
	private HashMap<Set<XEventClass>, Integer> mapSigma2eventSize;
	
	private Set<XEventClass> lastSigma;
	private List<XEventClass> partialTrace;
	
	public FilteredlogLoopNoiseFilter(List<Set<XEventClass>> sigmas) {
		this.sigmas = sigmas;
		noise = new MultiSet<XEventClass>();
		sublogs = new ArrayList<MultiSet<List<XEventClass>>>();
		
		//initialise the sublogs, make a hashmap of activities
		mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		mapActivity2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2eventSize.put(sigma, 0);
			MultiSet<List<XEventClass>> sublog = new MultiSet<List<XEventClass>>();
			sublogs.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}
	}
	
	/*
	 * Add a trace to be filtered and to be added to its sublogs
	 */
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		partialTrace = new LinkedList<XEventClass>();
		
		lastSigma = sigmas.get(0);
		Iterator<XEventClass> it = trace.iterator();
		while (it.hasNext()) {
			XEventClass event = it.next();
			if (!lastSigma.contains(event)) {
				finishPartialTrace(cardinality);
				lastSigma = mapActivity2sigma.get(event);
			}
			partialTrace.add(event);
		}
		finishPartialTrace(cardinality);
		
		//add an empty trace if the last event was not of the body part
		if (lastSigma != sigmas.get(0)) {
			lastSigma = sigmas.get(0);
			finishPartialTrace(cardinality);
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
	
	private void finishPartialTrace(int cardinality) {
		//add the current partial trace to its sublog
		MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(lastSigma);
		sublog.add(partialTrace, cardinality);
		int size = mapSigma2eventSize.get(lastSigma) + cardinality * partialTrace.size();
		mapSigma2eventSize.put(lastSigma, size);
		partialTrace = new LinkedList<XEventClass>();
	}
}
