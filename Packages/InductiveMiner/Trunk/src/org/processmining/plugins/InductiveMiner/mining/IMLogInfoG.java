package org.processmining.plugins.InductiveMiner.mining;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;

public class IMLogInfoG<X> {
	protected final DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> directlyFollowsGraph;
	protected final DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> eventuallyFollowsGraph;
	protected final DefaultDirectedGraph<X, DefaultEdge> directlyFollowsTransitiveClosureGraph;

	protected final MultiSet<X> activities;
	protected final MultiSet<X> startActivities;
	protected final MultiSet<X> endActivities;

	protected final HashMap<X, MultiSet<X>> minimumSelfDistancesBetween;
	protected final HashMap<X, Integer> minimumSelfDistances;

	protected final long numberOfTraces;
	protected final long numberOfEvents;
	protected final long numberOfEpsilonTraces;
	protected final int longestTrace;
	protected final long lengthStrongestTrace;
	protected final long strongestDirectEdge;
	protected final long strongestEventualEdge;
	protected final long strongestStartActivity;
	protected final long strongestEndActivity;
	
	public IMLogInfoG(MultiSet<? extends IMTraceG<X>> log) {
		//initialise, read the log
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<X, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<X, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		activities = new MultiSet<X>();
		startActivities = new MultiSet<X>();
		endActivities = new MultiSet<X>();
		minimumSelfDistances = new HashMap<X, Integer>();
		minimumSelfDistancesBetween = new HashMap<X, MultiSet<X>>();
		int numberOfTraces = 0;
		long numberOfEvents = 0;
		long numberOfEpsilonTraces = 0;
		int longestTrace = 0;
		long lengthStrongestTrace = 0;

		X fromEventClass;
		X toEventClass;

		//walk trough the log
		HashMap<X, Integer> eventSeenAt;
		List<X> readTrace;

		for (List<X> trace : log) {
			long cardinality = log.getCardinalityOf(trace);

			toEventClass = null;
			fromEventClass = null;

			int traceSize = 0;
			eventSeenAt = new HashMap<X, Integer>();
			readTrace = new LinkedList<X>();

			for (X ec : trace) {

				activities.add(ec, cardinality);
				if (!directlyFollowsGraph.containsVertex(ec)) {
					directlyFollowsGraph.addVertex(ec);
					eventuallyFollowsGraph.addVertex(ec);
				}

				fromEventClass = toEventClass;
				toEventClass = ec;

				//add connections to the eventually-follows graph
				DefaultWeightedEdge edge;
				long newEventuallyCardinality;
				for (X eventuallySeen : readTrace) {
					edge = eventuallyFollowsGraph.addEdge(eventuallySeen, toEventClass);
					newEventuallyCardinality = cardinality;
					if (edge == null) {
						edge = eventuallyFollowsGraph.getEdge(eventuallySeen, toEventClass);
						newEventuallyCardinality += eventuallyFollowsGraph.getEdgeWeight(edge);
					}
					eventuallyFollowsGraph.setEdgeWeight(edge, newEventuallyCardinality);
				}

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

				if (fromEventClass != null) {

					//add edge to directly-follows graph
					edge = directlyFollowsGraph.addEdge(fromEventClass, toEventClass);
					long newCardinality = cardinality;
					if (edge == null) {
						edge = directlyFollowsGraph.getEdge(fromEventClass, toEventClass);
						newCardinality = newCardinality + (int) (directlyFollowsGraph.getEdgeWeight(edge));
					}
					directlyFollowsGraph.setEdgeWeight(edge, newCardinality);

				} else {
					startActivities.add(toEventClass, cardinality);
				}

				traceSize += 1;
			}

			//update the longest-trace-counter
			if (traceSize > longestTrace) {
				longestTrace = traceSize;
			}

			numberOfTraces += cardinality;
			numberOfEvents += traceSize * cardinality;

			lengthStrongestTrace = Math.max(lengthStrongestTrace, cardinality);

			if (toEventClass != null) {
				endActivities.add(toEventClass, cardinality);
			}

			if (traceSize == 0) {
				numberOfEpsilonTraces = numberOfEpsilonTraces + cardinality;
			}
		}
		//debug(minimumSelfDistancesBetween.toString());

		//copy local fields to class fields
		this.numberOfTraces = numberOfTraces;
		this.numberOfEvents = numberOfEvents;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
		this.longestTrace = longestTrace;
		this.lengthStrongestTrace = lengthStrongestTrace;

		//find the edge with the greatest weight
		int strongestDirectEdge = 0;
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			strongestDirectEdge = Math.max(strongestDirectEdge, (int) directlyFollowsGraph.getEdgeWeight(edge));
		}
		this.strongestDirectEdge = strongestDirectEdge;

