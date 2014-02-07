package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingleXor;

public class SATSolveXor extends SATSolve {

	public SATSolveXor(IMLogInfo logInfo, MiningParameters parameters, JobList pool, AtomicResult result) {
		super(logInfo, parameters, pool, result);
	}
	
	public void solve() {
		//debug("start SAT search for exclusive choice cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleXor.class, true);
	}

}
