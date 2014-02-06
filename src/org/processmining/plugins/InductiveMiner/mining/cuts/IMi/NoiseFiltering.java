package org.processmining.plugins.InductiveMiner.mining.cuts.IMi;



public class NoiseFiltering {
	/*
	public DirectlyFollowsRelation filterNoise(float threshold) {
		//filter start activities
		MultiSet<XEventClass> filteredStartActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : startActivities) {
			if (startActivities.getCardinalityOf(activity) >= strongestStartActivity * threshold) {
				filteredStartActivities.add(activity, startActivities.getCardinalityOf(activity));
			}
		}

		//filter end activities
		MultiSet<XEventClass> filteredEndActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : endActivities) {
			if (endActivities.getCardinalityOf(activity) >= strongestEndActivity * threshold) {
				filteredEndActivities.add(activity, endActivities.getCardinalityOf(activity));
			}
		}

		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {
			filteredDirectlyFollowsGraph.addVertex(activity);
		}
		//add edges

		/*
		 * //method 1: global threshold for (DefaultWeightedEdge edge :
		 * directlyFollowsGraph.edgeSet()) { if
		 * (directlyFollowsGraph.getEdgeWeight(edge) >= strongestDirectEdge *
		 * threshold) { XEventClass from =
		 * directlyFollowsGraph.getEdgeSource(edge); XEventClass to =
		 * directlyFollowsGraph.getEdgeTarget(edge); DefaultWeightedEdge
		 * filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
		 * filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge,
		 * directlyFollowsGraph.getEdgeWeight(edge)); } }
		 */

	/*
		//method 2: local threshold
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = endActivities.getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : directlyFollowsGraph.outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) directlyFollowsGraph.getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : directlyFollowsGraph.outgoingEdgesOf(activity)) {
				if (directlyFollowsGraph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = directlyFollowsGraph.getEdgeSource(edge);
					XEventClass to = directlyFollowsGraph.getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
					filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, directlyFollowsGraph.getEdgeWeight(edge));
				}
			}
		}

		//filter eventually-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : eventuallyFollowsGraph.vertexSet()) {
			filteredEventuallyFollowsGraph.addVertex(activity);
		}
		//add edges
		/*
		 * //method 1: global threshold for (DefaultWeightedEdge edge :
		 * eventuallyFollowsGraph.edgeSet()) { if
		 * (eventuallyFollowsGraph.getEdgeWeight(edge) >= strongestEventualEdge
		 * * threshold) { XEventClass from =
		 * eventuallyFollowsGraph.getEdgeSource(edge); XEventClass to =
		 * eventuallyFollowsGraph.getEdgeTarget(edge); DefaultWeightedEdge
		 * filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
		 * filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge,
		 * eventuallyFollowsGraph.getEdgeWeight(edge)); } }
		 */
/*
		//method 2: local threshold
		for (XEventClass activity : eventuallyFollowsGraph.vertexSet()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = endActivities.getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : eventuallyFollowsGraph.outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) eventuallyFollowsGraph.getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : eventuallyFollowsGraph.outgoingEdgesOf(activity)) {
				if (eventuallyFollowsGraph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = eventuallyFollowsGraph.getEdgeSource(edge);
					XEventClass to = eventuallyFollowsGraph.getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
					filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge,
							eventuallyFollowsGraph.getEdgeWeight(edge));
				}
			}
		}

		return new DirectlyFollowsRelation(filteredDirectlyFollowsGraph, filteredEventuallyFollowsGraph,
				filteredStartActivities, filteredEndActivities, minimumSelfDistancesBetween, minimumSelfDistances,
				numberOfEpsilonTraces, longestTrace, lengthStrongestTrace, strongestDirectEdge, strongestEventualEdge,
				strongestStartActivity, strongestEndActivity);
	}
	*/
	
	/*
	 * Try to guess new parallel edges
	 */
	/*
	public DirectlyFollowsRelation addIncompleteEdges(float threshold) {
		//don't filter start activities
		MultiSet<XEventClass> filteredStartActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : startActivities) {
			filteredStartActivities.add(activity, startActivities.getCardinalityOf(activity));
		}

		//don't filter end activities
		MultiSet<XEventClass> filteredEndActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : endActivities) {
			filteredEndActivities.add(activity, endActivities.getCardinalityOf(activity));
		}

		//debug("add incomplete edges");

		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {
			filteredDirectlyFollowsGraph.addVertex(activity);
		}
		//add edges
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {

			//add all outgoing edges of this node that are already present
			for (DefaultWeightedEdge edge : directlyFollowsGraph.outgoingEdgesOf(activity)) {
				XEventClass from = directlyFollowsGraph.getEdgeSource(edge);
				XEventClass to = directlyFollowsGraph.getEdgeTarget(edge);
				DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
				double weight = directlyFollowsGraph.getEdgeWeight(edge);
				filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, weight);

				//see if this edge is weak enough to justify adding the reversed edge
				//debug(" check edge " + from + " -> " + to + " weight " + weight + " expected reverse weight " + Math.exp(1 / (1-threshold)));
				if (threshold != 0 && (threshold == 1 || weight < Math.exp(1 / (1 - threshold)))) {
					//if the reversed edge is not present, add it
					if (!directlyFollowsGraph.containsEdge(to, from)) {
						//debug("  add edge " + to + " -> " + from);
						DefaultWeightedEdge reversedEdge = filteredDirectlyFollowsGraph.addEdge(to, from);
						filteredDirectlyFollowsGraph.setEdgeWeight(reversedEdge, 1);
					} else {
						//debug("  edge " + to + " -> " + from + " exists already");
					}
				}
			}

		}

		return new DirectlyFollowsRelation(filteredDirectlyFollowsGraph, null, filteredStartActivities,
				filteredEndActivities, minimumSelfDistancesBetween, minimumSelfDistances, numberOfEpsilonTraces,
				longestTrace, lengthStrongestTrace, strongestDirectEdge, strongestEventualEdge, strongestStartActivity,
				strongestEndActivity);
	}*/
}
