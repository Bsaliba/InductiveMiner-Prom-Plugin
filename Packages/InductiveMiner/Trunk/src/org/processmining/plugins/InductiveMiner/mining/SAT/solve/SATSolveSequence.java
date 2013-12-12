package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingleSequence;

public class SATSolveSequence extends SATSolve {

	public SATSolveSequence(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters,
			JobList pool, AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}

	public void solve() {
		//debug("start SAT search for sequence cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleSequence.class, false);
	}
}
