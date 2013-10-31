package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingle;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingleLoop;

public class SATSolveLoop extends SATSolve {

	public SATSolveLoop(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPool pool,
			AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}

	public void solve() {
		//debug("start SAT search for loop cut likelier than " + bestTillNow.get().getProbability());
		
		for (int i = 1; i < directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size(); i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = new SATSolveSingleLoop(directlyFollowsRelation, parameters);
					SATResult result = solver.solveSingle(j, bestTillNow.get().getProbability());
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
