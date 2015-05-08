package org.processmining.plugins.InductiveMiner.mining.logs;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

public class LifeCycles {

	private static int eventsRemoved = 0;
	private static int eventsAdded = 0;

	public static XLog preProcessLog(XLog log, XEventClassifier classifier) {
		XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());

		eventsRemoved = 0;
		eventsAdded = 0;

		for (XTrace trace : log) {
			result.add(preProcessTrace(trace, classifier));
		}

		System.out.println("events added " + eventsAdded);
		System.out.println("events removed " + eventsRemoved);

		return result;
	}

	/**
	 * 
	 * @param trace
	 * @return a copy of the input trace, such that it is consistent.
	 */
	private static XTrace preProcessTrace(XTrace trace, XEventClassifier classifier) {
		Map<String, TIntArrayList> unmatchedStartEvents = getUnmatchedStartEvents(trace, classifier);

		//repair the trace
		XTrace result = new XTraceImpl(trace.getAttributes());
		int i = 0;
		for (XEvent event : trace) {

			if (isComplete(event)) {
				//copy to the new trace
				result.add(event);
			} else if (isStart(event)) {
				//copy to the new trace
				result.add(event);

				String activity = classifier.getClassIdentity(event);
				if (unmatchedStartEvents.containsKey(activity) && unmatchedStartEvents.get(activity).contains(i)) {
					//this start event is not matched; add a corresponding complete event

					XAttributeMap map = (XAttributeMap) event.getAttributes().clone();

					//add life cycle transition
					map.put(XLifecycleExtension.KEY_TRANSITION,
							XFactoryRegistry
									.instance()
									.currentDefault()
									.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, "complete",
											XLifecycleExtension.instance()));

					//remove time stamp
					map.remove(XTimeExtension.KEY_TIMESTAMP);

					XEvent newEvent = new XEventImpl(map);
					result.add(newEvent);
					eventsAdded++;
				}
			} else {
				//non-completion non-start event, do not copy to the new trace
				eventsRemoved++;
			}

			i++;
		}

		return result;
	}

	/**
	 * 
	 * @param trace
	 * @param classifier
	 * @return the unmatched start event indices
	 */
	public static Map<String, TIntArrayList> getUnmatchedStartEvents(Iterable<XEvent> trace, XEventClassifier classifier) {
		Map<String, TIntArrayList> unmatchedStartEvents = new THashMap<>();

		int i = 0;
		for (XEvent event : trace) {
			String activity = classifier.getClassIdentity(event);

			if (isComplete(event)) {
				//this is a completion event; check whether there's an open activity instance
				if (unmatchedStartEvents.containsKey(activity)) {
					//there was an open activity instance; close it

					unmatchedStartEvents.get(activity).removeAt(unmatchedStartEvents.get(activity).size() - 1);
					if (unmatchedStartEvents.get(activity).size() == 0) {
						unmatchedStartEvents.remove(activity);
					}
				} else {
					//there was no open activity; skip
				}
			} else if (isStart(event)) {
				//this is a start event; open an activity instance

				if (!unmatchedStartEvents.containsKey(activity)) {
					unmatchedStartEvents.put(activity, new TIntArrayList());
				}
				unmatchedStartEvents.get(activity).add(i);
			} else {
				//ignore this event as it is neither start nor complete
			}

			i++;
		}
		return unmatchedStartEvents;
	}

	public static boolean isStart(XEvent event) {
		return IMLog.lifeCycleClassifier.getClassIdentity(event).equals("start");
	}

	public static boolean isComplete(XEvent event) {
		return IMLog.lifeCycleClassifier.getClassIdentity(event).equals("complete");
	}
}
