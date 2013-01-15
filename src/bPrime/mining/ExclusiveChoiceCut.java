package bPrime.mining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ExclusiveChoiceCut {
	public static Set<Set<XEventClass>> findExclusiveChoiceCut(DefaultDirectedGraph<XEventClass, DefaultEdge> G) {
		
		//compute the connected components of the directly-follows graph
		ConnectivityInspector<XEventClass, DefaultEdge> connectedComponentsGraph = new ConnectivityInspector<XEventClass, DefaultEdge>(G);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();
		
		return new HashSet<Set<XEventClass>>(connectedComponents);
	}
}
