package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class XLog2DfgLifeCycle {
	@Plugin(name = "Convert log to lifecycle directly-follows graph", returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel(
				"Directly-follows graph of " + XConceptExtension.instance().extractName(log));
		return log2Dfg(log);
	}

	public static Dfg log2Dfg(XLog log) {
		XEventClassifier classifierActivity = MiningParameters.getDefaultClassifier();
		XEventClassifier classifierLifeCycle = new XEventLifeTransClassifier();

		XLogInfo infoActivity = XLogInfoFactory.createLogInfo(log, classifierActivity);
		XLogInfo infoLifeCycle = XLogInfoFactory.createLogInfo(log, classifierLifeCycle);

		//read log
		Dfg dfg = new Dfg();
		for (XTrace trace : log) {

			System.out.println(trace);
			
			//directly-follows relation
			if (!trace.isEmpty()) {
				int front = 0;
				int back = 0;
				
				MultiSet<XEventClass> openActivityOccurrencesAtFront = new MultiSet<XEventClass>();
				if (isStart(trace.get(front), infoLifeCycle)) {
					openActivityOccurrencesAtFront.add(getActivity(trace.get(front), infoActivity));
				}

				while (front < trace.size() - 1) {
					System.out.println("front " + front + ", back " + back + ", open " + openActivityOccurrencesAtFront);
					
					XEvent nextFrontEvent = trace.get(front + 1);
					
					//see whether this event was opened before
					if (isStart(nextFrontEvent, infoLifeCycle)) {
						openActivityOccurrencesAtFront.add(getActivity(nextFrontEvent, infoActivity));
					} else {
						//complete event
						if (openActivityOccurrencesAtFront.contains(nextFrontEvent)) {
							//this activity occurrence was open; close it
							openActivityOccurrencesAtFront.add(getActivity(nextFrontEvent, infoActivity), -1);
						} else {
							//next front is non-started but complete

							//we are adding a start event; add all complete activities from [back:front] to the directly-follows graph.
							XEventClass to = infoActivity.getEventClasses().getClassOf(nextFrontEvent);
							for (int i = back; i <= front; i++) {
								XEventClass from = infoActivity.getEventClasses().getClassOf(trace.get(i));
								dfg.addDirectlyFollowsEdge(from, to, 1);
							}
							
							//progress back and front
							front++;
							back = front;
							continue;
						}
					}

					if (isComplete(nextFrontEvent, infoLifeCycle)
							&& containsActivity(nextFrontEvent, infoActivity, trace, back, front)) {
						//we cannot progress front, so progress back
						back++;
					} else {
						if (!isComplete(nextFrontEvent, infoLifeCycle)) {
							//we are adding a start event; add all complete activities from [back:front] to the directly-follows graph.
							XEventClass to = infoActivity.getEventClasses().getClassOf(nextFrontEvent);
							for (int i = back; i <= front; i++) {
								XEventClass from = infoActivity.getEventClasses().getClassOf(trace.get(i));
								dfg.addDirectlyFollowsEdge(from, to, 1);
							}
						}

						//progress front
						front++;
					}
				}
			}

			//start/end activities
			{
				boolean activityOccurrenceCompleted = false;
				MultiSet<XEventClass> activityOccurrencesEndedSinceLastStart = new MultiSet<>();
				MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
				for (XEvent event : trace) {
					XEventClass activity = infoActivity.getEventClasses().getClassOf(event);
					
					if (isStart(event, infoLifeCycle)) {
						openActivityOccurrences.add(getActivity(event, infoActivity));
						if (!activityOccurrenceCompleted) {
							//no activity occurrence has been completed yet. Add to start events.
							dfg.addStartActivity(activity, 1);
						}
						activityOccurrencesEndedSinceLastStart = new MultiSet<>();
					} else {
						//complete event
						if (openActivityOccurrences.contains(event)) {
							//this activity occurrence was open; close it
							openActivityOccurrences.add(getActivity(event, infoActivity), -1);
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
					
					activityOccurrenceCompleted = activityOccurrenceCompleted || isComplete(event, infoLifeCycle);
				}
				dfg.getEndActivities().addAll(activityOccurrencesEndedSinceLastStart);
			}
		}

		return dfg;
	}

	
	public static XEventClass getActivity(XEvent event, XLogInfo infoActivity)  {
		return infoActivity.getEventClasses().getClassOf(event);
	}
	
	public static boolean containsActivity(XEvent event, XLogInfo infoActivity, XTrace trace, int back, int front) {
		return trace.subList(back, front).contains(getActivity(event, infoActivity));
	}

	public static boolean isStart(XEvent event, XLogInfo infoLifeCycle) {
		XEventClass nextFrontLifeCycle = infoLifeCycle.getEventClasses().getClassOf(event);
		return nextFrontLifeCycle.getId().equals("start");
	}
	
	public static boolean isComplete(XEvent event, XLogInfo infoLifeCycle) {
		XEventClass nextFrontLifeCycle = infoLifeCycle.getEventClasses().getClassOf(event);
		return nextFrontLifeCycle.getId().equals("complete") || nextFrontLifeCycle.getId().equals("");
	}
}
