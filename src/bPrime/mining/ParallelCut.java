package bPrime.mining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ParallelCut {
	public static Set<Set<XEventClass>> findParallelCut(DirectlyFollowsRelation dfr) {
		
		//construct the negated graph
		DirectedGraph<XEventClass, DefaultEdge> negatedGraph = new DefaultDirectedGraph<XEventClass, DefaultEdge>(DefaultEdge.class);
		
		//add the vertices
		for (XEventClass e : dfr.getGraph().vertexSet()) {
			negatedGraph.addVertex(e);
		}
		
		//walk through the edges and negate them
		for (XEventClass e1 : dfr.getGraph().vertexSet()) {
			for (XEventClass e2 : dfr.getGraph().vertexSet()) {
				if (e1 != e2) {
					if (!dfr.getGraph().containsEdge(e1, e2) || !dfr.getGraph().containsEdge(e2, e1)) {
						negatedGraph.addEdge(e1, e2);
					}
				}
			}
		}
		
		System.out.println(dfr.debugGraph());
		
		//compute the connected components of the negated graph
		ConnectivityInspector<XEventClass, DefaultEdge> connectedComponentsGraph = new ConnectivityInspector<XEventClass, DefaultEdge>(negatedGraph);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();
		
		return new HashSet<Set<XEventClass>>(connectedComponents);
	}
}
