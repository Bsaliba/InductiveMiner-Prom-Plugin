package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.jobList.JobListConcurrent;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.SATSolveXor;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMin implements CutFinder, DfgCutFinder {

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		float threshold = minerState.getParameters().getIncompleteThreshold();
		JobList jobList = new JobListConcurrent(minerState.getParameters().getSatPool());
		
		MultiSet<XEventClass> startActivities = dfg.getStartActivities();
		MultiSet<XEventClass> endActivities = dfg.getEndActivities();
		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();
		Graph<XEventClass> transitiveGraph = TransitiveClosure.transitiveClosure(XEventClass.class, graph);
		Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = null;
		Probabilities satProbabilities = minerState.getParameters().getSatProbabilities();
		boolean debug = minerState.getParameters().isDebug();
		CutFinderIMinInfo info = new CutFinderIMinInfo(startActivities, endActivities, graph, transitiveGraph,
				minimumSelfDistancesBetween, satProbabilities, jobList, debug);
		return findCut(info, threshold);
	}

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		float threshold = minerState.parameters.getIncompleteThreshold();
		JobList jobList = new JobListConcurrent(minerState.parameters.getSatPool());

		MultiSet<XEventClass> startActivities = logInfo.getStartActivities();
		MultiSet<XEventClass> endActivities = logInfo.getEndActivities();
		Graph<XEventClass> graph = logInfo.getDirectlyFollowsGraph();
		Graph<XEventClass> transitiveGraph = TransitiveClosure.transitiveClosure(XEventClass.class, graph);
		Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = logInfo
				.getMinimumSelfDistancesBetween();
		Probabilities satProbabilities = minerState.parameters.getSatProbabilities();
		boolean debug = minerState.parameters.isDebug();
		CutFinderIMinInfo info = new CutFinderIMinInfo(startActivities, endActivities, graph, transitiveGraph,
				minimumSelfDistancesBetween, satProbabilities, jobList, debug);
		return findCut(info, threshold);
	}

	public static Cut findCut(CutFinderIMinInfo info, float threshold) {
		/*
		 * long start1 = (new Date()).getTime(); AtomicResult bestSATResult1 =
		 * new AtomicResult(minerState.parameters.getIncompleteThreshold()); new
		 * SATSolveXor2().solveAll(logInfo, 1, bestSATResult1, minerState); try
		 * { SATPool.join(); } catch (ExecutionException e) {
		 * e.printStackTrace(); return null; } long end1 = (new
		 * Date()).getTime() - start1;
		 */

		//long start2 = (new Date()).getTime();
		AtomicResult bestSATResult = new AtomicResult(threshold);
		(new SATSolveXor(info, bestSATResult)).solve();
		(new SATSolveParallel(info, bestSATResult)).solve();

		(new SATSolveSequence(info, bestSATResult)).solve();

		(new SATSolveLoop(info, bestSATResult)).solve();

		try {
			info.getJobList().join();
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
