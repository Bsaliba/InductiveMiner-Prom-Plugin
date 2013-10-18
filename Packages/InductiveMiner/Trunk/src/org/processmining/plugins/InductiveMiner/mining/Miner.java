package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.DebugProbabilities;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATSolveLoop;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATSolveParallel;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATSolveSequence;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATSolveXor;
import org.processmining.plugins.InductiveMiner.mining.cuts.ExclusiveChoiceCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.LoopCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.ParallelCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.SequenceCut;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.FilterResults;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.Exhaustive;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessor;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorMatrix;
import org.processmining.plugins.InductiveMiner.model.Binoperator;
import org.processmining.plugins.InductiveMiner.model.EventClass;
import org.processmining.plugins.InductiveMiner.model.ExclusiveChoice;
import org.processmining.plugins.InductiveMiner.model.Loop;
import org.processmining.plugins.InductiveMiner.model.Node;
import org.processmining.plugins.InductiveMiner.model.Parallel;
import org.processmining.plugins.InductiveMiner.model.ProcessTreeModel;
import org.processmining.plugins.InductiveMiner.model.Sequence;
import org.processmining.plugins.InductiveMiner.model.Tau;
import org.processmining.plugins.InductiveMiner.model.conversion.ProcessTreeModel2PetriNet;
import org.processmining.plugins.InductiveMiner.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class Miner {

	/*
	 * basic usage:
	 * 
	 * Process tree - call mine, will return a ProcessTreeModel object (=
	 * internal process tree representation) - call
	 * ProcessTreeModel2ProcessTree.convert(model.root); to obtain a ProcessTree
	 */

	public Object[] mineParametersPetrinetWithoutConnections(PluginContext context, XLog log,
			MiningParameters parameters) {
		ProcessTreeModel model = mine(context, log, parameters);
		WorkflowNet workflowNet = ProcessTreeModel2PetriNet.convert(model.root);

		//create mapping
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping = new TransEvClassMapping(parameters.getClassifier(), dummy);
		for (Pair<Transition, XEventClass> p : workflowNet.transition2eventClass) {
			mapping.put(p.getFirst(), p.getSecond());
			activities.add(p.getSecond());
		}

		return new Object[] { model, workflowNet, mapping, activities };
	}

	private int recursionStepsCounter;
	private final MultiSet<XEventClass> noiseEvents = new MultiSet<XEventClass>();
	private final AtomicInteger noiseEmptyTraces = new AtomicInteger();

	public ProcessTreeModel mine(PluginContext context, XLog log, MiningParameters parameters) {
		//prepare the log
		//debug("Start conversion to internal log format");
		Filteredlog filteredLog = new Filteredlog(log, parameters);

		//debug initial log
		//debug(filteredLog.toString());
		debug("\n\nStart mining", parameters);
		recursionStepsCounter = -1;

		//create the model
		ProcessTreeModel model = new ProcessTreeModel();

		//initialise the thread pool
		ThreadPool pool = new ThreadPool(1);
		noiseEmptyTraces.set(0);
		noiseEvents.empty();

		//add a dummy node and mine
		Binoperator dummyRootNode = new Sequence(1);
		mineProcessTree(filteredLog, parameters, dummyRootNode, 0, pool);

		//wait for all jobs to terminate
		try {
			pool.join();
		} catch (ExecutionException e) {
			//debug("something failed");
			e.printStackTrace();
			model.root = null;
		}

		//output noise statistics
		double fitness = 1 - (noiseEvents.size() + noiseEmptyTraces.get())
				/ (filteredLog.getNumberOfEvents() + filteredLog.getNumberOfTraces() * 1.0);
		debug("Filtered empty traces: " + noiseEmptyTraces + ", noise events: " + noiseEvents.size() + " "
				+ ((float) noiseEvents.size() / filteredLog.getNumberOfEvents() * 100) + "% " + noiseEvents, parameters);
		debug("\"Fitness\": " + fitness, parameters);
		model.fitness = fitness;

		//debug(dummyRootNode.toString());
		model.root = dummyRootNode.getChild(0);
		//debug("mined model " + model.root.toString());

		return model;
	}

	private void mineProcessTree(Filteredlog log, final MiningParameters parameters, final Binoperator target, //the target where we must store our result 
			final int index, //in which subtree we must store our result
			final ThreadPool pool) {

		debug("", parameters);
		debug("==================", parameters);
		debug("Log size: " + String.valueOf(log.getNumberOfTraces()), parameters);
		//debug(log.toString());

		//read the log
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(log, parameters);
		UpToKSuccessorMatrix kSuccessor = UpToKSuccessor.fromLog(log, parameters);
		
		for (XEventClass a : directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet()) {
			debug("msd " + a.toString() + " (" + directlyFollowsRelation.getMinimumSelfDistance(a) + "): " + directlyFollowsRelation.getMinimumSelfDistanceBetween(a).toString(), parameters);
		}
		debug(kSuccessor.toString(), parameters);
		
		debug(DebugProbabilities.debug(directlyFollowsRelation, parameters), parameters);

		//base case: empty log
		if (log.getNumberOfEvents() + log.getNumberOfTraces() == 0) {
			//empty log, return tau
			debug("Empty log, discover tau " + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet(),
					parameters);
			Node node = new Tau();
			target.setChild(index, node);

			node.metadata.put("numberOfEvents", new Integer(0));
			node.metadata.put("numberOfTraces", new Integer(0));

			return;
		}

		if (log.getEventClasses().size() == 1) {
			//the log contains just one activity

			//assuming application of the activity follows a geometric distribution, we estimate parameter ^p

			//calculate the event-per-trace size of the log
			double p = log.getNumberOfTraces() / ((log.getNumberOfEvents() + log.getNumberOfTraces()) * 1.0);

			//debug("Single activity " + log.getEventClasses().iterator().next());
			//debug(" traces: " + log.getNumberOfTraces());
			//debug(" events: " + log.getNumberOfEvents());
			//debug(" p-value: " + String.valueOf(p));

			if (0.5 - parameters.getNoiseThreshold() <= p && p <= 0.5 + parameters.getNoiseThreshold()) {
				//^p is close enough to 0.5, consider it as a single activity
				debug(" discover activity", parameters);

				Node node = new EventClass(log.getEventClasses().iterator().next());
				node.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
				node.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));
				target.setChild(index, node);

				//update noise counters
				FilterResults results = log.applyFilterActivity(log.getEventClasses().iterator().next());
				registerFilteredNoise(node, results);

				return;
			}
			//debug(" do not discover activity");
			//else, the probability to stop is too low or too high, and we better output a flower model
		}

		//this clause is not proven in the paper
		if (directlyFollowsRelation.getNumberOfEpsilonTraces() != 0) {
			//the log contains empty traces

			if (directlyFollowsRelation.getNumberOfEpsilonTraces() < directlyFollowsRelation.getLengthStrongestTrace()
					* parameters.getNoiseThreshold()) {
				//there are not enough empty traces, the empty traces are considered noise

				//filter the empty traces from the log and recurse
				FilterResults result = log.applyEpsilonFilter();
				final Filteredlog sublog = result.sublogs.iterator().next();

				//save the filtered empty traces as metadata
				registerFilteredNoise(target, result);

				debug(" filter empty traces", parameters);

				recurse(parameters, pool, sublog, target, index);
				return;

			} else {
				//There are too many empty traces to consider them noise.
				//Mine an xor(tau, ..) and recurse.
				final Binoperator node = new ExclusiveChoice(2);
				node.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
				node.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));
				target.setChild(index, node);

				FilterResults result = log.applyEpsilonFilter();

				Tau tau = new Tau();
				node.setChild(0, tau);
				tau.metadata.put("numberOfEvents", new Integer(0));
				tau.metadata.put("numberOfTraces", new Integer(result.filteredEmptyTraces));

				debug(" mine x(tau, ..)", parameters);

				final Filteredlog sublog = result.sublogs.iterator().next();
				recurse(parameters, pool, sublog, node, 1);

				return;
			}
		}
		recursionStepsCounter++;

		//SAT cut search
		if (parameters.useSAT()) {
			if (mineSAT(log, parameters, target, index, pool, directlyFollowsRelation)) {
				return;
			}
		}

		//exclusive choice operator
		Set<Set<XEventClass>> exclusiveChoiceCut = ExclusiveChoiceCut.findExclusiveChoiceCut(directlyFollowsRelation
				.getDirectlyFollowsGraph());
		if (exclusiveChoiceCut.size() > 1) {
			final Binoperator node = new ExclusiveChoice(exclusiveChoiceCut.size());
			FilterResults filterResults = log.applyFilterExclusiveChoice(exclusiveChoiceCut);
			outputAndRecurse(parameters, target, index, pool, exclusiveChoiceCut, node, filterResults, log);
			return;
		}

		//sequence operator
		List<Set<XEventClass>> sequenceCut = SequenceCut.findSequenceCut(directlyFollowsRelation
				.getDirectlyFollowsGraph());
		if (sequenceCut.size() > 1) {
			final Binoperator node = new Sequence(sequenceCut.size());
			FilterResults filterResults = log.applyFilterSequence(sequenceCut);
			outputAndRecurse(parameters, target, index, pool, sequenceCut, node, filterResults, log);
			return;
		}

		//try exhaustive cut search
		if (parameters.isUseExhaustiveKSuccessor()) {
			Exhaustive exhaustive = new Exhaustive(log, kSuccessor, parameters);
			Exhaustive.Result er = exhaustive.tryAll();
			if (er.cutType == "parallel") {
				final Binoperator node = new Parallel(2);
				outputAndRecurse(parameters, target, index, pool, er.cut, node, er.sublogs, log);
				return;
			} else if (er.cutType == "loop") {
				final Binoperator node = new Loop(2);
				outputAndRecurse(parameters, target, index, pool, er.cut, node, er.sublogs, log);
				return;
			}
		}

		//parallel and loop operator
		Set<Set<XEventClass>> parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, true);
		List<Set<XEventClass>> loopCut = LoopCut.findLoopCut(directlyFollowsRelation);

		//sometimes, a parallel and loop cut are both possible 
		//in that case, recompute a stricter parallel cut using minimum-self-distance
		//if (parallelCut.size() > 1 && loopCut.size() > 1) {
		//	parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, true);
		//}
		//update: this is also useful for parallel-cut separations
		//only if the strict one doesn't work, return the more relaxed one
		if (parallelCut.size() == 0 && loopCut.size() == 0) {
			parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, false);
		}

		//parallel operator
		if (parallelCut.size() > 1) {
			final Binoperator node = new Parallel(parallelCut.size());
			FilterResults filterResults = log.applyFilterParallel(parallelCut);
			outputAndRecurse(parameters, target, index, pool, parallelCut, node, filterResults, log);
			return;
		}

		//loop operator
		if (loopCut.size() > 1) {
			final Binoperator node = new Loop(loopCut.size());
			FilterResults filterResults = log.applyFilterLoop(loopCut);
			outputAndRecurse(parameters, target, index, pool, loopCut, node, filterResults, log);
			return;
		}

		debug("fall through", parameters);

		//apply noise filtering
		DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered = null;
		if (parameters.getNoiseThreshold() != 0) {
			debug("filter noise", parameters);

			//filter noise
			directlyFollowsRelationNoiseFiltered = directlyFollowsRelation.filterNoise(parameters.getNoiseThreshold());
			DirectlyFollowsRelation directlyFollowsRelationIncompleteCorrected = directlyFollowsRelationNoiseFiltered
					.addIncompleteEdges(parameters.getIncompleteThreshold());

			//exclusive choice operator
			Set<Set<XEventClass>> exclusiveChoiceCutNoise = ExclusiveChoiceCut
					.findExclusiveChoiceCut(directlyFollowsRelationNoiseFiltered.getDirectlyFollowsGraph());
			if (exclusiveChoiceCutNoise.size() > 1) {
				final Binoperator node = new ExclusiveChoice(exclusiveChoiceCutNoise.size());
				FilterResults filterResults = log.applyFilterExclusiveChoice(exclusiveChoiceCutNoise);
				outputAndRecurse(parameters, target, index, pool, exclusiveChoiceCutNoise, node, filterResults, log);
				return;
			}

			//sequence operator
			List<Set<XEventClass>> sequenceCutNoise = SequenceCut.findSequenceCut(directlyFollowsRelationNoiseFiltered
					.getEventuallyFollowsGraph());
			if (sequenceCutNoise.size() > 1) {
				final Binoperator node = new Sequence(sequenceCutNoise.size());
				FilterResults filterResults = log.applyFilterSequence(sequenceCutNoise);
				outputAndRecurse(parameters, target, index, pool, sequenceCutNoise, node, filterResults, log);
				return;
			}

			//parallel and loop operator
			Set<Set<XEventClass>> parallelCutNoise = ParallelCut.findParallelCut(
					directlyFollowsRelationIncompleteCorrected, false);
			List<Set<XEventClass>> loopCutNoise = LoopCut.findLoopCut(directlyFollowsRelationNoiseFiltered);

			//sometimes, a parallel and loop cut are both possible
			//in that case, recompute a stricter parallel cut using minimum-self-distance
			if (parallelCutNoise.size() > 1 && loopCutNoise.size() > 1) {
				parallelCutNoise = ParallelCut.findParallelCut(directlyFollowsRelationIncompleteCorrected, true);
			}

			//parallel operator
			if (parallelCutNoise.size() > 1) {
				final Binoperator node = new Parallel(parallelCutNoise.size());
				FilterResults filterResults = log.applyFilterParallel(parallelCutNoise);
				outputAndRecurse(parameters, target, index, pool, parallelCutNoise, node, filterResults, log);
				return;
			}

			//loop operator
			if (loopCutNoise.size() > 1) {
				final Binoperator node = new Loop(loopCutNoise.size());
				FilterResults filterResults = log.applyFilterLoop(loopCutNoise);
				outputAndRecurse(parameters, target, index, pool, loopCutNoise, node, filterResults, log);
				return;
			}
		}

		//tau loop
		Filteredlog tauloopSublog = tauLoop(log, parameters, target, index, pool, directlyFollowsRelation,
				directlyFollowsRelationNoiseFiltered);
		if (tauloopSublog != null) {
			final Binoperator node = new Loop(2);
			target.setChild(index, node);
			node.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
			node.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));

			Tau tau = new Tau();
			node.setChild(1, tau);

			debug("Chosen tau loop", parameters);

			recurse(parameters, pool, tauloopSublog, node, 0);
			return;
		}

		//flower loop fall-through
		{
			debug("Chosen flower loop {" + Sets.implode(log.getEventClasses(), ", ") + "}", parameters);

			Binoperator node = new Loop(log.getEventClasses().size() + 1);
			node.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
			node.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));
			node.setChild(0, new Tau());

			//filter the log
			List<Set<XEventClass>> sigmas = new LinkedList<Set<XEventClass>>();
			sigmas.add(new HashSet<XEventClass>());
			for (XEventClass a : log.getEventClasses()) {
				Set<XEventClass> sigma = new HashSet<XEventClass>();
				sigma.add(a);
				sigmas.add(sigma);
			}
			FilterResults result = log.applyFilterLoop(sigmas);

			int i = 1;
			Iterator<Filteredlog> it = result.sublogs.iterator();
			for (XEventClass a : log.getEventClasses()) {
				Node child = new EventClass(a);

				Filteredlog sublog = it.next();
				child.metadata.put("numberOfEvents", new Integer(sublog.getNumberOfEvents()));
				child.metadata.put("numberOfTraces", new Integer(sublog.getNumberOfTraces()));

				node.setChild(i, child);
				i++;
			}
			target.setChild(index, node);

			//output XES
			outputXES(log, parameters, node);

			return;
		}
	}

	private boolean mineSAT(Filteredlog log, final MiningParameters parameters, final Binoperator target,
			final int index, final ThreadPool pool, DirectlyFollowsRelation directlyFollowsRelation) {
		 
		ThreadPool SATPool = new ThreadPool(1);
		AtomicResult bestSATResult = new AtomicResult(parameters.getIncompleteThreshold());
		parameters.getSatProbabilities().setDirectlyFollowsRelation(directlyFollowsRelation);
		
		(new SATSolveXor(directlyFollowsRelation, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveSequence(directlyFollowsRelation, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveParallel(directlyFollowsRelation, parameters, SATPool, bestSATResult)).solve();
		(new SATSolveLoop(directlyFollowsRelation, parameters, SATPool, bestSATResult)).solve();
		
		try {
			SATPool.join();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		SATResult satResult = bestSATResult.get();

		if (satResult.getCut() != null) {
			if (satResult.getType() == "xor") {
				Set<Set<XEventClass>> xorCutIncomplete = new HashSet<Set<XEventClass>>(satResult.getCut());
				final Binoperator node = new ExclusiveChoice(xorCutIncomplete.size());
				FilterResults filterResults = log.applyFilterExclusiveChoice(xorCutIncomplete);
				outputAndRecurse(parameters, target, index, pool, xorCutIncomplete, node, filterResults, log);
				return true;
			} else if (satResult.getType() == "sequence") {
				final Binoperator node = new Sequence(satResult.getCut().size());
				FilterResults filterResults = log.applyFilterSequence(satResult.getCut());
				outputAndRecurse(parameters, target, index, pool, satResult.getCut(), node, filterResults, log);
				return true;
			} else if (satResult.getType() == "parallel") {
				Set<Set<XEventClass>> parallelCutIncomplete = new HashSet<Set<XEventClass>>(satResult.getCut());
				final Binoperator node = new Parallel(parallelCutIncomplete.size());
				FilterResults filterResults = log.applyFilterParallel(parallelCutIncomplete);
				outputAndRecurse(parameters, target, index, pool, parallelCutIncomplete, node, filterResults, log);
				return true;
			} else if (satResult.getType() == "loop") {
				final Binoperator node = new Loop(satResult.getCut().size());
				FilterResults filterResults = log.applyFilterLoop(satResult.getCut());
				outputAndRecurse(parameters, target, index, pool, satResult.getCut(), node, filterResults, log);
				return true;
			}
		}
		return false;
	}

	private Filteredlog tauLoop(Filteredlog log, final MiningParameters parameters, final Binoperator target,
			final int index, final ThreadPool pool, DirectlyFollowsRelation directlyFollowsRelation,
			DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered) {

		DirectlyFollowsRelation dfr;
		if (parameters.getNoiseThreshold() != 0) {
			dfr = directlyFollowsRelationNoiseFiltered;
		} else {
			dfr = directlyFollowsRelation;
		}

		List<Set<XEventClass>> tauLoopCut = new LinkedList<Set<XEventClass>>();
		tauLoopCut.add(new HashSet<XEventClass>());
		tauLoopCut.get(0).addAll(directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet());
		FilterResults filterResults = log.applyFilterTauLoop(tauLoopCut, dfr.getStartActivities().toSet(), dfr
				.getEndActivities().toSet());
		final Filteredlog sublog = ((List<Filteredlog>) filterResults.sublogs).get(0);

		if (sublog.getNumberOfTraces() > log.getNumberOfTraces()) {
			return sublog;
		}

		return null;
	}

	private void outputXES(Filteredlog log, final MiningParameters parameters, Node node) {
		if (parameters.getOutputFlowerLogFileName() != null) {
			XLog xLog = log.toXLog();
			XSerializer logSerializer = new XesXmlSerializer();
			try {
				File file = new File(parameters.getOutputFlowerLogFileName() + "" + recursionStepsCounter);
				FileOutputStream out = new FileOutputStream(file);
				logSerializer.serialize(xLog, out);
				out.close();
				node.metadata.put("logFile", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	private void outputAndRecurse(final MiningParameters parameters, final Binoperator target, final int index,
			final ThreadPool pool, Collection<Set<XEventClass>> cut, final Binoperator newTargetNode,
			FilterResults filterResults, Filteredlog log) {

		registerFilteredNoise(newTargetNode, filterResults);

		outputAndRecurse(parameters, target, index, pool, cut, newTargetNode, filterResults.sublogs, log);
	}

	private void outputAndRecurse(final MiningParameters parameters, final Binoperator target, final int index,
			final ThreadPool pool, Collection<Set<XEventClass>> cut, final Binoperator newTargetNode,
			Collection<Filteredlog> sublogs, Filteredlog log) {

		//store metadata
		newTargetNode.metadata.put("numberOfEvents", new Integer(log.getNumberOfEvents()));
		newTargetNode.metadata.put("numberOfTraces", new Integer(log.getNumberOfTraces()));

		//output the cut
		debugCut(newTargetNode, cut, sublogs, parameters);

		//set the result
		target.setChild(index, newTargetNode);
		int i = 0;
		for (Filteredlog sublog : sublogs) {
			final Filteredlog sublog2 = sublog;
			final int j = i;
			recurse(parameters, pool, sublog2, newTargetNode, j);
			i++;
		}
	}

	private void recurse(final MiningParameters parameters, final ThreadPool pool, final Filteredlog sublog,
			final Binoperator newTargetNode, final int newTargetChildIndex) {
		pool.addJob(new Runnable() {
			public void run() {
				mineProcessTree(sublog, parameters, newTargetNode, newTargetChildIndex, pool);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void registerFilteredNoise(Node node, FilterResults result) {

		//noisy events
		if (result.filteredEvents != null) {
			//store as metadata
			if (!node.metadata.containsKey("filteredEvents")) {
				node.metadata.put("filteredEvents", new MultiSet<XEventClass>());
			}

			//global
			synchronized (noiseEvents) {
				noiseEvents.addAll(result.filteredEvents);
			}

			//local
			MultiSet<XEventClass> s = ((MultiSet<XEventClass>) node.metadata.get("filteredEvents"));
			synchronized (s) {
				s.addAll(result.filteredEvents);
			}
		}

		//empty traces
		if (result.filteredEmptyTraces != 0) {
			//store as metadata
			if (!node.metadata.containsKey("filteredEmptyTraces")) {
				node.metadata.put("filteredEmptyTraces", new AtomicInteger(0));
			}

			//global
			noiseEmptyTraces.addAndGet(result.filteredEmptyTraces);

			//local
			((AtomicInteger) node.metadata.get("filteredEmptyTraces")).addAndGet(result.filteredEmptyTraces);
		}
	}

	private void debugCut(Binoperator node, Collection<Set<XEventClass>> cut, Collection<Filteredlog> sublogs,
			MiningParameters parameters) {
		StringBuilder r = new StringBuilder("step " + recursionStepsCounter + " chosen " + node.getOperatorString());
		Iterator<Set<XEventClass>> it = cut.iterator();
		while (it.hasNext()) {
			r.append(" {" + Sets.implode(it.next(), ", ") + "}");
		}
		r.append("\n");

		debug(r.toString(), parameters);
	}

	private void debug(String x, MiningParameters parameters) {
		if (parameters.isDebug()) {
			System.out.println(x);
		}
	}
}
