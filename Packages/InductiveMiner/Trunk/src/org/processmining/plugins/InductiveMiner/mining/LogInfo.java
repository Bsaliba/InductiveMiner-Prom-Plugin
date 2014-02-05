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
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMTrace;

public class LogInfo {

	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	private final DefaultDirectedGraph<XEventClass, DefaultEdge> directlyFollowsTransitiveClosureGraph;

	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;

	private final HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	private final HashMap<XEventClass, Integer> minimumSelfDistances;

	private long numberOfTraces;
	private long numberOfEvents;
	private int numberOfEpsilonTraces;
	private int longestTrace;
	private int lengthStrongestTrace;
	private int strongestDirectEdge;
	private int strongestEventualEdge;
	private int strongestStartActivity;
	private int strongestEndActivity;

	public LogInfo(IMLog log) {
		//initialise, read the log
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		startActivities = new MultiSet<XEventClass>();
		endActivities = new MultiSet<XEventClass>();
		minimumSelfDistances = new HashMap<XEventClass, Integer>();
		minimumSelfDistancesBetween = new HashMap<XEventClass, MultiSet<XEventClass>>();
		numberOfTraces = 0;
		numberOfEvents = 0;
		numberOfEpsilonTraces = 0;
		longestTrace = 0;
		lengthStrongestTrace = 0;

		XEventClass fromEventClass;
		XEventClass toEventClass;

		//walk trough the log
		HashMap<XEventClass, Integer> eventSeenAt;
		List<XEventClass> readTrace;

		for (IMTrace trace: log) {
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

		//find the edge with the greatest weight
		strongestDirectEdge = 0;
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			strongestDirectEdge = Math.max(strongestDirectEdge, (int) directlyFollowsGraph.getEdgeWeight(edge));
		}

		//find the edge with the greatest weight
		strongestEventualEdge = 0;
		for (DefaultWeightedEdge edge : eventuallyFollowsGraph.edgeSet()) {
			strongestEventualEdge = Math.max(strongestEventualEdge, (int) eventuallyFollowsGraph.getEdgeWeight(edge));
		}

		//find the strongest start activity
		strongestStartActivity = 0;
		for (XEventClass activity : startActivities) {
			strongestStartActivity = Math.max(strongestStartActivity, startActivities.getCardinalityOf(activity));
		}

		//find the strongest end activity
		strongestEndActivity = 0;
		for (XEventClass activity : endActivities) {
			strongestEndActivity = Math.max(strongestEndActivity, endActivities.getCardinalityOf(activity));
		}

		//compute the transitive closure of the directly-follows graph
		directlyFollowsTransitiveClosureGraph = TransitiveClosure.transitiveClosure(directlyFollowsGraph);
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
	
	public MultiSet<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new MultiSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
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

	public int getNumberOfEpsilonTraces() {
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
