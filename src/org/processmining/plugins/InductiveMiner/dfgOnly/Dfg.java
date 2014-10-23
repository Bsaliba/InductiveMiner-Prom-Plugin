package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Pseudograph;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class Dfg {
	private final Graph<XEventClass> directlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	private final Pseudograph<XEventClass, DefaultWeightedEdge> parallelGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainDirectlyFollowsGraph;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainEventuallyFollowsGraph;

	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
	private final MultiSet<XEventClass> uncertainStartActivities;
	private final MultiSet<XEventClass> uncertainEndActivities;

	public Dfg() {
		directlyFollowsGraph = new Graph<XEventClass>(XEventClass.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		parallelGraph = new Pseudograph<>(DefaultWeightedEdge.class);
		uncertainDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		uncertainEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		startActivities = new MultiSet<>();
		endActivities = new MultiSet<>();
		uncertainStartActivities = new MultiSet<>();
		uncertainEndActivities = new MultiSet<>();
	}

	public Dfg(final Graph<XEventClass> directlyFollowsGraph,
			final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph,
			final Pseudograph<XEventClass, DefaultWeightedEdge> parallelGraph,
			final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainDirectlyFollowsGraph,
			final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> uncertainEventuallyFollowsGraph,
			final MultiSet<XEventClass> startActivities, final MultiSet<XEventClass> endActivities,
			final MultiSet<XEventClass> uncertainStartActivities, final MultiSet<XEventClass> uncertainEndActivities) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.eventuallyFollowsGraph = eventuallyFollowsGraph;
		this.parallelGraph = parallelGraph;
		this.uncertainDirectlyFollowsGraph = uncertainDirectlyFollowsGraph;
		this.uncertainEventuallyFollowsGraph = uncertainEventuallyFollowsGraph;

		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.uncertainStartActivities = uncertainStartActivities;
		this.uncertainEndActivities = uncertainEndActivities;
	}

	public void addActivity(XEventClass activity) {
		directlyFollowsGraph.addVertex(activity);
		eventuallyFollowsGraph.addVertex(activity);
		parallelGraph.addVertex(activity);
		uncertainDirectlyFollowsGraph.addVertex(activity);
		uncertainEventuallyFollowsGraph.addVertex(activity);
	}

	public Graph<XEventClass> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public Pseudograph<XEventClass, DefaultWeightedEdge> getParallelGraph() {
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
		addActivity(source);
		addActivity(target);
		directlyFollowsGraph.addEdge(source, target, 1);
	}

	public void addEventuallyFollowsEdge(final XEventClass source, final XEventClass target, final double cardinality) {
		addActivity(source);
		addActivity(target);
		addEdgeToGraph(eventuallyFollowsGraph, source, target, cardinality);
	}

	public void addParallelEdge(final XEventClass a, final XEventClass b, final double cardinality) {
		addActivity(a);
		addActivity(b);
		addEdgeToGraph(parallelGraph, a, b, cardinality);
	}

	public void addUncertainDirectlyFollowsEdge(final XEventClass source, final XEventClass target,
			final double cardinality) {
		addActivity(source);
		addActivity(target);
		addEdgeToGraph(uncertainDirectlyFollowsGraph, source, target, cardinality);
	}

	public void addUncertainEventuallyFollowsEdge(final XEventClass source, final XEventClass target,
			final double cardinality) {
		addActivity(source);
		addActivity(target);
		addEdgeToGraph(uncertainEventuallyFollowsGraph, source, target, cardinality);
	}

	public static <X> void addEdgeToGraph(final Pseudograph<X, DefaultWeightedEdge> graph, final X a, final X b,
			final double cardinality) {
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
