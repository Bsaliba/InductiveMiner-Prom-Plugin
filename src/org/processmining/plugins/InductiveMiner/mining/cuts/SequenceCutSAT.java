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
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class SequenceCutSAT extends SAT {

	public SequenceCutSAT(DirectlyFollowsRelation directlyFollowsRelation, double threshold) {
		super(directlyFollowsRelation, threshold);
	}

	@SuppressWarnings("unchecked")
	public Object[] solve() {
		debug("sequence cut SAT");

		Object[] minCostResult = new Object[] { threshold };
		for (int i = 1; i < 0.5 + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size() - 1; i++) {
			Object[] result = solveSingle(i, (Double) minCostResult[0]);
			if (result != null && (Double) result[0] < (Double) minCostResult[0]) {
				minCostResult = result;
			}
		}
		if (minCostResult.length == 1) {
			return null;
		}
		debug("final optimal solution " + minCostResult[1] + " " + minCostResult[2]);
		Set<Set<XEventClass>> cut = new HashSet<Set<XEventClass>>();
		cut.add((Set<XEventClass>) minCostResult[1]);
		cut.add((Set<XEventClass>) minCostResult[2]);
		return new Object[] { minCostResult[0], cut };
	}

	public Object[] solveSingle(int cutSize, double bestAverageTillNow) {

		debug(" solve optimisation problem with cut size " + cutSize);

		newSolver();

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();

		//edges
		Map<Pair<XEventClass, XEventClass>, Edge> edgeCrossesCut2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> edgeReachabilityViolation2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (XEventClass a : nodes) {
			for (XEventClass b : nodes) {
				if (a != b) {
					Edge edgeCrossesCut = new Edge(varCounter, a, b);
					edgeCrossesCut2var.put(new Pair<XEventClass, XEventClass>(a, b), edgeCrossesCut);
					varInt2var.put(varCounter, edgeCrossesCut);
					varCounter++;

					Edge edgeReachabilityViolation = new Edge(varCounter, a, b);
					edgeReachabilityViolation2var.put(new Pair<XEventClass, XEventClass>(a, b),
							edgeReachabilityViolation);
					varInt2var.put(varCounter, edgeReachabilityViolation);
					varCounter++;
				}
			}
		}

		//initialise reachability
		SequenceCutReachability<XEventClass, DefaultWeightedEdge> reachability = new SequenceCutReachability<XEventClass, DefaultWeightedEdge>(
				graph);

		try {

			//constraint: exactly cutSize nodes on the left side
			{
				int[] clause = new int[countNodes];
				int i = 0;
				for (XEventClass a : graph.vertexSet()) {
					clause[i] = node2var.get(a).getVarInt();
					i++;
				}
				solver.addExactly(new VecInt(clause), cutSize);
			}

			//constraint: no edge crosses cut backwards
			for (DefaultWeightedEdge edge : graph.edgeSet()) {
				XEventClass aI = graph.getEdgeSource(edge);
				XEventClass aJ = graph.getEdgeTarget(edge);
				int C = edgeCrossesCut2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();
				int[] clause = { -C };
				solver.addClause(new VecInt(clause));
			}

			//constraint: edges and nodes consistent
			{
				for (XEventClass aI : nodes) {
					for (XEventClass aJ : nodes) {
						if (aI != aJ) {
							int A = node2var.get(aI).getVarInt();
							int B = node2var.get(aJ).getVarInt();
							int C = edgeCrossesCut2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

							int clause1[] = { -A, B, C };
							int clause2[] = { A, -C };
							int clause3[] = { -B, -C };

							solver.addClause(new VecInt(clause1));
							solver.addClause(new VecInt(clause2));
							solver.addClause(new VecInt(clause3));
						}
					}
				}
			}

			//constraint: reachability violation is recorded
			{
				for (XEventClass aI : nodes) {
					for (XEventClass aJ : nodes) {
						if (aI != aJ) {
							int A = node2var.get(aI).getVarInt();
							int B = node2var.get(aJ).getVarInt();

							int C = edgeReachabilityViolation2var.get(new Pair<XEventClass, XEventClass>(aI, aJ))
									.getVarInt();
							if (!reachability.getReachableFromTo(aI).contains(aJ)) {
								int clause1[] = { A, -B, C };
								int clause2[] = { -A, B, C };
								solver.addClause(new VecInt(clause1));
								solver.addClause(new VecInt(clause2));
							}
						}
					}
				}
			}

			//objective function: least cost for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					clause.push(edgeReachabilityViolation2var.get(new Pair<XEventClass, XEventClass>(aI, aJ))
							.getVarInt());
					coefficients.push(BigInteger.valueOf(1));
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				String x = "";
				String y = "";
				//int cost = 0;
				for (XEventClass a : nodes) {
					for (XEventClass b : nodes) {
						if (a != b) {
							Edge e = edgeCrossesCut2var.get(new Pair<XEventClass, XEventClass>(a, b));
							if (e.isResult()) {
								x += e.toString() + ", ";
								//cost += getCost(graph, aI, aJ).intValue();
							}
							Edge e2 = edgeReachabilityViolation2var.get(new Pair<XEventClass, XEventClass>(a, b));
							if (e2.isResult()) {
								y += e2.toString() + ", ";
								//cost += getCost(graph, aI, aJ).intValue();
							}
						}
					}
				}

				//double averageCost = cost / ((double) numberOfEdgesInCut * (double) 1000);

				debug("   edges crossing cut " + x);
				debug("   non-reachable pairs " + y);
				//debug("   cost " + cost);
				//debug("   edges " + numberOfEdgesInCut);
				//debug("   average cost per edge " + averageCost);

				double averageCost = 0;
				return new Object[] { Double.valueOf(averageCost), result.getLeft(), result.getRight() };
			}
		} catch (TimeoutException e) {
			debug("  timeout");
		} catch (ContradictionException e) {
			debug("  inconsistent problem");
		}
		return null;

	}
}
