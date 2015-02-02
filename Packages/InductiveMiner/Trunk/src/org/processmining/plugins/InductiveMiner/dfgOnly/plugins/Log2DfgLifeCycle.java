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
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class Log2DfgLifeCycle {
	@Plugin(name = "Convert log to directly-follows graph using lifecycle", returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel(
				"Directly-follows graph of " + XConceptExtension.instance().extractName(log));
		return log2Dfg(new IMLog(log, MiningParameters.getDefaultClassifier()));
	}

	public static Dfg log2Dfg(IMLog log) {

		//read log
		Dfg dfg = new Dfg();
		for (IMTrace trace : log) {

			//directly-follows relation
			if (!trace.isEmpty()) {
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
			
			//parallelism
			{
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

			//start/end activities
			{
				boolean activityOccurrenceCompleted = false;
				MultiSet<XEventClass> activityOccurrencesEndedSinceLastStart = new MultiSet<>();
				MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
				System.out.println("trace");
				for (XEvent event : trace) {
					XEventClass activity = log.classify(event);

					if (log.isStart(event)) {
						System.out.println(" start " + activity);
						openActivityOccurrences.add(activity);
						if (!activityOccurrenceCompleted) {
							//no activity occurrence has been completed yet. Add to start events.
							dfg.addStartActivity(activity, 1);
						}
						activityOccurrencesEndedSinceLastStart = new MultiSet<>();
					} else if (log.isComplete(event)) {
						System.out.println(" complete " + activity);
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
		}

		return dfg;
	}

	private static boolean containsActivity(IMLog log, XEventClass eventClass, IMTrace trace, int back, int front) {
		for (XEvent e : trace.subList(back, front + 1)) {
			if (log.classify(e).equals(eventClass)) {
				return true;
			}
		}
		return false;
		//		return trace.subList(back, front).contains(getActivity(event, infoActivity));
	}
}
