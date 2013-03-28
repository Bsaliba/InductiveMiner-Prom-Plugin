package bPrime.mining;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

import bPrime.MultiSet;

public class FilteredlogLogSplitterSequence extends FilteredlogLogSplitter {
	
	public FilteredlogLogSplitterSequence(List<Set<XEventClass>> sigmas) {
		super(sigmas);
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
					noiseEvents.add(event, cardinality);
				}
			}

			MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
			sublog.add(newTrace, cardinality);
			mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + newTrace.size() * cardinality);
			
			lastPosition = atPosition;
			i++;
		}
		
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
}
