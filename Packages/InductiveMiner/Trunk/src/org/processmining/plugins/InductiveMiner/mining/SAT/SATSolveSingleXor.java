package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class SATSolveSingleXor extends SATSolveSingle {

	private class Compare implements Comparator<Triple<Integer, Integer, BigInteger>> {
		public int compare(Triple<Integer, Integer, BigInteger> arg0, Triple<Integer, Integer, BigInteger> arg1) {
			return arg1.getC().compareTo(arg0.getC());
		}
	}

	public SATSolveSingleXor(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters) {
		super(directlyFollowsRelation, parameters);
	}

	public SATResult solveSingle(int cutSize, double bestAverageTillNow) {
		//debug(" solve xor with cut size " + cutSize + " and probability " + bestAverageTillNow);

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();
		Probabilities probabilities = parameters.getSatProbabilities();

		//compute number of edges in the cut
		int numberOfEdgesInCut = (countNodes - cutSize) * cutSize;

		//edges
		HashMap<Pair<XEventClass, XEventClass>, Edge> edge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		HashMap<Pair<XEventClass, XEventClass>, Edge> maximumBoundaryEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = i + 1; j < countNodes; j++) {
				XEventClass aI = nodes[i];
				XEventClass aJ = nodes[j];

				edge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));

				//maximal boundary edge
				maximumBoundaryEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
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

			//constraint: maximumBoundaryEdge(a) => boundary(a,b)
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					int A = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
					int B = maximumBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

					int clause1[] = { A, -B };
					solver.addClause(new VecInt(clause1));
				}
			}

			//constraint: only one maximumBoundaryEdge
			{
				List<Triple<Integer, Integer, BigInteger>> list = new ArrayList<Triple<Integer, Integer, BigInteger>>();
				for (int i = 0; i < countNodes; i++) {
					for (int j = i + 1; j < countNodes; j++) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int e = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int mbe = maximumBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						list.add(new Triple<Integer, Integer, BigInteger>(e, mbe, probabilities.getProbabilityXorB(aI,
								aJ)));
					}
				}
				Collections.sort(list, new Compare());
				int clause[] = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					Triple<Integer, Integer, BigInteger> p1 = list.get(i);
					for (int j = 0; j < i; j++) {
						Triple<Integer, Integer, BigInteger> p2 = list.get(j);
						int clause1[] = { -p1.getA(), -p2.getB() };
						solver.addClause(new VecInt(clause1));
					}
					clause[i] = p1.getB();
				}
				solver.addExactly(new VecInt(clause), 1);
			}

			//objective function: maximum boundary edge
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					clause.push(maximumBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
					coefficients.push(probabilities.getProbabilityXorB(aI, aJ).negate());
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//constraint: better than best previous run
			BigInteger minObjectiveFunction = BigInteger
					.valueOf((long) (probabilities.doubleToIntFactor * bestAverageTillNow));
			solver.addAtMost(clause, coefficients, minObjectiveFunction.negate());

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				String x = "";
				String mbes = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = i + 1; j < countNodes; j++) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						Edge e = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
						if (e.isResult()) {
							x += e.toString() + " (" + probabilities.getProbabilityXor(aI, aJ) + "), ";
						}

						Edge mbe = maximumBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
						if (mbe.isResult()) {
							mbes += e.toString() + " (" + probabilities.getProbabilityXor(aI, aJ) + "), ";
							sumProbability += probabilities.getProbabilityXor(aI, aJ);
						}
					}
				}

				double averageProbability = sumProbability;
				SATResult result2 = new SATResult(result.getLeft(), result.getRight(), averageProbability, "xor");

				//debug("  " + result2.toString());
				//debug("   edges " + x);
				//debug("   maximum boundary edges " + mbes);
				//debug("   sum probability " + sumProbability);

				return result2;
			} else {
				//debug("  no solution");
			}
		} catch (ContradictionException e) {
			//debug("  inconsistent problem " + e);
		} catch (TimeoutException e1) {
			//debug("  time out " + e);
		}
		return null;
	}

}
