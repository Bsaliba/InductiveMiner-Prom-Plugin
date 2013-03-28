package bPrime.mining;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public class FilteredlogLogSplitterLoop extends FilteredlogLogSplitter {
	
	public FilteredlogLogSplitterLoop(List<Set<XEventClass>> sigmas) {
		super(sigmas);
	}
	
	private Set<XEventClass> lastSigma;
	private List<XEventClass> partialTrace;
	
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
		
		//add an empty trace if the last event was not of sigma_1
		if (lastSigma != sigmas.get(0)) {
			lastSigma = sigmas.get(0);
			finishPartialTrace(cardinality);
		}
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
