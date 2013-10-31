package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Matrix;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class DebugProbabilities {
	
	public static String debug(DirectlyFollowsRelation relation, MiningParameters parameters, boolean useHTML) {
		
		String newLine;
		if (useHTML) {
			newLine = "<br>\n";
		} else {
			newLine = "\n";
		}
		
		
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		parameters.getSatProbabilities().setDirectlyFollowsRelation(relation);

		if (graph.vertexSet().size() == 1) {
			return "";
		}

		StringBuilder r = new StringBuilder();
		{
			r.append("xor");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityXor(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("sequence");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilitySequence(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("parallel");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityParallel(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("loop single");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopSingle(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}
		
		{
			r.append("loop indirect");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopIndirect(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}
		
		{
			r.append("loop double");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopDouble(a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		return r.toString();
	}
}
