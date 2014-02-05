package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.concurrent.ExecutionException;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveXor;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public class CutFinderIMin implements CutFinder {

	public Cut findCut(IMLog log, LogInfo logInfo, MiningParameters parameters) {
		JobList SATPool = parameters.getSatPool();
		AtomicResult bestSATResult = new AtomicResult(parameters.getIncompleteThreshold());
		
		(new SATSolveXor(logInfo, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveSequence(logInfo, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveParallel(logInfo, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveLoop(logInfo, parameters, SATPool, bestSATResult)).solve();
		
		try {
			SATPool.join();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		SATResult satResult = bestSATResult.get();
		
		return satResult.getCut();
	}

}
