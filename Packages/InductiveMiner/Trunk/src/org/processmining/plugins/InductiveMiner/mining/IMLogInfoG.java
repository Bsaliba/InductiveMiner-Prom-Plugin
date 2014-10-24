package org.processmining.plugins.InductiveMiner.mining;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class IMLogInfoG<X> {
	protected final Graph<X> directlyFollowsGraph;
	protected final Graph<X> directlyFollowsTransitiveClosureGraph;

	protected final MultiSet<X> activities;
	protected final MultiSet<X> startActivities;
	protected final MultiSet<X> endActivities;

	protected final Map<X, MultiSet<X>> minimumSelfDistancesBetween;
	protected final Map<X, Integer> minimumSelfDistances;

	protected final long numberOfEvents;
	protected final long numberOfEpsilonTraces;
	protected final long highestTraceCardinality;
	protected final long occurencesOfMostOccuringDirectEdge;
	protected final X mostOccurringStartActivity;
	protected final X mostOccurringEndActivity;
	
	public IMLogInfoG(MultiSet<? extends IMTraceG<X>> log) {
		directlyFollowsGraph = null;
		directlyFollowsTransitiveClosureGraph = null;
		activities = null;
		startActivities = null;
		endActivities = null;
		minimumSelfDistances = null;
		minimumSelfDistancesBetween = null;
		numberOfEvents = 1;
		numberOfEpsilonTraces = 1;
		highestTraceCardinality = 1;
		occurencesOfMostOccuringDirectEdge = 1;
		mostOccurringStartActivity = null;
		mostOccurringEndActivity = null;
	}

	public IMLogInfoG(Class<X> clazz, MultiSet<? extends IMTraceG<X>> log) {
		//initialise, read the log
		directlyFollowsGraph = new Graph<X>(clazz);
		activities = new MultiSet<X>();
		startActivities = new MultiSet<X>();
		endActivities = new MultiSet<X>();
		minimumSelfDistances = new THashMap<X, Integer>();
		minimumSelfDistancesBetween = new THashMap<X, MultiSet<X>>();
		long numberOfEvents = 0;
		long numberOfEpsilonTraces = 0;
		int longestTrace = 0;
		long highestTraceCardinality = 0;

		X fromEventClass;
		X toEventClass;

		//walk trough the log
		Map<X, Integer> eventSeenAt;
		List<X> readTrace;

		for (List<X> trace : log) {
			long cardinality = log.getCardinalityOf(trace);

			toEventClass = null;
			fromEventClass = null;

			int traceSize = 0;
			eventSeenAt = new THashMap<X, Integer>();
			readTrace = new ArrayList<X>();

			for (X ec : trace) {

				activities.add(ec, cardinality);
				directlyFollowsGraph.addVertex(ec);

				fromEventClass = toEventClass;
				toEventClass = ec;

				readTrace.add(toEventClass);

				if (eventSeenAt.containsKey(toEventClass)) {
					//we have detected an activity for the second time
					//check whether this is shorter than what we had already seen
					int oldDistance = Integer.MAX_VALUE;
					if (minimumSelfDistances.containsKey(toEventClass)) {
						oldDistance = minimumSelfDistances.get(toEventClass);
					}

					if (!minimumSelfDistances.containsKey(toEventClass)
							|| traceSize - eventSeenAt.get(toEventClass) <= oldDistance) {
						//keep the new minimum self distance
						int newDistance = traceSize - eventSeenAt.get(toEventClass);
						if (oldDistance > newDistance) {
							//we found a shorter minimum self distance, record and restart with a new multiset
							minimumSelfDistances.put(toEventClass, newDistance);

							minimumSelfDistancesBetween.put(toEventClass, new MultiSet<X>());
						}

						//store the minimum self-distance activities
						MultiSet<X> mb = minimumSelfDistancesBetween.get(toEventClass);
						mb.addAll(readTrace.subList(eventSeenAt.get(toEventClass) + 1, traceSize), cardinality);
					}
				}
				eventSeenAt.put(toEventClass, traceSize);
				{
					if (fromEventClass != null) {
						//add edge to directly-follows graph
						directlyFollowsGraph.addEdge(fromEventClass, toEventClass, cardinality);
					} else {
						//add edge to start activities
						startActivities.add(toEventClass, cardinality);
					}
				}

				traceSize += 1;
			}

			//update the longest-trace-counter
			if (traceSize > longestTrace) {
				longestTrace = traceSize;
			}

			numberOfEvents += traceSize * cardinality;

			highestTraceCardinality = Math.max(highestTraceCardinality, cardinality);

			if (toEventClass != null) {
				endActivities.add(toEventClass, cardinality);
			}

			if (traceSize == 0) {
				numberOfEpsilonTraces = numberOfEpsilonTraces + cardinality;
			}
		}
		//debug(minimumSelfDistancesBetween.toString());

		//copy local fields to class fields
		this.numberOfEvents = numberOfEvents;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
		this.highestTraceCardinality = highestTraceCardinality;

		//find the edge with the greatest weight
		this.occurencesOfMostOccuringDirectEdge = directlyFollowsGraph.getWeightOfHeaviestEdge();

		//find the strongest start and end activities
		{
			long occurrencesOfMostOccuringStartActivity = 0;
			long occurrencesOfMostOccuringEndActivity = 0;
			X mostOccurringStartActivity = null;
			X mostOccurringEndActivity = null;
			for (X activity : startActivities) {
				if (startActivities.getCardinalityOf(activity) > occurrencesOfMostOccuringStartActivity) {
					occurrencesOfMostOccuringStartActivity = startActivities.getCardinalityOf(activity);
					mostOccurringStartActivity = activity;
				}
				if (endActivities.getCardinalityOf(activity) > occurrencesOfMostOccuringEndActivity) {
					occurrencesOfMostOccuringEndActivity = endActivities.getCardinalityOf(activity);
					mostOccurringEndActivity = activity;
				}
			}
			this.mostOccurringStartActivity = mostOccurringStartActivity;
			this.mostOccurringEndActivity = mostOccurringEndActivity;
		}

		//compute the transitive closure of the directly-follows graph
		directlyFollowsTransitiveClosureGraph = TransitiveClosure.transitiveClosure(clazz, directlyFollowsGraph);
	}

	public IMLogInfoG(Graph<X> directlyFollowsGraph, Graph<X> directlyFollowsTransitiveClosureGraph,
			MultiSet<X> activities, MultiSet<X> startActivities, MultiSet<X> endActivities,
			Map<X, MultiSet<X>> minimumSelfDistancesBetween, Map<X, Integer> minimumSelfDistances,
			long numberOfEvents, long numberOfEpsilonTraces, long lengthStrongestTrace, long strongestDirectEdge,
			X mostOccurringStartActivity, X mostOccurringEndActivity) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.directlyFollowsTransitiveClosureGraph = directlyFollowsTransitiveClosureGraph;
		this.activities = activities;
		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfEvents = numberOfEvents;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
		this.highestTraceCardinality = lengthStrongestTrace;
		this.occurencesOfMostOccuringDirectEdge = strongestDirectEdge;
		this.mostOccurringStartActivity = mostOccurringStartActivity;
		this.mostOccurringEndActivity = mostOccurringEndActivity;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("number of events: " + numberOfEvents + "\n");
		result.append("start activities: " + startActivities + "\n");
		result.append("end activities: " + endActivities);
		return result.toString();
	}

	public MultiSet<X> getActivities() {
		return activities;
	}

	public Graph<X> getDirectlyFollowsTransitiveClosureGraph() {
		return directlyFollowsTransitiveClosureGraph;
	}

	public Graph<X> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public MultiSet<X> getStartActivities() {
		return startActivities;
	}

	public MultiSet<X> getEndActivities() {
		return endActivities;
	}

	public Map<X, MultiSet<X>> getMinimumSelfDistancesBetween() {
		return minimumSelfDistancesBetween;
	}

	public MultiSet<X> getMinimumSelfDistanceBetween(X activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new MultiSet<X>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public Map<X, Integer> getMinimumSelfDistances() {
		return minimumSelfDistances;
	}

	public int getMinimumSelfDistance(X a) {
		if (minimumSelfDistances.containsKey(a)) {
			return minimumSelfDistances.get(a);
		}
		return 0;
	}

	public long getNumberOfEvents() {
		return numberOfEvents;
	}

	/**
	 * Gives the number of empty traces in the log.
	 * 
	 * @return
	 */
	public long getNumberOfEpsilonTraces() {
		return numberOfEpsilonTraces;
	}

	/**
	 * Gives the number of times the trace that occurs the most occurs in the
	 * log.
	 * 
	 * @return
	 */
	public long getHighestTraceCardinality() {
		return highestTraceCardinality;
	}

	/**
	 * Gives the number of times the directed edge that occurs the most occurs
	 * in the log.
	 * 
	 * @return
	 */
	public long getOccurencesOfMostOccuringDirectEdge() {
		return occurencesOfMostOccuringDirectEdge;
	}

	public X getMostOccurringStartActivity() {
		return mostOccurringStartActivity;
	}

	public long getOccurrencesOfMostOccurringStartActivity() {
		return startActivities.getCardinalityOf(mostOccurringStartActivity);
	}

	public X getMostOccurringEndActivity() {
		return mostOccurringEndActivity;
	}

	public long getOccurrencesOfMostOccurringEndActivity() {
		return endActivities.getCardinalityOf(mostOccurringEndActivity);
	}
}
