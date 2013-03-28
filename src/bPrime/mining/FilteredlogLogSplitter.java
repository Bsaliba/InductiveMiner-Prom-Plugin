package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public abstract class FilteredlogLogSplitter {
	
	protected List<Set<XEventClass>> sigmas;
	
	protected HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog;
	protected HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma;
	protected HashMap<Set<XEventClass>, Integer> mapSigma2eventSize;
	
	protected MultiSet<XEventClass> noiseEvents;
	
	public FilteredlogLogSplitter(List<Set<XEventClass>> sigmas) {
		this.sigmas = sigmas;
		mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		mapActivity2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2eventSize.put(sigma, 0);
			mapSigma2sublog.put(sigma, new MultiSet<List<XEventClass>>());
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}
		noiseEvents = new MultiSet<XEventClass>();
	}
	
	/*
	 * add a trace to the filtered logs
	 */
	public abstract void filterTrace(List<XEventClass> trace, int cardinality);
	
	/*
	 * Return the result of the filtering as a list of Filteredlogs.
	 */
	public List<Filteredlog> getSublogs() {
		//make a copy of the arguments and return the new filtered sublogs
		List<Filteredlog> result = new ArrayList<Filteredlog>();
		for (Set<XEventClass> sigma : sigmas) {
			result.add(new Filteredlog(mapSigma2sublog.get(sigma), new HashSet<XEventClass>(sigma), mapSigma2eventSize.get(sigma)));
		}
		return result;
	}
	
	/*
	 * Return a multiset of the events that were classified as noise up till now
	 */
	public MultiSet<XEventClass> getNoiseEvents() {
		return noiseEvents;
	}
	
	
}
