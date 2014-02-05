package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Matrix;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class DebugProbabilities {
	
	public static String debug(LogInfo logInfo, MiningParameters parameters, boolean useHTML) {
		
		String newLine;
		if (useHTML) {
			newLine = "<br>\n";
		} else {
			newLine = "\n";
		}
		
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = logInfo.getDirectlyFollowsGraph();

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
						m.set(a, b, parameters.getSatProbabilities().getProbabilityXor(logInfo, a, b));
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
						m.set(a, b, parameters.getSatProbabilities().getProbabilitySequence(logInfo, a, b));
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
						m.set(a, b, parameters.getSatProbabilities().getProbabilityParallel(logInfo, a, b));
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
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopSingle(logInfo, a, b));
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
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopIndirect(logInfo, a, b));
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
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopDouble(logInfo, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		return r.toString();
	}
}
