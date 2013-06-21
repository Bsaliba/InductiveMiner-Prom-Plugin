package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import org.processmining.plugins.InductiveMiner.mining.cuts.ExclusiveChoiceCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.LoopCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.ParallelCut;
import org.processmining.plugins.InductiveMiner.mining.cuts.SequenceCut;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.plugins.InductiveMiner.model.Binoperator;
import org.processmining.plugins.InductiveMiner.model.EventClass;
import org.processmining.plugins.InductiveMiner.model.ExclusiveChoice;
import org.processmining.plugins.InductiveMiner.model.Loop;
import org.processmining.plugins.InductiveMiner.model.Parallel;
import org.processmining.plugins.InductiveMiner.model.ProcessTreeModel;
import org.processmining.plugins.InductiveMiner.model.Sequence;
import org.processmining.plugins.InductiveMiner.model.Tau;
import org.processmining.plugins.InductiveMiner.model.conversion.Dot2Image;
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

	public ProcessTreeModel mine(PluginContext context, XLog log, MiningParameters parameters) {
		//prepare the log
		//debug("Start conversion to internal log format");
		Filteredlog filteredLog = new Filteredlog(log, parameters);

		//debug initial log
		//debug(filteredLog.toString());
		debug("\n\nStart mining");
		recursionStepsCounter = -1;

		//create the model
		ProcessTreeModel model = new ProcessTreeModel();
		final MultiSet<XEventClass> noiseEvents = new MultiSet<XEventClass>();
		final AtomicInteger noiseEmptyTraces = new AtomicInteger();

		//initialise the thread pool
		ThreadPool pool = new ThreadPool(0);

		//add a dummy node and mine
		Binoperator dummyRootNode = new Sequence(1);
		mineProcessTree(filteredLog, parameters, dummyRootNode, 0, pool, noiseEvents, noiseEmptyTraces);

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
		debug("Filtered empty traces: " + noiseEmptyTraces + ", noise events: "
				+ ((float) noiseEvents.size() / filteredLog.getNumberOfEvents() * 100) + "% " + noiseEvents);
		debug("\"Fitness\": " + fitness);
		model.fitness = fitness;

		//debug(dummyRootNode.toString());
		model.root = dummyRootNode.getChild(0);
		//debug("mined model " + model.root.toString());

		return model;
	}

	private void mineProcessTree(Filteredlog log, final MiningParameters parameters, final Binoperator target, //the target where we must store our result 
			final int index, //in which subtree we must store our result
			final ThreadPool pool, final MultiSet<XEventClass> noiseEvents, final AtomicInteger noiseEmptyTraces) {

		debug("");
		debug("==================");
		//debug(log.toString());

		//read the log
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(log, parameters);

		//base case: empty log
		if (log.getNumberOfEvents() + log.getNumberOfTraces() == 0) {
			//empty log, return tau
			debug("Empty log, discover tau " + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet());
			target.setChild(index, new Tau());
			return;
		}

		if (log.getEventClasses().size() == 1) {
			//the log contains just one activity

			//assuming application of the activity follows a geometric distribution, we estimate parameter ^p

			//calculate the event-per-trace size of the log
			double p = log.getNumberOfTraces() / ((log.getNumberOfEvents() + log.getNumberOfTraces()) * 1.0);
			debug("Single activity " + log.getEventClasses().iterator().next());
			debug(" traces: " + log.getNumberOfTraces());
			debug(" events: " + log.getNumberOfEvents());
			debug(" p-value: " + String.valueOf(p));

			if (0.5 - parameters.getNoiseThreshold() <= p && p <= 0.5 + parameters.getNoiseThreshold()) {
				//^p is close enough to 0.5, consider it as a single activity
				debug(" discover activity");

				//update noise counters
				log.applyFilterActivity(log.getEventClasses().iterator().next(), noiseEvents, noiseEmptyTraces);

				target.setChild(index, new EventClass(log.getEventClasses().iterator().next()));
				return;
			}
			debug(" do not discover activity");
			//else, the probability to stop is too low or too high, and we better output a flower model
		}

		//this clause is not proven in the paper
		if (directlyFollowsRelation.getNumberOfEpsilonTraces() != 0) {
			//the log contains empty traces

			//debug(log.toString());
			//debug("remove epsilon from log" + directlyFollowsRelation.getNumberOfEpsilonTraces() + " / " + directlyFollowsRelation.getLengthStrongestTrace());

			if (directlyFollowsRelation.getNumberOfEpsilonTraces() < directlyFollowsRelation.getLengthStrongestTrace()
					* parameters.getNoiseThreshold()) {
				//there are not enough empty traces, the empty traces are considered noise

				debug("Filtered empty traces: " + directlyFollowsRelation.getNumberOfEpsilonTraces());
				noiseEmptyTraces.addAndGet(directlyFollowsRelation.getNumberOfEpsilonTraces());

				//filter the empty traces from the log and recurse
				final Filteredlog sublog = log.applyEpsilonFilter();

				//debug(" Size after epsilon filter: " + String.valueOf(sublog.getSize()));

				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog, parameters, target, index, pool, noiseEvents, noiseEmptyTraces);
					}
				});

			} else {
				//There are too many empty traces to consider them noise.
				//Mine an xor(tau, ..) and recurse.
				final Binoperator node = new ExclusiveChoice(2);
				node.setChild(0, new Tau());
				target.setChild(index, node);

				final Filteredlog sublog = log.applyEpsilonFilter();
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog, parameters, node, 1, pool, noiseEvents, noiseEmptyTraces);
					}
				});
			}
			return;
		}

		debug("Log size: " + String.valueOf(log.getNumberOfTraces()));
		recursionStepsCounter++;

		//exclusive choice operator
		Set<Set<XEventClass>> exclusiveChoiceCut = ExclusiveChoiceCut.findExclusiveChoiceCut(directlyFollowsRelation
				.getDirectlyFollowsGraph());
		if (exclusiveChoiceCut.size() > 1) {
			//set the result
			final Binoperator node = new ExclusiveChoice(exclusiveChoiceCut.size());
			target.setChild(index, node);

			debugCut(node, exclusiveChoiceCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelation, exclusiveChoiceCut, false);

			//filter the log and recurse
			Set<Filteredlog> sublogs = log.applyFilterExclusiveChoice(exclusiveChoiceCut, noiseEvents);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//sequence operator
		List<Set<XEventClass>> sequenceCut = SequenceCut.findSequenceCut(directlyFollowsRelation
				.getDirectlyFollowsGraph());
		if (sequenceCut.size() > 1) {
			//set the result
			final Binoperator node = new Sequence(sequenceCut.size());
			target.setChild(index, node);

			debugCut(node, sequenceCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelation, sequenceCut, true);

			List<Filteredlog> sublogs = log.applyFilterSequence(sequenceCut, noiseEvents);

			//filter the log and recurse
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//parallel and loop operator
		Set<Set<XEventClass>> parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, false);
		List<Set<XEventClass>> loopCut = LoopCut.findLoopCut(directlyFollowsRelation);

		//sometimes, a parallel and loop cut are both possible
		//in that case, recompute a stricter parallel cut using minimum-self-distance
		if (parallelCut.size() > 1 && loopCut.size() > 1) {
			parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, true);
		}

		if (parallelCut.size() > 1) {

			//noise tryout
			//ParallelCut.allPossibilities(directlyFollowsRelation2, pool);
			//debug(log.toString());

			final Binoperator node = new Parallel(parallelCut.size());
			target.setChild(index, node);

			debugCut(node, parallelCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelation, parallelCut, false);

			Set<Filteredlog> sublogs = log.applyFilterParallel(parallelCut, noiseEvents);

			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//loop operator
		if (loopCut.size() > 1) {
			final Binoperator node = new Loop(loopCut.size());
			target.setChild(index, node);

			debugCut(node, loopCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelation, loopCut, false);

			//filter the log and recurse
			List<Filteredlog> sublogs = log.applyFilterLoop(loopCut, noiseEvents);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//with noise
		//filter noise
		DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered = directlyFollowsRelation.filterNoise(parameters
				.getNoiseThreshold());

		//exclusive choice operator
		Set<Set<XEventClass>> exclusiveChoiceCutNoise = ExclusiveChoiceCut
				.findExclusiveChoiceCut(directlyFollowsRelationNoiseFiltered.getDirectlyFollowsGraph());
		if (exclusiveChoiceCutNoise.size() > 1) {
			//set the result
			final Binoperator node = new ExclusiveChoice(exclusiveChoiceCutNoise.size());
			target.setChild(index, node);

			debugCut(node, exclusiveChoiceCutNoise);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered,
					exclusiveChoiceCutNoise, false);

			//filter the log and recurse
			Set<Filteredlog> sublogs = log.applyFilterExclusiveChoice(exclusiveChoiceCutNoise, noiseEvents);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//sequence operator
		List<Set<XEventClass>> sequenceCutNoise = SequenceCut.findSequenceCut(directlyFollowsRelationNoiseFiltered
				.getEventuallyFollowsGraph());
		if (sequenceCutNoise.size() > 1) {
			//set the result
			final Binoperator node = new Sequence(sequenceCutNoise.size());
			target.setChild(index, node);

			debugCut(node, sequenceCutNoise);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, sequenceCutNoise,
					true);

			List<Filteredlog> sublogs = log.applyFilterSequence(sequenceCutNoise, noiseEvents);

			//filter the log and recurse
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//parallel and loop operator
		Set<Set<XEventClass>> parallelCutNoise = ParallelCut.findParallelCut(directlyFollowsRelation, false);
		List<Set<XEventClass>> loopCutNoise = LoopCut.findLoopCut(directlyFollowsRelationNoiseFiltered);

		//sometimes, a parallel and loop cut are both possible
		//in that case, recompute a stricter parallel cut using minimum-self-distance
		if (parallelCutNoise.size() > 1 && loopCutNoise.size() > 1) {
			parallelCutNoise = ParallelCut.findParallelCut(directlyFollowsRelation, true);
		}

		if (parallelCutNoise.size() > 1) {

			//noise tryout
			//ParallelCut.allPossibilities(directlyFollowsRelation2, pool);
			//debug(log.toString());

			final Binoperator node = new Parallel(parallelCutNoise.size());
			target.setChild(index, node);

			debugCut(node, parallelCutNoise);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, parallelCutNoise,
					false);

			Set<Filteredlog> sublogs = log.applyFilterParallel(parallelCutNoise, noiseEvents);

			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//loop operator
		if (loopCutNoise.size() > 1) {
			final Binoperator node = new Loop(loopCutNoise.size());
			target.setChild(index, node);

			debugCut(node, loopCutNoise);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, loopCutNoise, false);

			//filter the log and recurse
			List<Filteredlog> sublogs = log.applyFilterLoop(loopCutNoise, noiseEvents);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(new Runnable() {
					public void run() {
						mineProcessTree(sublog2, parameters, node, j, pool, noiseEvents, noiseEmptyTraces);
					}
				});
				i++;
			}

			return;
		}

		//flower loop fall-through
		Binoperator node = new Loop(log.getEventClasses().size() + 1);
		node.setChild(0, new Tau());

		debug("step " + recursionStepsCounter + " chosen flower loop {" + Sets.implode(log.getEventClasses(), ", ")
				+ "}");
		outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, null, false);
		
		//output XES
		XLog xLog = log.toLog();
		XSerializer logSerializer = new XesXmlSerializer();
		debug(xLog.size() + "");
		try {
			FileOutputStream out = new FileOutputStream(new File(parameters.getOutputFlowerLogFileName() + "" + recursionStepsCounter));
			logSerializer.serialize(xLog, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int i = 1;
		for (XEventClass a : log.getEventClasses()) {
			node.setChild(i, new EventClass(a));
			i++;
		}
		target.setChild(index, node);

		return;
	}

	private void outputImage(MiningParameters parameters, DirectlyFollowsRelation directlyFollowsRelation,
			DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered, Collection<Set<XEventClass>> cut,
			boolean includeEventuallyFollows) {
		//output an image of the directly follows graph

		if (parameters.getOutputDFGfileName() != null) {
			//original
			Dot2Image.dot2image(directlyFollowsRelation.toDot(cut, false), new File(parameters.getOutputDFGfileName()
					+ recursionStepsCounter + "dfg.png"), null);
			//noise filtered
			Dot2Image
					.dot2image(directlyFollowsRelationNoiseFiltered.toDot(cut, false),
							new File(parameters.getOutputDFGfileName() + recursionStepsCounter
									+ "dfg-noiseFiltered.png"), null);
		}
	}

	private void debugCut(Binoperator node, Collection<Set<XEventClass>> cut) {
		String r = "step " + recursionStepsCounter + " chosen " + node.getOperatorString();
		Iterator<Set<XEventClass>> it = cut.iterator();
		while (it.hasNext()) {
			r += " {" + Sets.implode(it.next(), ", ") + "}";
		}
		debug(r);
	}

	private void debug(String x) {
		System.out.println(x);
	}
}
