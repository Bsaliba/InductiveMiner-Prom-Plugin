package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.SATResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingle;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingleLoop;

public class SATSolveLoop extends SATSolve {

	public SATSolveLoop(LogInfo logInfo, MiningParameters parameters, JobList pool,
			AtomicResult result) {
		super(logInfo, parameters, pool, result);
	}

	public void solve() {
		//debug("start SAT search for loop cut likelier than " + bestTillNow.get().getProbability());
		
		for (int i = 1; i < logInfo.getActivities().size(); i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = new SATSolveSingleLoop(logInfo, parameters);
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
