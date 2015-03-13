package org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2.IMEventIterator;

public class IMLog2IMLogInfoLifeCycle implements IMLog2IMLogInfo {

	private class Count {
		long numberOfEvents = 0;
		long numberOfEpsilonTraces = 0;
		MultiSet<XEventClass> activities = new MultiSet<XEventClass>();
	}
	
	public IMLogInfo createLogInfo(IMLog2 log) {
		Count count = new Count();
		Dfg dfg = log2Dfg(log, count);
		
		TObjectIntHashMap<XEventClass> minimumSelfDistances = new TObjectIntHashMap<>();
		THashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = new THashMap<XEventClass, MultiSet<XEventClass>>();
		
		return new IMLogInfo(dfg, count.activities, minimumSelfDistancesBetween, minimumSelfDistances,
				count.numberOfEvents, count.numberOfEpsilonTraces);
	}
	
	public static Dfg log2Dfg(IMLog2 log, Count count) {
		Dfg dfg = new Dfg();
		for (IMTrace2 trace : log) {
			processTrace(log, dfg, trace, count);
			
			if (trace.isEmpty()) {
				count.numberOfEpsilonTraces++;
			} else {
				count.numberOfEvents += trace.size();
			}
		}
		return dfg;
	}
	
	/**
	 * Get a directly-follows graph from a log.
	 * 
	 * @param log
	 * @return
	 */
	public static Dfg log2Dfg(IMLog2 log) {
		Dfg dfg = new Dfg();
		for (IMTrace2 trace : log) {
			processTrace(log, dfg, trace, null);
		}
		return dfg;
	}

	private static void processTrace(IMLog2 log, Dfg dfg, IMTrace2 trace, Count count) {
		if (trace.isEmpty()) {
			return;
		}

		//directly-follows relation
		processDirectlyFollows(log, dfg, trace, count);

		//parallelism
		processParallel(log, dfg, trace);

		//start/end activities
		processStartEnd(log, dfg, trace);
	}

	private static void processStartEnd(IMLog2 log, Dfg dfg, IMTrace2 trace) {
		boolean activityOccurrenceCompleted = false;
		MultiSet<XEventClass> activityOccurrencesEndedSinceLastStart = new MultiSet<>();
		MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
		for (XEvent event : trace) {
			XEventClass activity = log.classify(event);

			if (log.isStart(event)) {
				openActivityOccurrences.add(activity);
				if (!activityOccurrenceCompleted) {
					//no activity occurrence has been completed yet. Add to start events.
					dfg.addStartActivity(activity, 1);
				}
				activityOccurrencesEndedSinceLastStart = new MultiSet<>();
			} else if (log.isComplete(event)) {
				//complete event
				if (openActivityOccurrences.contains(activity)) {
					//this activity occurrence was open; close it
					openActivityOccurrences.add(activity, -1);
					activityOccurrencesEndedSinceLastStart.add(activity);
				} else {
					//next front is non-started but complete

					if (!activityOccurrenceCompleted) {
						//no activity occurrence has been completed yet. Add to start events.
						dfg.addStartActivity(activity, 1);
					}
					activityOccurrenceCompleted = true;

					activityOccurrencesEndedSinceLastStart = new MultiSet<>();
					activityOccurrencesEndedSinceLastStart.add(activity);
				}
			}

			activityOccurrenceCompleted = activityOccurrenceCompleted || log.isComplete(event);
		}
		dfg.getEndActivities().addAll(activityOccurrencesEndedSinceLastStart);
	}

	private static void processParallel(IMLog2 log, Dfg dfg, IMTrace2 trace) {
		MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
		for (XEvent event : trace) {
			XEventClass eventClass = log.classify(event);

			if (log.isStart(event)) {
				//this is a start event
				openActivityOccurrences.add(eventClass);
			} else {
				//this is an end event
				openActivityOccurrences.remove(eventClass, 1);

				//this activity occurrence is parallel to all open activity occurrences
				for (XEventClass eventClass2 : openActivityOccurrences) {
					dfg.addParallelEdge(eventClass, eventClass2, openActivityOccurrences.getCardinalityOf(eventClass2));
				}
			}
		}
	}

	private static void processDirectlyFollows(IMLog2 log, Dfg dfg, IMTrace2 trace, Count count) {
		IMEventIterator itCurrent = trace.iterator();
		MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

		boolean isStart[] = new boolean[trace.size()];
		
		int i = 0;
		while (itCurrent.hasNext()) {
			XEvent event = itCurrent.next();
			XEventClass activity = log.classify(event);

			//this is a start event if the log says so, or if we see a complete without corresponding preceding start event. 
			boolean isStartEvent = log.isStart(event) || !openActivityInstances.contains(activity);
			boolean isCompleteEvent = log.isComplete(event);
			isStart[i] = isStartEvent;

			if (isStartEvent) {
				//this is a start event, which means that it could have predecessors
				walkBack(itCurrent, isStart, log, i, dfg, activity);
			}
			if (isCompleteEvent && count != null) {
				//this is a complete event, add it to the activities
				count.activities.add(activity);
			}

			//update the open activity instances
			if (isCompleteEvent && !isStartEvent) {
				//if this ends an activity instance (and it was open already), remove it 
				openActivityInstances.remove(activity);
			}
			if (isStartEvent && !isCompleteEvent) {
				//if this starts an activity instance (and does not immediately close it), it is left open for now
				openActivityInstances.add(activity);
			}
			i++;
		}
	}
	
	private static void walkBack(IMEventIterator it, boolean[] isStart, IMLog2 log, int i, Dfg dfg, XEventClass target) {
		it = it.clone();
		MultiSet<XEventClass> completes = new MultiSet<>();
		while (it.hasPrevious()) {
			i--;
			XEvent event = it.previous();
			XEventClass activity = log.classify(event);
			
			if (log.isComplete(event)) {
				completes.add(activity);
				dfg.addDirectlyFollowsEdge(activity, target, 1);
			}
			if (isStart[i] && completes.contains(activity)) {
				return;
			}
		}
	}

}
