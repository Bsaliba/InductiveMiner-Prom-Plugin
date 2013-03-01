package bPrime.mining;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import bPrime.Sets;

public class ParallelCut {
	public static Set<Set<XEventClass>> findParallelCut(DirectlyFollowsRelation dfr, boolean useMinimumSelfDistance) {
		
		//choose the graph to use: directly-follows or eventually-follows
		DirectedGraph<XEventClass, DefaultWeightedEdge> graph = dfr.getEventuallyFollowsGraph();
		
		//noise filtering can have removed all start and end activities.
		//if that is the case, return
		if (dfr.getStartActivities().toSet().size() == 0 ||
				dfr.getEndActivities().toSet().size() == 0) {
			Set<Set<XEventClass>> emptyResult = new HashSet<Set<XEventClass>>();
			emptyResult.add(graph.vertexSet());
			return emptyResult;
		}
		
		//construct the negated graph
		DirectedGraph<XEventClass, DefaultEdge> negatedGraph = new DefaultDirectedGraph<XEventClass, DefaultEdge>(DefaultEdge.class);
		
		//add the vertices
		for (XEventClass e : graph.vertexSet()) {
			negatedGraph.addVertex(e);
		}
		
		//walk through the edges and negate them
		for (XEventClass e1 : graph.vertexSet()) {
			for (XEventClass e2 : graph.vertexSet()) {
				if (e1 != e2) {
					if (!graph.containsEdge(e1, e2) || !graph.containsEdge(e2, e1)) {
						negatedGraph.addEdge(e1, e2);
						//debug("add negated edge " + e1 + " -> " + e2);
					}
				}
			}
		}
		
		//if wanted, apply an extension to the B' algorithm to account for loops that have the same directly-follows graph as a parallel operator would have
		//make sure that activities on the minimum-self-distance-path are not separated by a parallel operator
		if (useMinimumSelfDistance) {
			for (XEventClass activity : graph.vertexSet()) {
				for (XEventClass activity2 : dfr.getMinimumSelfDistanceBetween(activity)) {
					negatedGraph.addEdge(activity, activity2);
				}
			}
		}
		
		//debug(dfr.debugGraph());
		
		//compute the connected components of the negated graph
		ConnectivityInspector<XEventClass, DefaultEdge> connectedComponentsGraph = new ConnectivityInspector<XEventClass, DefaultEdge>(negatedGraph);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();
		
		//not all connected components are guaranteed to have start and end activities. Merge those that do not.
		List<Set<XEventClass>> ccsWithStartEnd = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithStart = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithEnd = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithNothing = new LinkedList<Set<XEventClass>>();
		for (Set<XEventClass> cc : connectedComponents) {
			Boolean hasStart = true;
			if (Sets.intersection(cc, dfr.getStartActivities().toSet()).size() == 0) {
				hasStart = false;
			}
			Boolean hasEnd = true;
			if (Sets.intersection(cc, dfr.getEndActivities().toSet()).size() == 0) {
				hasEnd = false;
			}
			if (hasStart) {
				if (hasEnd) {
					ccsWithStartEnd.add(cc);
				} else {
					ccsWithStart.add(cc);
				}
			} else {
				if (hasEnd) {
					ccsWithEnd.add(cc);
				} else {
					ccsWithNothing.add(cc);
				}
			}
		}
		//debug("StartEnd " + ccsWithStartEnd.toString());
		//debug("Start " + ccsWithStart.toString());
		//debug("End " + ccsWithEnd.toString());
		//debug("Nothing " + ccsWithNothing.toString());
		//add full sets
		List<Set<XEventClass>> connectedComponents2 = new LinkedList<Set<XEventClass>>(ccsWithStartEnd);
		//add combinations of end-only and start-only components
		Integer startCounter = 0;
		Integer endCounter = 0;
		while (startCounter < ccsWithStart.size() && endCounter < ccsWithEnd.size()) {
			Set<XEventClass> set = new HashSet<XEventClass>();
			set.addAll(ccsWithStart.get(startCounter));
			set.addAll(ccsWithEnd.get(endCounter));
			connectedComponents2.add(set);
			startCounter++;
			endCounter++;
		}
		//the start-only components can be added to any set
		while (startCounter < ccsWithStart.size()) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(ccsWithStart.get(startCounter));
			connectedComponents2.set(0, set);
			startCounter++;
		}
		//the end-only components can be added to any set
		while (endCounter < ccsWithEnd.size()) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(ccsWithEnd.get(endCounter));
			connectedComponents2.set(0, set);
			endCounter++;
		}
		//the non-start-non-end components can be added to any set
		for (Set<XEventClass> cc : ccsWithNothing) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(cc);
			connectedComponents2.set(0, set);
		}
		
		return new HashSet<Set<XEventClass>>(connectedComponents2);
	}
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
