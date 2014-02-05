package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingleParallel;

public class SATSolveParallel extends SATSolve {

	public SATSolveParallel(LogInfo logInfo, MiningParameters parameters, JobList pool, AtomicResult result) {
		super(logInfo, parameters, pool, result);
	}
	
	public void solve() {
		//debug("start SAT search for parallel cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleParallel.class, true);
	}
}
