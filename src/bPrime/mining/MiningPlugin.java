package bPrime.mining;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.processtree.ProcessTree;

import bPrime.ProcessTreeModelConnection;
import bPrime.Sets;
import bPrime.ThreadPool;
import bPrime.model.Binoperator;
import bPrime.model.EventClass;
import bPrime.model.ExclusiveChoice;
import bPrime.model.Loop;
import bPrime.model.Parallel;
import bPrime.model.ProcessTreeModel;
import bPrime.model.ProcessTreeModel.Operator;
import bPrime.model.Sequence;
import bPrime.model.Tau;
import bPrime.model.conversion.Dot2Image;
import bPrime.model.conversion.ProcessTreeModel2PetriNet;
import bPrime.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;
import bPrime.model.conversion.ProcessTreeModel2ProcessTree;

@Plugin(name = "Mine a Process Tree using B'", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class MiningPlugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, default", requiredParameterLabels = { 0 })
	public ProcessTree mineDefault(PluginContext context, XLog log) {
		return this.mineParameters(context, log, new MiningParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, parameterized", requiredParameterLabels = { 0, 1 })
	public ProcessTree mineParameters(PluginContext context, XLog log, MiningParameters parameters) {
		Collection<ProcessTreeModelConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(ProcessTreeModelConnection.class, context, log);
			for (ProcessTreeModelConnection connection : connections) {
				if (connection.getObjectWithRole(ProcessTreeModelConnection.LOG).equals(log)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ProcessTreeModelConnection.MODEL);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		ProcessTreeModel model = mine(context, log, parameters);
		ProcessTree tree = ProcessTreeModel2ProcessTree.convert(model.root);
		context.addConnection(new ProcessTreeModelConnection(log, tree, parameters));
		return tree;
	}
	/*
@Plugin(name = "Mine a Process Tree using B'", returnLabels = { "Petri net", "Initial marking", "Final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class MiningPlugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree Petri net, default", requiredParameterLabels = { 0 })
	public Object[] mineDefaultPetrinet(PluginContext context, XLog log) {
		return this.mineParametersPetrinet(context, log, new MiningParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree Petri net, parameterized", requiredParameterLabels = { 0, 1 })
	public Object[] mineParametersPetrinet(PluginContext context, XLog log, MiningParameters parameters) {
		Collection<ProcessTreeModelConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(ProcessTreeModelConnection.class, context, log);
			for (ProcessTreeModelConnection connection : connections) {
				if (connection.getObjectWithRole(ProcessTreeModelConnection.LOG).equals(log)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ProcessTreeModelConnection.MODEL);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		//call the connectionless function
		Object[] arr = mineParametersPetrinetWithoutConnections(context, log, parameters);
		ProcessTreeModel2PetriNet.WorkflowNet workflowNet = (WorkflowNet) arr[1];
		TransEvClassMapping mapping = (TransEvClassMapping) arr[2];
		
		ProcessTreeModel2PetriNet.addMarkingsToProm(context, workflowNet);
		
		//create connections
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.addConnection(new LogPetrinetConnectionImpl(log, info.getEventClasses(), workflowNet.petrinet, workflowNet.transition2eventClass));
		
		context.addConnection(new EvClassLogPetrinetConnection("classifier-log-petrinet connection", workflowNet.petrinet, log, parameters.getClassifier(), mapping));
		
		return new Object[] { workflowNet.petrinet, workflowNet.initialMarking, workflowNet.finalMarking };
	}*/
	
	public Object[] mineParametersPetrinetWithoutConnections(PluginContext context, XLog log, MiningParameters parameters) {
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
		
		return new Object[] {model, workflowNet, mapping, activities};
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
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		XEventClasses eventClasses = info.getEventClasses();
		ProcessTreeModel model = new ProcessTreeModel(eventClasses);
		
		//initialise the thread pool
		ThreadPool pool = new ThreadPool(0);
		
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
		
		//debug(dummyRootNode.toString());
		model.root = dummyRootNode.getChild(0);
		//debug("mined model " + model.root.toString());
		
		return model;
	}
	
	private void outputImage(MiningParameters parameters, 
			DirectlyFollowsRelation directlyFollowsRelation,
			DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered,
			Collection<Set<XEventClass>> cut) {
		//output an image of the directly follows graph
		
		if (parameters.getOutputDFGfileName() != null) {
			//original
			Dot2Image.dot2image(directlyFollowsRelation.toDot(cut), 
					new File(parameters.getOutputDFGfileName() + recursionStepsCounter + ".png"), 
					null);
			//noise filtered
			Dot2Image.dot2image(directlyFollowsRelationNoiseFiltered.toDot(cut), 
					new File(parameters.getOutputDFGfileName() + recursionStepsCounter + "-noiseFiltered.png"), 
					null);
			debug("dfr-images " + recursionStepsCounter);
		}
	}
	
	private void mineProcessTree(
			Filteredlog log, 
			final MiningParameters parameters, 
			final Binoperator target, //the target where we must store our result 
			final int index, //in which subtree we must store our result
			final ThreadPool pool) {
		
		//debug("");
		//debug("==================");
		//debug(log.toString());
		
		//read the log
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(log, parameters);
		
		//filter noise
		DirectlyFollowsRelation directlyFollowsRelationNoiseFiltered = directlyFollowsRelation.filterNoise(parameters.getNoiseThreshold());
		
		//this clause is not proven in the paper
		//filter out the empty traces by adding an xor-operator
		if (directlyFollowsRelation.getNumberOfEpsilonTraces() != 0) {
			//the log contains the empty trace
			
			//debug(log.toString());
			//debug("remove epsilon from log" + directlyFollowsRelation.getNumberOfEpsilonTraces() + " / " + directlyFollowsRelation.getLengthStrongestTrace());
			
			if (directlyFollowsRelation.getNumberOfEpsilonTraces() < directlyFollowsRelation.getLengthStrongestTrace() * parameters.getNoiseThreshold()) {
				//there are not enough empty traces, the empty traces are considered noise
				
				debug(" Filtered noise: " + directlyFollowsRelation.getNumberOfEpsilonTraces() + " empty traces.");
				
				//filter the empty traces from the log and recurse
				final Filteredlog sublog = log.applyEpsilonFilter();
				
				//debug(" Size after epsilon filter: " + String.valueOf(sublog.getSize()));
				
				pool.addJob(
					new Runnable() {
			            public void run() {
			            	mineProcessTree(sublog, parameters, target, index, pool);
			            }
				});
				
			} else {
				//There are too many empty traces to consider them noise.
				//Mine an xor(tau, ..) and recurse.
				final Binoperator node = new ExclusiveChoice(2);
				node.setChild(0, new Tau());
				target.setChild(index, node);
				
				final Filteredlog sublog = log.applyEpsilonFilter();
				pool.addJob(
					new Runnable() {
			            public void run() {
			            	mineProcessTree(sublog, parameters, node, 1, pool);
			            }
				});
			}
			return;
		}
		
		if (log.getEventClasses().size() == 1) {
			//the log contains just one activity
			
				
			//assuming application of the activity follows a geometric distribution, we estimate parameter ^p
				
			//calculate the event-per-trace size of the log
			double p = log.getNumberOfTraces() / ((log.getNumberOfEvents() + log.getNumberOfTraces())*1.0);
			debug("traces: " + log.getNumberOfTraces());
			debug("events: " + log.getNumberOfEvents());
			debug("p-value " + String.valueOf(p));
			debug(log.toString());
			
			if (p >= 1 - parameters.getNoiseThreshold()) {
				//the probability to stop is so high, we better ignore all non-empty traces
				debug("short traces, discover tau");
				target.setChild(index, new Tau());
				return;
			} else if (p < parameters.getNoiseThreshold()) {
				//the probability to stop is so low, we better ignore all empty traces
				debug("long traces, filter empty traces");
				final Filteredlog sublog = log.applyEpsilonFilter();
				pool.addJob(
					new Runnable() {
			            public void run() {
			            	mineProcessTree(sublog, parameters, target, index, pool);
			            }
				});
				return;
			} else if (p >= 0.5) {
				//the average is somewhere between 0 and 1 events per trace.
				//consider it as clear evidence of a single activity
				debug("neither vlees nor vis, discover activity");
				target.setChild(index, new EventClass(log.getEventClasses().iterator().next()));
				return;
			}
		}

		debug("Log size: " + String.valueOf(log.getNumberOfTraces()));
		recursionStepsCounter++;
		
		//exclusive choice operator
		Set<Set<XEventClass>> exclusiveChoiceCut = ExclusiveChoiceCut.findExclusiveChoiceCut(directlyFollowsRelationNoiseFiltered.getDirectlyFollowsGraph());
		if (exclusiveChoiceCut.size() > 1) {
			//set the result
			final Binoperator node = new ExclusiveChoice(exclusiveChoiceCut.size());
			target.setChild(index, node);
			
			debugCut(node, exclusiveChoiceCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, exclusiveChoiceCut);
			
			//filter the log and recurse
			Set<Filteredlog> sublogs = log.applyFilterExclusiveChoice2(exclusiveChoiceCut);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(
						new Runnable() {
				            public void run() {
				            	mineProcessTree(sublog2, parameters, node, j, pool);
				            }
				});
				i++;
			}
			
			return;
		}
		
		//sequence operator
		List<Set<XEventClass>> sequenceCut = SequenceCut.findSequenceCut(directlyFollowsRelationNoiseFiltered.getEventuallyFollowsGraph());
		if (sequenceCut.size() > 1) {
			//set the result
			final Binoperator node = new Sequence(sequenceCut.size());
			target.setChild(index, node);
			
			debugCut(node, sequenceCut);
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, sequenceCut);
			
			List<Filteredlog> sublogs = log.applyFilterSequence(sequenceCut);
			
			//filter the log and recurse
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(
						new Runnable() {
				            public void run() {
				            	mineProcessTree(sublog2, parameters, node, j, pool);
				            }
				});
				i++;
			}
			
			return;
		}
		
		//parallel and loop operator
		Set<Set<XEventClass>> parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation, false);
		List<Set<XEventClass>> loopCut = LoopCut.findLoopCut(directlyFollowsRelationNoiseFiltered);
		
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
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, parallelCut);
			
			int i = 0;
			for (Set<XEventClass> activities : parallelCut) {
				final Filteredlog sublog = log.applyFilter(Operator.PARALLEL, activities);
				final int j = i;
				pool.addJob(
						new Runnable() {
				            public void run() {
				            	mineProcessTree(sublog, parameters, node, j, pool);
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
			outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, loopCut);
			
			
			//filter the log and recurse
			List<Filteredlog> sublogs = log.applyFilterLoop(loopCut);
			int i = 0;
			for (Filteredlog sublog : sublogs) {
				final Filteredlog sublog2 = sublog;
				final int j = i;
				pool.addJob(
						new Runnable() {
				            public void run() {
				            	mineProcessTree(sublog2, parameters, node, j, pool);
				            }
				});
				i++;
			}
			
			return;
		}
		
		//flower loop fall-through
		Binoperator node = new Loop(log.getEventClasses().size()+1);
		node.setChild(0, new Tau());
		
		debug("step " + recursionStepsCounter + " chosen flower loop {" + Sets.implode(log.getEventClasses(), ", ") + "}");
		outputImage(parameters, directlyFollowsRelation, directlyFollowsRelationNoiseFiltered, null);
		
		int i = 1;
		for (XEventClass a : log.getEventClasses()) {
			node.setChild(i, new EventClass(a));
			i++;
		}
		target.setChild(index, node);
		
		return;
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
