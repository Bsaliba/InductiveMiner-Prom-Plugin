package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.concurrent.ExecutionException;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveXor;

public class CutFinderIMin implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		JobList SATPool = minerState.parameters.getSatPool();
		AtomicResult bestSATResult = new AtomicResult(minerState.parameters.getIncompleteThreshold());
		
		(new SATSolveXor(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();
		(new SATSolveSequence(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();
		(new SATSolveParallel(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();
		(new SATSolveLoop(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();
		
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
