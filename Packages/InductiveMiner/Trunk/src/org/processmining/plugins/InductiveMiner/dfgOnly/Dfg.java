package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class Dfg {
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	private final ListenableUndirectedWeightedGraph<XEventClass, DefaultWeightedEdge> parallelGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainDirectlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainEventuallyFollowsGraph;

	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
	private final MultiSet<XEventClass> uncertainStartActivities;
	private final MultiSet<XEventClass> uncertainEndActivities;

	public Dfg() {
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		parallelGraph = new ListenableUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
		uncertainDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		uncertainEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		startActivities = new MultiSet<>();
		endActivities = new MultiSet<>();
		uncertainStartActivities = new MultiSet<>();
		uncertainEndActivities = new MultiSet<>();
	}

	public void addActivity(XEventClass activity) {
		directlyFollowsGraph.addVertex(activity);
		eventuallyFollowsGraph.addVertex(activity);
		parallelGraph.addVertex(activity);
		uncertainDirectlyFollowsGraph.addVertex(activity);
		uncertainEventuallyFollowsGraph.addVertex(activity);
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public ListenableUndirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getParallelGraph() {
		return parallelGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getUncertainDirectlyFollowsGraph() {
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

	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final double cardinality) {
		addEdgeToGraph(directlyFollowsGraph, source, target, cardinality);
	}
	
	public void addEventuallyFollowsEdge(final XEventClass source, final XEventClass target, final double cardinality) {
		addEdgeToGraph(eventuallyFollowsGraph, source, target, cardinality);
	}
	
	public void addParallelEdge(final XEventClass a, final XEventClass b, final double cardinality) {
		addEdgeToGraph(parallelGraph, a, b, cardinality);
	}
	
	public void addUncertainDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final double cardinality) {
		addEdgeToGraph(uncertainDirectlyFollowsGraph, source, target, cardinality);
	}
	
	public void addUncertainEventuallyFollowsEdge(final XEventClass source, final XEventClass target, final double cardinality) {
		addEdgeToGraph(uncertainEventuallyFollowsGraph, source, target, cardinality);
	}
	
	public static <X> void addEdgeToGraph(final ListenableUndirectedWeightedGraph<X, DefaultWeightedEdge> graph, final X a,
			final X b, final double cardinality) {
		if (graph.containsEdge(a, b)) {
			DefaultWeightedEdge oldEdge = graph.getEdge(a, b);
			graph.setEdgeWeight(oldEdge, cardinality + graph.getEdgeWeight(oldEdge));
		} else {
			DefaultWeightedEdge edge = graph.addEdge(a, b);
			graph.setEdgeWeight(edge, cardinality);
		}
	}
	
	public static <X> void addEdgeToGraph(final DefaultDirectedGraph<X, DefaultWeightedEdge> graph, final X source,
			final X target, final double cardinality) {
		if (graph.containsEdge(source, target)) {
			DefaultWeightedEdge oldEdge = graph.getEdge(source, target);
			graph.setEdgeWeight(oldEdge, cardinality + graph.getEdgeWeight(oldEdge));
		} else {
			DefaultWeightedEdge edge = graph.addEdge(source, target);
			graph.setEdgeWeight(edge, cardinality);
		}
	}
}
