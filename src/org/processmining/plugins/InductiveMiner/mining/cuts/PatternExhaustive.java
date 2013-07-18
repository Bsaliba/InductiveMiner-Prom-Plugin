package org.processmining.plugins.InductiveMiner.mining.cuts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class PatternExhaustive {
	public static List<Set<XEventClass>> findCut(DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> G) {
		int bits = G.vertexSet().size() - 1;
		List<XEventClass> activities = new ArrayList<XEventClass>(G.vertexSet());
		List<Set<XEventClass>> cut;
		
		//exhaustively try all possibilities
		for (int i=0;i<Math.pow(2, bits);i++) {
			boolean[] p = getCutWithNumber(i, bits);
			
			//make the cut
			cut = new ArrayList<Set<XEventClass>>(2);
			cut.add(new HashSet<XEventClass>());
			cut.add(new HashSet<XEventClass>());
			cut.get(0).add(activities.get(activities.size()-1));
			for (int j=0;j<bits;j++) {
				if (p[j]) {
					cut.get(0).add(activities.get(j));
				} else {
					cut.get(1).add(activities.get(j));
				}
			}
			
			//the cut cannot be trivial
			if (cut.get(1).size() == 0) {
				continue;
			}
			
			
		}
		
		
		return null;
	}
	
	private static boolean[] getCutWithNumber(long number, int bits) {
		boolean[] result = new boolean[bits];
	    for (int i = bits-1; i >= 0; i--) {
	        result[i] = (number & (1 << i)) != 0;
	    }
	    return result;
	}
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
