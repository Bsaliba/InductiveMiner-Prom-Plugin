package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingleParallel;

public class SATSolveParallel extends SATSolve {

	public SATSolveParallel(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPool pool, AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}
	
	public void solve() {
		//debug("start SAT search for parallel cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleParallel.class, true);
	}
}
