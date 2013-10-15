package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Matrix;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class DebugProbabilities {
	public static String debug(DirectlyFollowsRelation relation, MiningParameters parameters) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		parameters.getSatProbabilities().setDirectlyFollowsRelation(relation);

		if (graph.vertexSet().size() == 1) {
			return "";
		}

		StringBuilder r = new StringBuilder();
		{
			r.append("xor\n");
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityXor(a, b));
					}
				}
			}
			r.append(m.toString());
			r.append("\n");
		}

		{
			r.append("sequence\n");
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilitySequence(a, b));
					}
				}
			}
			r.append(m.toString());
			r.append("\n");
		}
		
		{
			r.append("loop single\n");
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopSingle(a, b));
					}
				}
			}
			r.append(m.toString());
			r.append("\n");
		}

		{
			r.append("parallel\n");
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityParallel(a, b));
					}
				}
			}
			r.append(m.toString());
			r.append("\n");
		}

		{
			r.append("loop double\n");
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.vertexSet(), false);
			for (XEventClass a : graph.vertexSet()) {
				for (XEventClass b : graph.vertexSet()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopDouble(a, b));
					}
				}
			}
			r.append(m.toString());
			r.append("\n");
		}

		return r.toString();
	}
}
