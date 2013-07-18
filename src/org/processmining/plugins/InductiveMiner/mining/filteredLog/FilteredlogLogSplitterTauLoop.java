package org.processmining.plugins.InductiveMiner.mining.filteredLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;

/*
 * For the tau loop, first step is to split the log on start -> end transitions
 * Yields only one log, with splitted traces
 */

public class FilteredlogLogSplitterTauLoop extends FilteredlogLogSplitter {

	private Set<XEventClass> startActivities;
	private Set<XEventClass> endActivities;
	private List<XEventClass> partialTrace;
	private Set<XEventClass> sigma;
	private boolean atTransition;

	public FilteredlogLogSplitterTauLoop(List<Set<XEventClass>> sigmas, Set<XEventClass> startActivities, Set<XEventClass> endActivities) {
		super(sigmas);
		sigma = sigmas.get(0);
		
		this.startActivities = startActivities;
		this.endActivities = endActivities;
	}

	/*
	 * split on transitions end -> start
	 */
	/*
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		Iterator<XEventClass> it = trace.iterator();
		atTransition = false;
		partialTrace = new LinkedList<XEventClass>();
		while (it.hasNext()) {
			XEventClass event = it.next();
			
			if (atTransition && startActivities.contains(event)) {
				//we discovered a transition
				finishPartialTrace(cardinality);
				atTransition = false;
			}
			
			if (endActivities.contains(event)) {
				atTransition = true;
			} else {
				atTransition = false;
			}
			
			partialTrace.add(event);
		}
		finishPartialTrace(cardinality);
	}
	*/
	
	/*
	 * split on start
	 */
	public void filterTrace(List<XEventClass> trace, int cardinality) {
		Iterator<XEventClass> it = trace.iterator();
		boolean first = true;
		partialTrace = new LinkedList<XEventClass>();
		while (it.hasNext()) {
			XEventClass event = it.next();
			
			if (!first && startActivities.contains(event)) {
				//we discovered a transition
				finishPartialTrace(cardinality);
				first = true;
			}
			
			partialTrace.add(event);
			first = false;
		}
		finishPartialTrace(cardinality);
	} 

	private void finishPartialTrace(int cardinality) {
		//add the current partial trace to its sublog
		MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
		sublog.add(partialTrace, cardinality);
		int size = mapSigma2eventSize.get(sigma) + cardinality * partialTrace.size();
		mapSigma2eventSize.put(sigma, size);
		partialTrace = new LinkedList<XEventClass>();
	}

}
