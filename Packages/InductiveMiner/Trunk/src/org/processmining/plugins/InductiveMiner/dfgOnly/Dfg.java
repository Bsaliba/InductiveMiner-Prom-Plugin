package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class Dfg {
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	private final SimpleWeightedGraph<XEventClass, DefaultWeightedEdge> parallelGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainDirectlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainEventuallyFollowsGraph;

	private final MultiSet<XEventClass> startActivities;	
	private final MultiSet<XEventClass> endActivities;
	private final MultiSet<XEventClass> uncertainStartActivities;
	private final MultiSet<XEventClass> uncertainEndActivities;

	public Dfg() {
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		parallelGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		uncertainDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		uncertainEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		startActivities = new MultiSet<>();
		endActivities = new MultiSet<>();
		uncertainStartActivities = new MultiSet<>();
		uncertainEndActivities = new MultiSet<>();
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public SimpleWeightedGraph<XEventClass, DefaultWeightedEdge> getParallelGraph() {
		return parallelGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getUncertaintyGraph() {
		return uncertainDirectlyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getUncertainEventuallyFollowsGraph() {
		return uncertainEventuallyFollowsGraph;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}

	public MultiSet<XEventClass> getUncertainStartActivities() {
		return uncertainStartActivities;
	}

	public MultiSet<XEventClass> getUncertainEndActivities() {
		return uncertainEndActivities;
	}
}