		//find the edge with the greatest weight
		int strongestEventualEdge = 0;
		for (DefaultWeightedEdge edge : eventuallyFollowsGraph.edgeSet()) {
			strongestEventualEdge = Math.max(strongestEventualEdge, (int) eventuallyFollowsGraph.getEdgeWeight(edge));
		}
		this.strongestEventualEdge = strongestEventualEdge;

		//find the strongest start activity
		long strongestStartActivity = 0;
		for (X activity : startActivities) {
			strongestStartActivity = Math.max(strongestStartActivity, startActivities.getCardinalityOf(activity));
		}
		this.strongestStartActivity = strongestStartActivity;

		//find the strongest end activity
		long strongestEndActivity = 0;
		for (X activity : endActivities) {
			strongestEndActivity = Math.max(strongestEndActivity, endActivities.getCardinalityOf(activity));
		}
		this.strongestEndActivity = strongestEndActivity;

		//compute the transitive closure of the directly-follows graph
		directlyFollowsTransitiveClosureGraph = TransitiveClosure.transitiveClosure(directlyFollowsGraph);
	}
	
	public IMLogInfoG(DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> directlyFollowsGraph,
			DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> eventuallyFollowsGraph,
			DefaultDirectedGraph<X, DefaultEdge> directlyFollowsTransitiveClosureGraph,
			MultiSet<X> activities,
			MultiSet<X> startActivities, MultiSet<X> endActivities,
			HashMap<X, MultiSet<X>> minimumSelfDistancesBetween,
			HashMap<X, Integer> minimumSelfDistances, long numberOfTraces, long numberOfEvents,
			long numberOfEpsilonTraces, int longestTrace, long lengthStrongestTrace, long strongestDirectEdge,
			long strongestEventualEdge, long strongestStartActivity, long strongestEndActivity) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.eventuallyFollowsGraph = eventuallyFollowsGraph;
		this.directlyFollowsTransitiveClosureGraph = directlyFollowsTransitiveClosureGraph;
		this.activities = activities;
		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfTraces = numberOfTraces;
		this.numberOfEvents = numberOfEvents;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
		this.longestTrace = longestTrace;
		this.lengthStrongestTrace = lengthStrongestTrace;
		this.strongestDirectEdge = strongestDirectEdge;
		this.strongestEventualEdge = strongestEventualEdge;
		this.strongestStartActivity = strongestStartActivity;
		this.strongestEndActivity = strongestEndActivity;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("number of traces: " + numberOfTraces + "\n");
		result.append("number of events: " + numberOfEvents + "\n");
		result.append("start activities: " + startActivities + "\n");
		result.append("end activities: " + endActivities);
		return result.toString();
	}

	public MultiSet<X> getActivities() {
		return activities;
	}

	public DefaultDirectedGraph<X, DefaultEdge> getDirectlyFollowsTransitiveClosureGraph() {
		return directlyFollowsTransitiveClosureGraph;
	}

	public DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<X, DefaultWeightedEdge> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public MultiSet<X> getStartActivities() {
		return startActivities;
	}

	public MultiSet<X> getEndActivities() {
		return endActivities;
	}

	public HashMap<X, MultiSet<X>> getMinimumSelfDistancesBetween() {
		return minimumSelfDistancesBetween;
	}

	public MultiSet<X> getMinimumSelfDistanceBetween(X activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new MultiSet<X>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public HashMap<X, Integer> getMinimumSelfDistances() {
		return minimumSelfDistances;
	}

	public int getMinimumSelfDistance(X a) {
		if (minimumSelfDistances.containsKey(a)) {
			return minimumSelfDistances.get(a);
		}
		return 0;
	}

	public long getNumberOfTraces() {
		return numberOfTraces;
	}

	public long getNumberOfEvents() {
		return numberOfEvents;
	}

	public long getNumberOfEpsilonTraces() {
		return numberOfEpsilonTraces;
	}

	public int getLongestTrace() {
		return longestTrace;
	}

	public long getLengthStrongestTrace() {
		return lengthStrongestTrace;
	}

	public long getStrongestDirectEdge() {
		return strongestDirectEdge;
	}

	public long getStrongestEventualEdge() {
		return strongestEventualEdge;
	}

	public long getStrongestStartActivity() {
		return strongestStartActivity;
	}

	public long getStrongestEndActivity() {
		return strongestEndActivity;
	}
}
