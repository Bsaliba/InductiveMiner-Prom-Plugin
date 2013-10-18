package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.math.BigInteger;
import java.util.HashMap;
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

public class SATSolveSingleLoop extends SATSolveSingle {

	public SATSolveSingleLoop(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters) {
		super(directlyFollowsRelation, parameters);
	}

	public SATResult solveSingle(int cutSize, double bestAverageTillNow) {
		//debug(" solve loop with cut size " + cutSize + " and probability " + bestAverageTillNow);

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();
		Probabilities probabilities = parameters.getSatProbabilities();

		//build startA, endA, startB, endB
		Map<XEventClass, Node> startA = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> endA = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> startB = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> endB = new HashMap<XEventClass, Node>();
		for (XEventClass a : nodes) {
			startA.put(a, newNodeVar(a));
			endA.put(a, newNodeVar(a));
			startB.put(a, newNodeVar(a));
			endB.put(a, newNodeVar(a));
		}

		//single and double boundary edges
		Map<Pair<XEventClass, XEventClass>, Edge> boundaryEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> singleBoundaryEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> doubleBoundaryEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = 0; j < countNodes; j++) {
				if (i != j) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					boundaryEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					singleBoundaryEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					if (i < j) {
						doubleBoundaryEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					}

				}
			}
		}

		try {
			//constraint: exactly cutSize ----boundary edges---- are cut
			{
				int[] clause = new int[countNodes * (countNodes - 1)];
				int k = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						if (i != j) {
							XEventClass aI = nodes[i];
							XEventClass aJ = nodes[j];
							clause[k] = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
							k++;
						}
					}
				}
				solver.addExactly(new VecInt(clause), cutSize);
			}

			/*
			 * //constraint: |startA| > 1 { int clause[] = new
			 * int[graph.vertexSet().size()]; for (int i = 0; i < countNodes;
			 * i++) { XEventClass aI = nodes[i]; clause[i] =
			 * node2var.get(aI).getVarInt(); } solver.addClause(new
			 * VecInt(clause)); }
			 */

			//constraint: (endA(a) and startB(b)) or (endB(a) and startA(b)) <=> bedge(a,b)
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int K = endA.get(aI).getVarInt();
						int L = startB.get(aJ).getVarInt();
						int X = endB.get(aI).getVarInt();
						int Y = startA.get(aJ).getVarInt();
						int Z = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

						int clause1[] = { -K, -L, Z };
						int clause2[] = { -X, -Y, Z };
						int clause3[] = { -Z, K, X };
						int clause4[] = { -Z, K, Y };
						int clause5[] = { -Z, L, X };
						int clause6[] = { -Z, L, Y };
						solver.addClause(new VecInt(clause1));
						solver.addClause(new VecInt(clause2));
						solver.addClause(new VecInt(clause3));
						solver.addClause(new VecInt(clause4));
						solver.addClause(new VecInt(clause5));
						solver.addClause(new VecInt(clause6));
					}
				}
			}

			//constraint: (a, b) \in dfg: cut(a) and -cut(b) => endA(a) and startB(b)
			for (DefaultWeightedEdge e : graph.edgeSet()) {
				XEventClass aI = graph.getEdgeSource(e);
				XEventClass aJ = graph.getEdgeTarget(e);
				int A = node2var.get(aI).getVarInt();
				int B = node2var.get(aJ).getVarInt();
				int C = endA.get(aI).getVarInt();
				int D = startB.get(aJ).getVarInt();

				int clause1[] = { -A, B, C };
				int clause2[] = { -A, B, D };
				solver.addClause(new VecInt(clause1));
				solver.addClause(new VecInt(clause2));
			}

			//constraint: (b, a) \in dfg: cut(a) and -cut(b) => startA(a) and endB(b)
			for (DefaultWeightedEdge e : graph.edgeSet()) {
				XEventClass nB = graph.getEdgeSource(e);
				XEventClass nA = graph.getEdgeTarget(e);
				int A = node2var.get(nA).getVarInt();
				int B = node2var.get(nB).getVarInt();
				int C = startA.get(nA).getVarInt();
				int D = endB.get(nB).getVarInt();

				int clause1[] = { -A, B, C };
				int clause2[] = { -A, B, D };
				solver.addClause(new VecInt(clause1));
				solver.addClause(new VecInt(clause2));
			}

			//constraint: startA(a) or endA(a) => cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = startA.get(a).getVarInt();
				int C = endA.get(a).getVarInt();
				int clause1[] = { A, -B };
				int clause2[] = { A, -C };
				solver.addClause(new VecInt(clause1));
				solver.addClause(new VecInt(clause2));
			}

			//constraint: startB(a) or endB(a) => -cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = startB.get(a).getVarInt();
				int C = endB.get(a).getVarInt();
				int clause1[] = { -A, -B };
				int clause2[] = { -A, -C };
				solver.addClause(new VecInt(clause1));
				solver.addClause(new VecInt(clause2));
			}

			//constraint: start(a): startA(a) or startB(a)
			for (XEventClass a : directlyFollowsRelation.getStartActivities()) {
				int A = startA.get(a).getVarInt();
				/*
				 * int B = startB.get(a).getVarInt(); int clause1[] = { A, B };
				 * solver.addClause(new VecInt(clause1));
				 */
				//for now: start(a) => startA(a)
				int clause1[] = { A };
				solver.addClause(new VecInt(clause1));
			}

			//constraint: end(a): endA(a) or endB(a)
			for (XEventClass a : directlyFollowsRelation.getEndActivities()) {
				int A = endA.get(a).getVarInt();
				/*
				 * int B = endB.get(a).getVarInt(); int clause1[] = { A, B };
				 * solver.addClause(new VecInt(clause1));
				 */
				//for now: end(a) => endA(a)
				int clause1[] = { A };
				solver.addClause(new VecInt(clause1));
			}

			//constraint: single edges are recorded
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int A = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int B = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();
						int C = singleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int clause1[] = { -A, B, C };
						int clause2[] = { A, -C };
						int clause3[] = { -B, -C };
						solver.addClause(new VecInt(clause1));
						solver.addClause(new VecInt(clause2));
						solver.addClause(new VecInt(clause3));
					}
				}
			}

			//constraint: double edges are recorded
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					int A = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
					int B = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();
					int C = doubleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
					int clause1[] = { -A, -B, C };
					int clause2[] = { A, -C };
					int clause3[] = { B, -C };
					solver.addClause(new VecInt(clause1));
					solver.addClause(new VecInt(clause2));
					solver.addClause(new VecInt(clause3));
				}
			}

			//objective function: highest probabilities for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						//single, treat as sequence
						clause.push(singleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
						BigInteger pab = probabilities.getProbabilityLoopSingleB(aI, aJ);
						//BigInteger pba = probabilities.getProbabilityLoopSingleB(aJ, aI);
						//coefficients.push(pab.subtract(pba).negate());
						coefficients.push(pab.negate());

						//double, treat as parallel
						if (i < j) {
							clause.push(doubleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ))
									.getVarInt());
							coefficients.push(probabilities.getProbabilityLoopDoubleB(aI, aJ)
									.multiply(BigInteger.valueOf(2)).negate());
						}
					}
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//constraint: better than best previous run
			BigInteger minObjectiveFunction = BigInteger.valueOf((long) (probabilities.doubleToIntFactor
					* bestAverageTillNow * cutSize));
			solver.addAtMost(clause, coefficients, minObjectiveFunction.negate());

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				String x = "";
				String ses = "";
				String des = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						if (i != j) {
							XEventClass aI = nodes[i];
							XEventClass aJ = nodes[j];
							Edge e = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (e.isResult()) {
								x += e.toString() + ", ";
							}

							//single edge
							Edge se = singleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (se.isResult()) {
								ses += se.toString() + " (" + probabilities.getProbabilityLoop(aI, aJ) + "), ";
								double pab = probabilities.getProbabilityLoopSingle(aI, aJ);
								//double pba = probabilities.getProbabilityLoopSingle(aJ, aI);
								//sumProbability += pab - pba;
								
								sumProbability += pab;
							}

							//double edge
							if (i < j) {
								Edge de = doubleBoundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
								if (de.isResult()) {
									des += de.toString() + " (" + probabilities.getProbabilityLoopDouble(aI, aJ)
											+ "), ";
									sumProbability += probabilities.getProbabilityLoopDouble(aI, aJ) * 2;
								}
							}
						}
					}
				}

				double averageProbability = sumProbability / cutSize;
				SATResult result2 = new SATResult(result.getLeft(), result.getRight(), averageProbability, "loop");

				//debug
				String sa = "";
				String ea = "";
				String sb = "";
				String eb = "";
				for (XEventClass e : graph.vertexSet()) {
					if (startA.get(e).isResult()) {
						sa += e.toString() + ", ";
					}
					if (endA.get(e).isResult()) {
						ea += e.toString() + ", ";
					}
					if (startB.get(e).isResult()) {
						sb += e.toString() + ", ";
					}
					if (endB.get(e).isResult()) {
						eb += e.toString() + ", ";
					}
				}

				//debug("  " + result2.toString());
				//debug("   boundary edges " + x);
				//debug("   single boundary edges " + ses);
				//debug("   double boundary edges " + des);
				//debug("   start A " + sa);
				//debug("   end A " + ea);
				//debug("   start B " + sb);
				//debug("   end B " + eb);
				//debug("   sum probability " + sumProbability);

				return result2;
			} else {
				//debug("  no solution");
			}
		} catch (TimeoutException e) {
			//debug("  timeout");
		} catch (ContradictionException e) {
			//debug("  inconsistent problem");
		}
		return null;
	}

}
