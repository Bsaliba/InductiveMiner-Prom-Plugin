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

		/*
		 * long start1 = (new Date()).getTime(); AtomicResult bestSATResult1 =
		 * new AtomicResult(minerState.parameters.getIncompleteThreshold()); new
		 * SATSolveXor2().solveAll(logInfo, 1, bestSATResult1, minerState); try
		 * { SATPool.join(); } catch (ExecutionException e) {
		 * e.printStackTrace(); return null; } long end1 = (new
		 * Date()).getTime() - start1;
		 */

		//long start2 = (new Date()).getTime();
		AtomicResult bestSATResult = new AtomicResult(minerState.parameters.getIncompleteThreshold());
		(new SATSolveXor(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();
		(new SATSolveParallel(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();

		(new SATSolveSequence(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();

		(new SATSolveLoop(logInfo, minerState.parameters, SATPool, bestSATResult)).solve();

		try {
			SATPool.join();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}

		//long end2 = (new Date()).getTime() - start2;

		//System.out.println("yices " + end1 + ", sat4j " + end2);

		SATResult satResult = bestSATResult.get();

		return satResult.getCut();
	}

}
