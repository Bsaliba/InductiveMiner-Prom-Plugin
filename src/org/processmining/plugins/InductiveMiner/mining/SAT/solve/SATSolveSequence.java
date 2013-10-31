package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingle;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingleSequence;

public class SATSolveSequence extends SATSolve {

	public SATSolveSequence(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters,
			ThreadPool pool, AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}

	public void solve() {
		//debug("start SAT search for sequence cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleSequence.class, false);
		
		for (int i = 1; i < directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size(); i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = new SATSolveSingleSequence(directlyFollowsRelation, parameters);
					//SATResult result = solver.solveSingle(j, bestTillNow.get().getProbability());
					SATResult result = solver.solveSingle(j, 0.5);
					if (result != null && result.getProbability() >= bestTillNow.get().getProbability()) {
						if (bestTillNow.maximumAndGet(result)) {
							debug("new maximum " + result);
						}
					}
				}
			});
		}
	}
}
