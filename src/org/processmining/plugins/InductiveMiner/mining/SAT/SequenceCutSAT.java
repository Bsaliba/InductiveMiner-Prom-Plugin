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

public class SequenceCutSAT extends SAT {

	public SequenceCutSAT(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters) {
		super(directlyFollowsRelation, parameters);
	}

	public Result solve(Result mostProbableResult) {
		debug("start SAT search for sequence cut likelier than " + mostProbableResult.probability);
		debug(Math.pow(0.5 * directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size(), 2) + " rounds");
		for (int c = 1; c <= Math.pow(0.5 * directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size(), 2)
				&& mostProbableResult.probability < 1; c++) {
			Result result = solveSingle(c, mostProbableResult.probability);
			if (result != null && result.probability >= mostProbableResult.probability) {
				mostProbableResult = result;
			}
		}
		//debug("final optimal solution " + mostProbableResult);
		return mostProbableResult;
	}

	public Result solveSingle(int cutSize, double bestAverageTillNow) {

		//debug(" solve optimisation problem with cut size " + cutSize);

		newSolver();

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsRelation
				.getDirectlyFollowsGraph();
		Probabilities probabilities = parameters.getSatProbabilities();

		//local start and end activities
		Map<XEventClass, Node> nodeIsBoundaryLeft = new HashMap<XEventClass, Node>();
		Map<XEventClass, Node> nodeIsBoundaryRight = new HashMap<XEventClass, Node>();
		for (XEventClass a : nodes) {
			Node n1 = new Node(varCounter, a);
			nodeIsBoundaryLeft.put(a, n1);
			varInt2var.put(varCounter, n1);
			varCounter++;

			Node n2 = new Node(varCounter, a);
			nodeIsBoundaryRight.put(a, n2);
			varInt2var.put(varCounter, n2);
			varCounter++;
		}

		//boundary and violating edges
		Map<Pair<XEventClass, XEventClass>, Edge> boundaryEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> violatingEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = 0; j < countNodes; j++) {
				if (i != j) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					Edge var = new Edge(varCounter, aI, aJ);
					boundaryEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), var);
					varInt2var.put(varCounter, var);
					varCounter++;

					violatingEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdge(aI, aJ));
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

			//constraint: bl(a) and br(b) <=> bedge(a,b)
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int A = nodeIsBoundaryLeft.get(aI).getVarInt();
						int B = nodeIsBoundaryRight.get(aJ).getVarInt();
						int C = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

						int clause1[] = { -A, -B, C };
						int clause2[] = { A, -C };
						int clause3[] = { B, -C };
						solver.addClause(new VecInt(clause1));
						solver.addClause(new VecInt(clause2));
						solver.addClause(new VecInt(clause3));
					}
				}
			}

			//constraint: (a, b) \in dfg and -cut(a) and cut(b) => bl(a) and br(b)
			for (DefaultWeightedEdge e : graph.edgeSet()) {
				XEventClass aI = graph.getEdgeSource(e);
				XEventClass aJ = graph.getEdgeTarget(e);
				int A = node2var.get(aI).getVarInt();
				int B = node2var.get(aJ).getVarInt();
				int C = nodeIsBoundaryLeft.get(aI).getVarInt();
				int D = nodeIsBoundaryRight.get(aJ).getVarInt();

				int clause1[] = { -A, B, C };
				int clause2[] = { -A, B, D };
				solver.addClause(new VecInt(clause1));
				solver.addClause(new VecInt(clause2));
			}

			//constraint: cut(a) and -cut(b) <=> violating(b, a)
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						int A = node2var.get(aI).getVarInt();
						int B = node2var.get(aJ).getVarInt();
						int C = violatingEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();

						int clause1[] = { -A, B, C };
						int clause2[] = { A, -C };
						int clause3[] = { -B, -C };
						solver.addClause(new VecInt(clause1));
						solver.addClause(new VecInt(clause2));
						solver.addClause(new VecInt(clause3));
					}
				}
			}

			//constraint: bl(a) => cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = nodeIsBoundaryLeft.get(a).getVarInt();
				int clause1[] = { A, -B };
				solver.addClause(new VecInt(clause1));
			}

			//constraint: br(a) => -cut(a)
			for (XEventClass a : graph.vertexSet()) {
				int A = node2var.get(a).getVarInt();
				int B = nodeIsBoundaryRight.get(a).getVarInt();
				int clause1[] = { -A, -B };
				solver.addClause(new VecInt(clause1));
			}

			//constraint: start(a) and -cut(a) => br(a)
			for (XEventClass a : directlyFollowsRelation.getStartActivities()) {
				int A = node2var.get(a).getVarInt();
				int B = nodeIsBoundaryRight.get(a).getVarInt();
				int clause1[] = { A, B };
				solver.addClause(new VecInt(clause1));
			}

			//constraint: end(a) and cut(a) => bl(a)
			for (XEventClass a : directlyFollowsRelation.getEndActivities()) {
				int A = node2var.get(a).getVarInt();
				int B = nodeIsBoundaryLeft.get(a).getVarInt();
				int clause1[] = { -A, B };
				solver.addClause(new VecInt(clause1));
			}

			//objective function: highest probabilities for edges, lowest for violating edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					if (i != j) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						BigInteger probability = probabilities.getProbabilitySequenceB(directlyFollowsRelation, aI, aJ);

						clause.push(boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
						coefficients.push(probability.negate());

						clause.push(violatingEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
						coefficients.push(probability);
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
				String ves = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						if (i != j) {
							XEventClass aI = nodes[i];
							XEventClass aJ = nodes[j];
							double probability = probabilities.getProbabilitySequence(directlyFollowsRelation, aI, aJ);
							Edge e = boundaryEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (e.isResult()) {
								x += e.toString() + " (" + probability + "), ";
								sumProbability += probability;
							}

							Edge ve = violatingEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (ve.isResult()) {
								ves += e.toString() + " (" + probability + "), ";
								sumProbability -= probability;
							}
						}
					}
				}

				//debug
				String bl = "";
				String br = "";
				for (XEventClass e : graph.vertexSet()) {
					Node n = nodeIsBoundaryLeft.get(e);
					if (n.isResult()) {
						bl += e.toString() + ", ";
					}

					Node m = nodeIsBoundaryRight.get(e);
					if (m.isResult()) {
						br += e.toString() + ", ";
					}
				}

				double averageProbability = sumProbability / cutSize;
				Result result2 = new Result(result.getLeft(), result.getRight(), averageProbability, "sequence");

				debug("  " + result2.toString());
				debug("   minimal sum probability " + minObjectiveFunction.toString());
				debug("   boundary edges " + x);
				debug("   boundary left " + bl);
				debug("   boundary right " + br);
				debug("   violating edges " + ves);
				debug("   sum probability " + sumProbability);

				return result2;
			} else {
				//debug("  no solution");
			}
		} catch (TimeoutException e) {
			debug("  timeout");
		} catch (ContradictionException e) {
			//debug("  inconsistent problem");
		}
		return null;
	}
}
