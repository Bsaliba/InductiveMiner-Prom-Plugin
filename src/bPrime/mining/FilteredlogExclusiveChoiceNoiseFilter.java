package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public class FilteredlogExclusiveChoiceNoiseFilter {
	private Set<Set<XEventClass>> sigmas;
	private MultiSet<XEventClass> noise;
	private List<MultiSet<List<XEventClass>>> sublogs;
	private HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog;
	private HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma;
	private HashMap<Set<XEventClass>, Integer> mapSigma2eventSize;
	
	public FilteredlogExclusiveChoiceNoiseFilter(Set<Set<XEventClass>> sigmas) {
		this.sigmas = sigmas;
		noise = new MultiSet<XEventClass>();
		sublogs = new ArrayList<MultiSet<List<XEventClass>>>();
		
		mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		mapActivity2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2sublog.put(sigma, new MultiSet<List<XEventClass>>());
			mapSigma2eventSize.put(sigma, 0);
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}
	}
	
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		//walk through the events and count how many go in each sigma
		HashMap<Set<XEventClass>, Integer> eventCounter = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			eventCounter.put(sigma, 0);
		}
		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			eventCounter.put(sigma, eventCounter.get(sigma) + 1);
		}
		
		//put the trace in the sublog of the sigma that accounts for more than half of the events
		for (Set<XEventClass> sigma : sigmas) {
			if (eventCounter.get(sigma) * 2 > trace.size()) {
				//make a copy of the trace, leaving out the noise
				List<XEventClass> newTrace = new ArrayList<XEventClass>();
				for (XEventClass event : trace) {
					if (sigma.contains(event)) {
						newTrace.add(event);
					} else {
						//debug
						noise.add(event, cardinality);							
					}
				}
				
				MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
				sublog.add(newTrace, cardinality);
				mapSigma2sublog.put(sigma, sublog);
				mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + (trace.size() * cardinality));
			}
		}
	}
	
	/*
	 * Return the result of the filtering as a list of Filteredlogs.
	 */
	public Set<Filteredlog> getSublogs() {
		//make a copy of the arguments and return the new filtered sublogs
		Set<Filteredlog> result = new HashSet<Filteredlog>();
		for (Set<XEventClass> sigma : sigmas) {
			HashSet<XEventClass> activities = new HashSet<XEventClass>(sigma);
			MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
			int size = mapSigma2eventSize.get(sigma);
			result.add(new Filteredlog(sublog, activities, size));
		}
		return result;
	}
	
	/*
	 * Return a multiset of the events that were classified as noise up till now
	 */
	public MultiSet<XEventClass> getNoise() {
		return noise;
	}
	
	private void debug(String x) {
		System.out.println(x);
	}
}
