package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.Iterator;

import org.apache.commons.collections15.iterators.ArrayIterator;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;

public class Dfg {
	private final Graph<XEventClass> directlyFollowsGraph;
//	private final Graph<XEventClass> eventuallyFollowsGraph;
	private final Graph<XEventClass> parallelGraph;
//	private final Graph<XEventClass> uncertainDirectlyFollowsGraph;
//	private final Graph<XEventClass> uncertainEventuallyFollowsGraph;

	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
//	private final MultiSet<XEventClass> uncertainStartActivities;
//	private final MultiSet<XEventClass> uncertainEndActivities;

	public Dfg() {
		this(1);
	}
	
	public Dfg(int initialSize) {
		directlyFollowsGraph = GraphFactory.create(XEventClass.class, initialSize);
//		eventuallyFollowsGraph = GraphFactory.create(XEventClass.class, initialSize);
		parallelGraph = GraphFactory.create(XEventClass.class, initialSize);
//		uncertainDirectlyFollowsGraph = GraphFactory.create(XEventClass.class, initialSize);
//		uncertainEventuallyFollowsGraph = GraphFactory.create(XEventClass.class, initialSize);

		startActivities = new MultiSet<>();
		endActivities = new MultiSet<>();
//		uncertainStartActivities = new MultiSet<>();
//		uncertainEndActivities = new MultiSet<>();
	}

	public Dfg(final Graph<XEventClass> directlyFollowsGraph, final Graph<XEventClass> eventuallyFollowsGraph,
			final Graph<XEventClass> parallelGraph, final Graph<XEventClass> uncertainDirectlyFollowsGraph,
			final Graph<XEventClass> uncertainEventuallyFollowsGraph, final MultiSet<XEventClass> startActivities,
			final MultiSet<XEventClass> endActivities, final MultiSet<XEventClass> uncertainStartActivities,
			final MultiSet<XEventClass> uncertainEndActivities) {
		this.directlyFollowsGraph = directlyFollowsGraph;
//		this.eventuallyFollowsGraph = eventuallyFollowsGraph;
		this.parallelGraph = parallelGraph;
//		this.uncertainDirectlyFollowsGraph = uncertainDirectlyFollowsGraph;
//		this.uncertainEventuallyFollowsGraph = uncertainEventuallyFollowsGraph;

		this.startActivities = startActivities;
		this.endActivities = endActivities;
//		this.uncertainStartActivities = uncertainStartActivities;
//		this.uncertainEndActivities = uncertainEndActivities;
	}

	public void addActivity(XEventClass activity) {
		directlyFollowsGraph.addVertex(activity);
//		eventuallyFollowsGraph.addVertex(activity);
		parallelGraph.addVertex(activity);
//		uncertainDirectlyFollowsGraph.addVertex(activity);
//		uncertainEventuallyFollowsGraph.addVertex(activity);
	}

	public Graph<XEventClass> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}
	
	public Iterable<XEventClass> getActivities() {
		return new Iterable<XEventClass>() {
			public Iterator<XEventClass> iterator() {
				return new ArrayIterator<XEventClass>(directlyFollowsGraph.getVertices());
			}
		};
		
	}

	public Graph<XEventClass> getEventuallyFollowsGraph() {
//		return eventuallyFollowsGraph;
		return null;
	}

	public Graph<XEventClass> getParallelGraph() {
		return parallelGraph;
	}

	public Graph<XEventClass> getUncertainDirectlyFollowsGraph() {
//		return uncertainDirectlyFollowsGraph;
		return null;
	}

	public Graph<XEventClass> getUncertainEventuallyFollowsGraph() {
//		return uncertainEventuallyFollowsGraph;
		return null;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}

	public MultiSet<XEventClass> getUncertainStartActivities() {
//		return uncertainStartActivities;
		return null;
	}
//
	public MultiSet<XEventClass> getUncertainEndActivities() {
//		return uncertainEndActivities;
		return null;
	}

	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality) {
		addActivity(source);
		addActivity(target);
		directlyFollowsGraph.addEdge(source, target, cardinality);
	}

//	public void addEventuallyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality) {
//		addActivity(source);
//		addActivity(target);
//		eventuallyFollowsGraph.addEdge(source, target, cardinality);
//	}

	public void addParallelEdge(final XEventClass a, final XEventClass b, final long cardinality) {
		addActivity(a);
		addActivity(b);
		parallelGraph.addEdge(a, b, cardinality);
	}

//	public void addUncertainDirectlyFollowsEdge(final XEventClass source, final XEventClass target,
//			final long cardinality) {
//		addActivity(source);
//		addActivity(target);
//		uncertainDirectlyFollowsGraph.addEdge(source, target, cardinality);
//	}
//
//	public void addUncertainEventuallyFollowsEdge(final XEventClass source, final XEventClass target,
//			final long cardinality) {
//		addActivity(source);
//		addActivity(target);
//		uncertainEventuallyFollowsGraph.addEdge(source, target, cardinality);
//	}

	public void addStartActivity(XEventClass activity, long cardinality) {
		addActivity(activity);
		startActivities.add(activity, cardinality);
	}

	public void addEndActivity(XEventClass activity, long cardinality) {
		addActivity(activity);
		endActivities.add(activity, cardinality);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (long edgeIndex : directlyFollowsGraph.getEdges()) {
			result.append(directlyFollowsGraph.getEdgeSource(edgeIndex));
			result.append("->");
			result.append(directlyFollowsGraph.getEdgeTargetIndex(edgeIndex));
			result.append(", ");
		}
		return result.toString();
	}

	/**
	 * Adds a directly-follows graph edge (in each direction) for each parallel
	 * edge.
	 */
	public void collapseParallelIntoDirectly() {
		for (long edgeIndex : parallelGraph.getEdges()) {
			directlyFollowsGraph.addEdge(parallelGraph.getEdgeSource(edgeIndex),
					parallelGraph.getEdgeTarget(edgeIndex), parallelGraph.getEdgeWeight(edgeIndex));
			directlyFollowsGraph.addEdge(parallelGraph.getEdgeTarget(edgeIndex),
					parallelGraph.getEdgeSource(edgeIndex), parallelGraph.getEdgeWeight(edgeIndex));
		}
	}
}
