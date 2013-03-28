package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public class FilteredlogLogSplitterExclusiveChoice extends FilteredlogLogSplitter {
	
	public FilteredlogLogSplitterExclusiveChoice(Set<Set<XEventClass>> sigmas) {
		super(new ArrayList<Set<XEventClass>>(sigmas));
	}
	
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		//walk through the events and count how many go in each sigma
		HashMap<Set<XEventClass>, Integer> eventCounter = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			eventCounter.put(sigma, 0);
		}
		int maxCounter = 0;
		Set<XEventClass> maxSigma = null;
		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			int newCounter = eventCounter.get(sigma) + 1;
			if (newCounter > maxCounter) {
				maxCounter = newCounter;
				maxSigma = sigma;
			}
			eventCounter.put(sigma, newCounter);
		}
				
		//make a copy of the trace, leaving out the noise
		List<XEventClass> newTrace = new ArrayList<XEventClass>();
		for (XEventClass event : trace) {
			if (maxSigma.contains(event)) {
				newTrace.add(event);
			} else {
				//debug
				noiseEvents.add(event, cardinality);							
			}
		}
				
		MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(maxSigma);
		sublog.add(newTrace, cardinality);
		mapSigma2sublog.put(maxSigma, sublog);
		mapSigma2eventSize.put(maxSigma, mapSigma2eventSize.get(maxSigma) + (trace.size() * cardinality));
	}
}
