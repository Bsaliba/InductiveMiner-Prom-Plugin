package bPrime.mining;

import java.util.Collection;
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
import org.processmining.processtree.ProcessTree;

import bPrime.ProcessTreeModelConnection;
import bPrime.ProcessTreeModelParameters;
import bPrime.Sets;
import bPrime.model.Binoperator;
import bPrime.model.ConversionToProcessTrees;
import bPrime.model.EventClass;
import bPrime.model.ExclusiveChoice;
import bPrime.model.Loop;
import bPrime.model.Parallel;
import bPrime.model.ProcessTreeModel;
import bPrime.model.ProcessTreeModel.Operator;
import bPrime.model.Sequence;
import bPrime.model.Tau;

@Plugin(name = "Mine a Process Tree using B'", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class MiningPlugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, default", requiredParameterLabels = { 0 })
	public ProcessTree mineDefault(PluginContext context, XLog log) {
		return this.mineParameters(context, log, new ProcessTreeModelParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, parameterized", requiredParameterLabels = { 0, 1 })
	public ProcessTree mineParameters(PluginContext context, XLog log, ProcessTreeModelParameters parameters) {
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
		ProcessTree tree = ConversionToProcessTrees.convert(model.root);
		context.addConnection(new ProcessTreeModelConnection(log, tree, parameters));
		return tree;
	}
	
	private ProcessTreeModel mine(PluginContext context, XLog log, ProcessTreeModelParameters parameters) {
		//prepare the log
		debug("Start conversion to internal log format");
		Filteredlog filteredLog = new Filteredlog(log, parameters);
		//debug(filteredLog.toString());
		
		//create the model
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		XEventClasses eventClasses = info.getEventClasses();
		ProcessTreeModel model = new ProcessTreeModel(eventClasses);
		
		//initialise the thread pool
		ThreadPool pool = new ThreadPool();
		
		//add a dummy node and mine
		Binoperator dummyRootNode = new Sequence(1);
		mineProcessTree(filteredLog, parameters, dummyRootNode, 0, pool);
		
		//wait for all jobs to terminate
		try {
			pool.join();
		} catch (ExecutionException e) {
			debug("something failed");
			e.printStackTrace();
			model.root = null;
		}
		
		//debug(dummyRootNode.toString());
		model.root = dummyRootNode.getChild(0);
		debug("mined model " + model.root.toString());
		
		return model;
	}
	
	private void mineProcessTree(
			Filteredlog log, 
			final ProcessTreeModelParameters parameters, 
			Binoperator target, //the target where we must store our result 
			int index, //in which subtree we must store our result
			final ThreadPool pool) {
		
		debug("");
		debug("==================");
		//debug(log.toString());
		
		//read the log
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(log, parameters);
		
		//this clause is not proven in the paper
		//filter out the empty traces by adding an xor-operator
		if (directlyFollowsRelation.getTauPresent()) {
			//log contains the empty trace
			final Binoperator node = new ExclusiveChoice(2);
			node.setChild(0, new Tau());
			target.setChild(index, node);
			final Filteredlog sublog = log.applyTauFilter();
			pool.addJob(
				new Runnable() {
		            public void run() {
		            	mineProcessTree(sublog, parameters, node, 1, pool);
		            }
			});
			debug("remove tau from log");
			return;
		}
		
		if (log.getEventClasses().size() == 1 && directlyFollowsRelation.getLongestTrace() == 1 && !directlyFollowsRelation.getTauPresent()) {
			//log only has one activity
			target.setChild(index, new EventClass(log.getEventClasses().iterator().next()));
			debug("activity " + log.getEventClasses().iterator().next());
			return;
		}
		
		//exclusive choice operator
		Set<Set<XEventClass>> exclusiveChoiceCut = ExclusiveChoiceCut.findExclusiveChoiceCut(directlyFollowsRelation.getGraph());
		if (exclusiveChoiceCut.size() > 1) {
			final Binoperator node = new ExclusiveChoice(exclusiveChoiceCut.size());
			debugCut(node, exclusiveChoiceCut);
			target.setChild(index, node);
			int i = 0;
			for (Set<XEventClass> activities : exclusiveChoiceCut) {
				final Filteredlog sublog = log.applyFilter(Operator.EXCLUSIVE_CHOICE, activities);
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
		
		//sequence operator
		List<Set<XEventClass>> sequenceCut = SequenceCut.findSequenceCut(directlyFollowsRelation.getGraph());
		if (sequenceCut.size() > 1) {
			final Binoperator node = new Sequence(sequenceCut.size());
			debugCut(node, sequenceCut);
			target.setChild(index, node);
			int i = 0;
			for (Set<XEventClass> activities : sequenceCut) {
				final Filteredlog sublog = log.applyFilter(Operator.SEQUENCE, activities);
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
		
		//parallel operator
		Set<Set<XEventClass>> parallelCut = ParallelCut.findParallelCut(directlyFollowsRelation.getGraph());
		if (parallelCut.size() > 1) {
			final Binoperator node = new Parallel(parallelCut.size());
			debugCut(node, parallelCut);
			target.setChild(index, node);
			//debug("set child " + index + " of " + target.toString());
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
		List<Set<XEventClass>> loopCut = LoopCut.findLoopCut(directlyFollowsRelation);
		if (loopCut.size() > 1) {
			final Binoperator node = new Loop(loopCut.size());
			debugCut(node, loopCut);
			target.setChild(index, node);
			int i = 0;
			for (Set<XEventClass> activities : loopCut) {
				final Filteredlog sublog = log.applyFilter(Operator.LOOP, activities);
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
		
		//flower loop fall-through
		debug("chosen flower loop {" + Sets.implode(log.getEventClasses(), ", ") + "}");
		Binoperator node = new Loop(log.getEventClasses().size()+1);
		node.setChild(0, new Tau());
		int i = 1;
		for (XEventClass a : log.getEventClasses()) {
			node.setChild(i, new EventClass(a));
			i++;
		}
		target.setChild(index, node);
		return;
	}
	
	private void debugCut(Binoperator node, Collection<Set<XEventClass>> cut) {
		String r = "chosen " + node.getOperatorString();
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
