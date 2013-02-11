package bPrime.mining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class ExclusiveChoiceCut {
	public static Set<Set<XEventClass>> findExclusiveChoiceCut(DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> G) {
		
		//compute the connected components of the directly-follows graph
		ConnectivityInspector<XEventClass, DefaultWeightedEdge> connectedComponentsGraph = new ConnectivityInspector<XEventClass, DefaultWeightedEdge>(G);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();
		
		return new HashSet<Set<XEventClass>>(connectedComponents);
	}
}
