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
		debug(" solve loop with cut size " + cutSize + " and probability " + bestAverageTillNow);

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();
		Probabilities probabilities = parameters.getSatProbabilities();

		//compute number of edges in the cut
		int numberOfEdgesInCut = (countNodes - cutSize) * cutSize * 2;

		//initialise startA, endA, startB, endB
		Map<XEventClass, Node> startBody = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> endBody = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> startRedo = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> endRedo = new HashMap<XEventClass, Node>();
		for (XEventClass a : nodes) {
			startBody.put(a, newNodeVar(a));
			endBody.put(a, newNodeVar(a));
			startRedo.put(a, newNodeVar(a));
			endRedo.put(a, newNodeVar(a));
		}

		//edges
		Map<Pair<XEventClass, XEventClass>, Edge> singleLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> reverseSingleLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> doubleLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> indirectLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> internalEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (XEventClass aI : nodes) {
			for (XEventClass aJ : nodes) {
				if (aI != aJ) {
					singleLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					reverseSingleLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					doubleLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					indirectLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
					internalEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));

				}
			}
		}

		try {
			//constraint: exactly cutSize nodes are cut
			{
				int[] clause = new int[countNodes];
				int k = 0;
				for (int i = 0; i < countNodes; i++) {
					XEventClass aI = nodes[i];
					clause[k] = node2var.get(aI).getVarInt();
					k++;
				}
				solver.addExactly(new VecInt(clause), cutSize);
			}

			//constraint: each edge is in exactly one category
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int S = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int R = reverseSingleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int D = doubleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int T = internalEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int N = indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int[] clause = { S, D, T, N, R };
						solver.addExactly(new VecInt(clause), 1);
					}
				}
			}

			//constraint: internal(a, b) <=> (cut(a) <=> cut(b)) 
			for (XEventClass aI : nodes) {
				for (XEventClass aJ : nodes) {
					if (aI != aJ) {
						int A = internalEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

						int B = node2var.get(aI).getVarInt();
						int C = node2var.get(aJ).getVarInt();

						addClause(-A, -B, C);
						addClause(-A, B, -C);

						addClause(A, B, C);
						addClause(A, -B, -C);
					}
				}
			}

			//constraint: reverse(b,a) <=> single(a,b)
			for (XEventClass aI : nodes) {
				for (XEventClass aJ : nodes) {
					if (aI != aJ) {
						int S = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
						int R = reverseSingleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();

						addClause(-S, R);
						addClause(-R, S);
					}
				}
			}

			//constraint: single(a, b) <=> (endBody(a) and startRedo(b)) xor (endRedo(a) and startBody(b)) 
			for (XEventClass a : nodes) {
				for (XEventClass b : nodes) {
					if (a != b) {
						int A = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(a, b)).getVarInt();
						
						int B = endBody.get(a).getVarInt();
						int C = startRedo.get(b).getVarInt();
						int D = endRedo.get(a).getVarInt();
						int E = startBody.get(b).getVarInt();
						
						addClause(-A, -B, -C, -D, -E);
						addClause(-A, D, B);
						addClause(-A, D, C);
						addClause(-A, E, B);
						addClause(-A, E, C);
						
						addClause(B, -D, -E, A);
						addClause(C, -D, -E, A);
						addClause(D, -B, -C, A);
						addClause(E, -B, -C, A);
					}
				}
			}

			//constraint: double(a, b) <=> (endBody(a) and startBody(a) and endRedo(b) and startRedo(b)) or (endBody(b) and startBody(b) and endRedo(a) and startRedo(a))
			for (XEventClass aI : nodes) {
				for (XEventClass aJ : nodes) {
					if (aI != aJ) {
						
					}
				}
			}

			//constraint: |startBody|, |endBody|, |startRedo|, |endRedo| > 0
			{
				int[] clauseStartBody = new int[nodes.length];
				int[] clauseEndBody = new int[nodes.length];
				int[] clauseStartRedo = new int[nodes.length];
				int[] clauseEndRedo = new int[nodes.length];
				for (int i = 0; i < nodes.length; i++) {
					clauseStartBody[i] = startBody.get(nodes[i]).getVarInt();
					clauseEndBody[i] = endBody.get(nodes[i]).getVarInt();
					clauseStartRedo[i] = startRedo.get(nodes[i]).getVarInt();
					clauseEndRedo[i] = endRedo.get(nodes[i]).getVarInt();
				}
				solver.addAtLeast(new VecInt(clauseStartBody), 1);
				solver.addAtLeast(new VecInt(clauseEndBody), 1);
				solver.addAtLeast(new VecInt(clauseStartRedo), 1);
				solver.addAtLeast(new VecInt(clauseEndRedo), 1);
			}

			//constraint: startBody(a) or endBody(a) => cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = startBody.get(a).getVarInt();
				int C = endBody.get(a).getVarInt();

				addClause(A, -B);
				addClause(A, -C);
			}

			//constraint: startB(a) or endB(a) => -cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = startRedo.get(a).getVarInt();
				int C = endRedo.get(a).getVarInt();

				addClause(-A, -B);
				addClause(-A, -C);
			}
			
			//constraint: Start(a) <=> startBody(a)
			for (XEventClass a : nodes) {
				int A = startBody.get(a).getVarInt();
				if (directlyFollowsRelation.getStartActivities().contains(a)) {
					addClause(A);
				} else {
					addClause(-A);
				}
			}
			
			//constraint: if End(a) then endBody(a)
			for (XEventClass a : nodes) {
				int A = endBody.get(a).getVarInt();
				if (directlyFollowsRelation.getEndActivities().contains(a)) {
					addClause(A);
				} else {
					addClause(-A);
				}
			}

			//objective function: highest probabilities for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (XEventClass aI : nodes) {
				for (XEventClass aJ : nodes) {
					if (aI != aJ) {
						//direct
						clause.push(singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
						BigInteger pab = probabilities.getProbabilityLoopSingleB(aI, aJ);
						coefficients.push(pab.multiply(BigInteger.valueOf(2)).negate());

						//indirect
						clause.push(indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
						BigInteger ind = probabilities.getProbabilityLoopIndirectB(aI, aJ);
						coefficients.push(ind.negate());
					}
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//constraint: better than best previous run
			BigInteger minObjectiveFunction = BigInteger.valueOf((long) (probabilities.doubleToIntFactor
					* bestAverageTillNow * numberOfEdgesInCut));
			solver.addAtMost(clause, coefficients, minObjectiveFunction.negate());

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				String single = "";
				String reverse = "";
				String doublee = "";
				String indirect = "";
				String internal = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						if (i != j) {
							XEventClass aI = nodes[i];
							XEventClass aJ = nodes[j];

							//single edge
							{
								Edge e = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
								if (e.isResult()) {
									double p = probabilities.getProbabilityLoopSingle(aI, aJ);
									single += e.toString() + " (" + p + "), ";
									sumProbability += p * 2;
								}
							}

							//reverse single edge
							{
								Edge e = reverseSingleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
								if (e.isResult()) {
									reverse += e.toString() + ", ";
								}
							}

							//double edge
							{
								Edge e = doubleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
								if (e.isResult()) {
									doublee += e.toString() + ", ";
								}
							}

							//internal edge
							{
								Edge e = internalEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
								if (e.isResult()) {
									internal += e.toString() + ", ";
								}
							}

							//indirect edge
							Edge se = indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (se.isResult()) {
								double p = probabilities.getProbabilityLoopIndirect(aI, aJ);
								indirect += se.toString() + " (" + p + "), ";
								sumProbability += p;
							}

						}
					}
				}

				double averageProbability = sumProbability / numberOfEdgesInCut;
				SATResult result2 = new SATResult(result.getLeft(), result.getRight(), averageProbability, "loop");

				//debug
				String sa = "";
				String ea = "";
				String sb = "";
				String eb = "";
				for (XEventClass e : graph.vertexSet()) {
					if (startBody.get(e).isResult()) {
						sa += e.toString() + ", ";
					}
					if (endBody.get(e).isResult()) {
						ea += e.toString() + ", ";
					}
					if (startRedo.get(e).isResult()) {
						sb += e.toString() + ", ";
					}
					if (endRedo.get(e).isResult()) {
						eb += e.toString() + ", ";
					}
				}

				debug("  " + result2.toString());
				debug("   single edges " + single);
				debug("   double edges " + doublee);
				debug("   indirect edges " + indirect);
				debug("   internal edges " + internal);
				debug("   reverse edges " + reverse);
				debug("   start body " + sa);
				debug("   end body " + ea);
				debug("   start redo " + sb);
				debug("   end redo " + eb);
				debug("   sum probability " + sumProbability);

				return result2;
			} else {
				debug("  no solution");
			}
		} catch (TimeoutException e) {
			//debug("  timeout");
		} catch (ContradictionException e) {
			debug("  inconsistent problem");
		}
		return null;
	}

}
