package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2.IMEventIterator;

public class Log2DfgLifeCycle {
	@Plugin(name = "Convert log to directly-follows graph using lifecycle", returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel(
				"Directly-follows graph of " + XConceptExtension.instance().extractName(log));
		return log2Dfg(new IMLog2(log, MiningParameters.getDefaultClassifier()));
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
			processTrace(log, dfg, trace);
		}
		return dfg;
	}

	private static void processTrace(IMLog2 log, Dfg dfg, IMTrace2 trace) {
		if (trace.isEmpty()) {
			return;
		}

		//directly-follows relation
		processTraceDirectlyFollows(log, dfg, trace);
		
		processDirectlyFollows(log, dfg, trace);

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

	private static void processDirectlyFollows(IMLog2 log, Dfg dfg, IMTrace2 trace) {
		IMEventIterator itCurrent = trace.iterator();
		IMEventIterator itLastStart = null;
		IMEventIterator itLastComplete = null;
		MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

		while (itCurrent.hasNext()) {
			XEvent event = itCurrent.next();
			XEventClass activity = log.classify(event);
			System.out.println("read " + activity);

			//this is a start event if the log says so, or if we see a complete without corresponding preceding start event. 
			boolean isStartEvent = log.isStart(event) || !openActivityInstances.contains(activity);
			boolean isCompleteEvent = log.isComplete(event);
			System.out.println(" start " + isStartEvent + ", complete " + isCompleteEvent);
			
			if (isStartEvent && itLastStart != null) {
				//the predecessors of this node are all from lastStart till here
				for (XEvent predecessor : itLastStart.getUntil(itCurrent)) {
//					if (log.isComplete(predecessor)) {
						XEventClass predecessorActivity = log.classify(predecessor);
						System.out.println(" predecessor " + predecessorActivity);
//					}
				}
			}
			
			if (isCompleteEvent) {
				itLastComplete = itCurrent.clone();
			}
			if (isStartEvent) {
				itLastStart = itCurrent.clone();
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
		}
	}

	private static void processTraceDirectlyFollows(IMLog2 log, Dfg dfg, IMTrace2 trace) {
		int front = 0;
		int back = 0;

		MultiSet<XEventClass> openActivityOccurrencesAtFront = new MultiSet<XEventClass>();
		XEvent eventFront = trace.get(front);
		if (log.isStart(eventFront)) {
			openActivityOccurrencesAtFront.add(log.classify(eventFront));
		}

		while (front < trace.size() - 1) {
			XEvent nextFrontEvent = trace.get(front + 1);
			XEventClass nextFrontClass = log.classify(nextFrontEvent);

			//see whether this event was opened before
			if (log.isStart(nextFrontEvent)) {
				//we are starting an activity occurrence, add it to the list of open activity occurrences. 
				openActivityOccurrencesAtFront.add(nextFrontClass);
			} else {
				//complete event
				if (openActivityOccurrencesAtFront.contains(nextFrontClass)) {
					//this activity occurrence was open; close it
					openActivityOccurrencesAtFront.remove(nextFrontClass, 1);
				} else {
					//next front is non-started but complete

					if (log.isStart(nextFrontEvent)) {
						//we are adding a start event; add all complete activities from [back:front] to the directly-follows graph.
						XEventClass to = nextFrontClass;
						for (XEvent from : trace.subList(back, front + 1)) {
							if (log.isComplete(from)) {
								XEventClass fromClass = log.classify(from);
								dfg.addDirectlyFollowsEdge(fromClass, to, 1);
							}
						}
					}

					//progress back and front
					front++;
					back = front;
					continue;
				}
			}

			if (log.isComplete(nextFrontEvent) && containsActivity(log, nextFrontClass, trace, back, front)) {
				//we cannot progress front, so progress back
				back++;
			} else {
				//progress front

				if (log.isStart(nextFrontEvent)) {
					//we are adding a start event; add all complete activities from [back:front] to the directly-follows graph.
					XEventClass to = log.classify(nextFrontEvent);
					for (XEvent from : trace.subList(back, front + 1)) {
						if (log.isComplete(from)) {
							XEventClass fromClass = log.classify(from);
							dfg.addDirectlyFollowsEdge(fromClass, to, 1);
						}
					}
				}

				//progress front
				front++;
			}
		}
	}

	private static boolean containsActivity(IMLog2 log, XEventClass eventClass, IMTrace2 trace, int back, int front) {
		for (XEvent e : trace.subList(back, front + 1)) {
			if (log.classify(e).equals(eventClass)) {
				return true;
			}
		}
		return false;
		//		return trace.subList(back, front).contains(getActivity(event, infoActivity));
	}
}
