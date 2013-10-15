package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class SATSolveXor extends SATSolve {

	public SATSolveXor(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPool pool, AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}
	
	public void solve() {
		//debug("start SAT search for exclusive choice cut likelier than " + bestTillNow.get().getProbability());
		
		for (int i = 1; i < 0.5 + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size() / 2 && bestTillNow.get().getProbability() < 1; i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = new SATSolveSingleXor(directlyFollowsRelation, parameters);
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
