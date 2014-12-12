package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class Dfg {
	private final Graph<XEventClass> directlyFollowsGraph;
	private final Graph<XEventClass> eventuallyFollowsGraph;
	private final Graph<XEventClass> parallelGraph;
	private final Graph<XEventClass> uncertainDirectlyFollowsGraph;
	private final Graph<XEventClass> uncertainEventuallyFollowsGraph;

	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
	private final MultiSet<XEventClass> uncertainStartActivities;
	private final MultiSet<XEventClass> uncertainEndActivities;

	public Dfg() {
		directlyFollowsGraph = new Graph<XEventClass>(XEventClass.class);
		eventuallyFollowsGraph = new Graph<XEventClass>(XEventClass.class);
		parallelGraph = new Graph<XEventClass>(XEventClass.class);
		uncertainDirectlyFollowsGraph = new Graph<XEventClass>(XEventClass.class);
		uncertainEventuallyFollowsGraph = new Graph<XEventClass>(XEventClass.class);

		startActivities = new MultiSet<>();
		endActivities = new MultiSet<>();
		uncertainStartActivities = new MultiSet<>();
		uncertainEndActivities = new MultiSet<>();
	}

	public Dfg(final Graph<XEventClass> directlyFollowsGraph,
			final Graph<XEventClass> eventuallyFollowsGraph,
			final Graph<XEventClass> parallelGraph,
			final Graph<XEventClass> uncertainDirectlyFollowsGraph,
			final Graph<XEventClass> uncertainEventuallyFollowsGraph,
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

	public Graph<XEventClass> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public Graph<XEventClass> getParallelGraph() {
		return parallelGraph;
	}

	public Graph<XEventClass> getUncertainDirectlyFollowsGraph() {
		return uncertainDirectlyFollowsGraph;
	}

	public Graph<XEventClass> getUncertainEventuallyFollowsGraph() {
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

	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality) {
		addActivity(source);
		addActivity(target);
		directlyFollowsGraph.addEdge(source, target, cardinality);
	}

	public void addEventuallyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality) {
		addActivity(source);
		addActivity(target);
		eventuallyFollowsGraph.addEdge(source, target, cardinality);
	}

	public void addParallelEdge(final XEventClass a, final XEventClass b, final long cardinality) {
		addActivity(a);
		addActivity(b);
		parallelGraph.addEdge(a, b, cardinality);
	}

	public void addUncertainDirectlyFollowsEdge(final XEventClass source, final XEventClass target,
			final long cardinality) {
		addActivity(source);
		addActivity(target);
		uncertainDirectlyFollowsGraph.addEdge(source, target, cardinality);
	}

	public void addUncertainEventuallyFollowsEdge(final XEventClass source, final XEventClass target,
			final long cardinality) {
		addActivity(source);
		addActivity(target);
		uncertainEventuallyFollowsGraph.addEdge(source, target, cardinality);
	}
	
	public void addStartActivity(XEventClass activity, long cardinality)  {
		startActivities.add(activity, cardinality);
	}
	
	public void addEndActivity(XEventClass activity, long cardinality)  {
		endActivities.add(activity, cardinality);
	}
}
