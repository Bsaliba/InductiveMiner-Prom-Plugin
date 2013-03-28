package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class FilteredlogLogSplitterParallel extends FilteredlogLogSplitter {

	public FilteredlogLogSplitterParallel(Set<Set<XEventClass>> sigmas) {
		super(new ArrayList<Set<XEventClass>>(sigmas));
	}

	public void filterTrace(List<XEventClass> trace, int cardinality) {
		
		HashMap<Set<XEventClass>, List<XEventClass>> mapSigma2subtrace = new HashMap<Set<XEventClass>, List<XEventClass>>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2subtrace.put(sigma, new LinkedList<XEventClass>());
		}
		
		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			 mapSigma2subtrace.get(sigma).add(event);
			 mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + cardinality);
		}
		
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma), cardinality);
		}
	}

}
