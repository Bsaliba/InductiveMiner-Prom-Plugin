package org.processmining.plugins.InductiveMiner.mining;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;

public class IMLogInfo {

	protected final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph;
	protected final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	protected final DefaultDirectedGraph<XEventClass, DefaultEdge> directlyFollowsTransitiveClosureGraph;

	protected final MultiSet<XEventClass> startActivities;
	protected final MultiSet<XEventClass> endActivities;

	protected final HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	protected final HashMap<XEventClass, Integer> minimumSelfDistances;

	protected final long numberOfTraces;
	protected final long numberOfEvents;
	protected final long numberOfEpsilonTraces;
	protected final int longestTrace;
	protected final int lengthStrongestTrace;
	protected final int strongestDirectEdge;
	protected final int strongestEventualEdge;
	protected final int strongestStartActivity;
	protected final int strongestEndActivity;

	public IMLogInfo(IMLog log) {
		//initialise, read the log
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		startActivities = new MultiSet<XEventClass>();
		endActivities = new MultiSet<XEventClass>();
		minimumSelfDistances = new HashMap<XEventClass, Integer>();
		minimumSelfDistancesBetween = new HashMap<XEventClass, MultiSet<XEventClass>>();
		int numberOfTraces = 0;
		int numberOfEvents = 0;
		int numberOfEpsilonTraces = 0;
		int longestTrace = 0;
		int lengthStrongestTrace = 0;

		XEventClass fromEventClass;
		XEventClass toEventClass;

		//walk trough the log
		HashMap<XEventClass, Integer> eventSeenAt;
		List<XEventClass> readTrace;

		for (IMTrace trace : log) {
			Integer cardinality = log.getCardinalityOf(trace);

			toEventClass = null;
			fromEventClass = null;

			int traceSize = 0;
			eventSeenAt = new HashMap<XEventClass, Integer>();
			readTrace = new LinkedList<XEventClass>();

			for (XEventClass ec : trace) {

				if (!directlyFollowsGraph.containsVertex(ec)) {
					directlyFollowsGraph.addVertex(ec);
					eventuallyFollowsGraph.addVertex(ec);
				}

				fromEventClass = toEventClass;
				toEventClass = ec;

				//add connections to the eventually-follows graph
				DefaultWeightedEdge edge;
				Integer newEventuallyCardinality;
				for (XEventClass eventuallySeen : readTrace) {
					edge = eventuallyFollowsGraph.addEdge(eventuallySeen, toEventClass);
					newEventuallyCardinality = cardinality;
					if (edge == null) {
						edge = eventuallyFollowsGraph.getEdge(eventuallySeen, toEventClass);
						newEventuallyCardinality += (int) eventuallyFollowsGraph.getEdgeWeight(edge);
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

							minimumSelfDistancesBetween.put(toEventClass, new MultiSet<XEventClass>());
						}

						//store the minimum self-distance activities
						MultiSet<XEventClass> mb = minimumSelfDistancesBetween.get(toEventClass);
						mb.addAll(readTrace.subList(eventSeenAt.get(toEventClass) + 1, traceSize), cardinality);
					}
				}
				eventSeenAt.put(toEventClass, traceSize);

				if (fromEventClass != null) {

					//add edge to directly-follows graph
					edge = directlyFollowsGraph.addEdge(fromEventClass, toEventClass);
					Integer newCardinality = cardinality;
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
		int strongestStartActivity = 0;
		for (XEventClass activity : startActivities) {
			strongestStartActivity = Math.max(strongestStartActivity, startActivities.getCardinalityOf(activity));
		}
		this.strongestStartActivity = strongestStartActivity;

		//find the strongest end activity
		int strongestEndActivity = 0;
		for (XEventClass activity : endActivities) {
			strongestEndActivity = Math.max(strongestEndActivity, endActivities.getCardinalityOf(activity));
		}
		this.strongestEndActivity = strongestEndActivity;

		//compute the transitive closure of the directly-follows graph
		directlyFollowsTransitiveClosureGraph = TransitiveClosure.transitiveClosure(directlyFollowsGraph);
	}
	
	public IMLogInfo(DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph,
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph,
			DefaultDirectedGraph<XEventClass, DefaultEdge> directlyFollowsTransitiveClosureGraph,
			MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities,
			HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			HashMap<XEventClass, Integer> minimumSelfDistances, long numberOfTraces, long numberOfEvents,
			long numberOfEpsilonTraces, int longestTrace, int lengthStrongestTrace, int strongestDirectEdge,
			int strongestEventualEdge, int strongestStartActivity, int strongestEndActivity) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.eventuallyFollowsGraph = eventuallyFollowsGraph;
		this.directlyFollowsTransitiveClosureGraph = directlyFollowsTransitiveClosureGraph;
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
		result.append("start activities: " + startActivities + "\n");
		result.append("end activities: " + endActivities);
		return result.toString();
	}

	public Set<XEventClass> getActivities() {
		return directlyFollowsGraph.vertexSet();
	}

	public DefaultDirectedGraph<XEventClass, DefaultEdge> getDirectlyFollowsTransitiveClosureGraph() {
		return directlyFollowsTransitiveClosureGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}

	public HashMap<XEventClass, MultiSet<XEventClass>> getMinimumSelfDistancesBetween() {
		return minimumSelfDistancesBetween;
	}

	public MultiSet<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new MultiSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public HashMap<XEventClass, Integer> getMinimumSelfDistances() {
		return minimumSelfDistances;
	}

	public int getMinimumSelfDistance(XEventClass a) {
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

	public int getLengthStrongestTrace() {
		return lengthStrongestTrace;
	}

	public int getStrongestDirectEdge() {
		return strongestDirectEdge;
	}

	public int getStrongestEventualEdge() {
		return strongestEventualEdge;
	}

	public int getStrongestStartActivity() {
		return strongestStartActivity;
	}

	public int getStrongestEndActivity() {
		return strongestEndActivity;
	}
}
