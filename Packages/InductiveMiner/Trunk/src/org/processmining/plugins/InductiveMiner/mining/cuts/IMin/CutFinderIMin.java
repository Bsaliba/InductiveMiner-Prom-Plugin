package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.packages.PackageManager.Canceller;
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

	public Cut findCut(Dfg dfg, DfgMinerState minerState, Canceller canceller) {
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
		return findCut(info, threshold, canceller);
	}

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState, Canceller canceller) {
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
		return findCut(info, threshold, canceller);
	}

	public static Cut findCut(CutFinderIMinInfo info, float threshold, Canceller canceller) {
		AtomicResult bestSATResult = new AtomicResult(threshold);
		(new SATSolveXor(info, bestSATResult, canceller)).solve();
		(new SATSolveParallel(info, bestSATResult, canceller)).solve();

		(new SATSolveSequence(info, bestSATResult, canceller)).solve();

		(new SATSolveLoop(info, bestSATResult, canceller)).solve();

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
