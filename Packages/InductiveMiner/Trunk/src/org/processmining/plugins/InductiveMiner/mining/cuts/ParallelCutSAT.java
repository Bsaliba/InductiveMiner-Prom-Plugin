package org.processmining.plugins.InductiveMiner.mining.cuts;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class ParallelCutSAT extends SAT {

	private MiningParameters parameters;

	public ParallelCutSAT(DirectlyFollowsRelation directlyFollowsRelation, double threshold, MiningParameters parameters) {
		super(directlyFollowsRelation, threshold);
		this.parameters = parameters;
	}

	public Object[] solve() {
		Object[] minCostResult = new Object[] { Double.MAX_VALUE };
		for (int i = 1; i < 0.5 + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size() / 2; i++) {
			Object[] result = solveSingle(i, (Double) minCostResult[0]);
			if (result != null && (Double) result[0] < (Double) minCostResult[0]) {
				minCostResult = result;
			}
		}
		if (minCostResult.length == 1) {
			return null;
		}
		//debug("final optimal solution " + minCostResult[1] + " " + minCostResult[2]);
		Set<Set<XEventClass>> cut = new HashSet<Set<XEventClass>>();
		cut.add((Set<XEventClass>) minCostResult[1]);
		cut.add((Set<XEventClass>) minCostResult[2]);
		return new Object[] { minCostResult[0], cut };
	}

	public Object[] solveSingle(int cutSize, double bestAverageTillNow) {

		//debug(" solve optimisation problem with cut size " + cutSize);

		newSolver();

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();

		//compute number of edges in the cut
		int numberOfEdgesInCut = (countNodes - cutSize) * cutSize;

		//edges
		Map<Pair<XEventClass, XEventClass>, Edge> edge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = i + 1; j < countNodes; j++) {
				XEventClass aI = nodes[i];
				XEventClass aJ = nodes[j];
				Edge var = new Edge(varCounter, aI, aJ);
				edge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), var);
				varInt2var.put(varCounter, var);
				varCounter++;
			}
		}

		try {
			//constraint: exactly cutSize nodes are cut
			{
				int[] clause = new int[countNodes];
				int i = 0;
				for (XEventClass a : graph.vertexSet()) {
					clause[i] = node2var.get(a).getVarInt();
					i++;
				}
				solver.addAtLeast(new VecInt(clause), cutSize);
				solver.addAtMost(new VecInt(clause), cutSize);
			}

			//constraint: edge is cut iff between two nodes on different sides of the cut
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					int A = node2var.get(aI).getVarInt();
					int B = node2var.get(aJ).getVarInt();
					int C = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

					int clause1[] = { A, B, -C };
					int clause2[] = { A, -B, C };
					int clause3[] = { -A, B, C };
					int clause4[] = { -A, -B, -C };

					solver.addClause(new VecInt(clause1));
					solver.addClause(new VecInt(clause2));
					solver.addClause(new VecInt(clause3));
					solver.addClause(new VecInt(clause4));
				}
			}

			//objective function: least cost for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					clause.push(edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
					coefficients.push(getCost(graph, aI, aJ));
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//constraint: better than best previous run
			BigInteger maxObjectiveFunction = BigInteger
					.valueOf((long) (1000 * bestAverageTillNow * numberOfEdgesInCut));
			//debug("  maximal cost " + maxObjectiveFunction.toString());
			solver.addAtMost(clause, coefficients, maxObjectiveFunction);

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				String x = "";
				int cost = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = i + 1; j < countNodes; j++) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						Edge e = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
						if (e.isResult()) {
							x += e.toString() + ", ";
							cost += getCost(graph, aI, aJ).intValue();
						}
					}
				}

				double averageCost = cost / ((double) numberOfEdgesInCut * (double) 1000);

				//debug("   edges " + x);
				//debug("   cost " + cost);
				//debug("   edges " + numberOfEdgesInCut);
				//debug("   average cost per edge " + averageCost);

				return new Object[] { Double.valueOf(averageCost), result.getLeft(), result.getRight() };
			} else {
				//debug("  no solution");
			}
		} catch (ContradictionException e) {
			//debug("  inconsistent problem");
		} catch (TimeoutException e) {
			//debug("  timeout");
		}

		return null;
	}

	private BigInteger getCost(DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph, XEventClass a,
			XEventClass b) {
		double cost = 0;
		if (parameters.getSatType() == 1) {

			/*
			 * sat type 1:
			 */

			double costAB;
			if (graph.containsEdge(a, b)) {
				costAB = graph.getEdgeWeight(graph.getEdge(a, b));
			} else {
				costAB = 0;
			}

			double costBA;
			if (graph.containsEdge(b, a)) {
				costBA = graph.getEdgeWeight(graph.getEdge(b, a));
			} else {
				costBA = 0;
			}

			if (costAB != 0 || costBA != 0) {
				cost = Math.abs(costAB - costBA) / Math.max(costAB, costBA);
			} else {
				cost = 2;
			}

		} else if (parameters.getSatType() == 2) {
			/*
			 * sat type 2: estimate probability that two nodes are not parallel
			 */
			double wAB;
			if (graph.containsEdge(a, b)) {
				wAB = graph.getEdgeWeight(graph.getEdge(a, b));
			} else {
				wAB = 0;
			}

			double wBA;
			if (graph.containsEdge(b, a)) {
				wBA = graph.getEdgeWeight(graph.getEdge(b, a));
			} else {
				wBA = 0;
			}
			double minW = Math.min(wAB, wBA);
			double maxW = Math.max(wAB, wBA);
			double cMinW = Math.exp(-parameters.getSatParameter() * minW);
			double cMaxW = Math.exp(-parameters.getSatParameter() * maxW);

			cost = (1 / 3.0) * cMaxW + (2 / 3.0) * cMinW;

		}
		return BigInteger.valueOf(Math.round(cost * 1000));
	}
}
