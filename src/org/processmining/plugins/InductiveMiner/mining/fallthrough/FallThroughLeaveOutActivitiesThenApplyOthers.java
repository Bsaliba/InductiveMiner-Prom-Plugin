package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;

public class FallThroughLeaveOutActivitiesThenApplyOthers implements FallThrough {
	
	/*
	 * (non-Javadoc)
	 * @see org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough#fallThrough(org.processmining.plugins.InductiveMiner.mining.IMLog, org.processmining.plugins.InductiveMiner.mining.IMLogInfo, org.processmining.processtree.ProcessTree, org.processmining.plugins.InductiveMiner.mining.MiningParameters)
	 * 
	 * Try to leave out an activity and recurse
	 * If this works, then putting the left out activity in parallel is fitness-preserving
	 */
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		
		if (logInfo.getActivities().toSet().size() < 3) {
			return null;
		}
		
		//leave out an activity
		for (XEventClass leaveOutActivity: logInfo.getActivities()) {
			
			//in a typical overcomplicated java-way, create a cut (parallel, [{a}, Sigma\{a}])
			Set<XEventClass> leaveOutSet = new HashSet<XEventClass>();
			leaveOutSet.add(leaveOutActivity);
			List<Set<XEventClass>> partition = new LinkedList<Set<XEventClass>>();
			partition.add(leaveOutSet);
			partition.add(Sets.complement(leaveOutSet, logInfo.getActivities().toSet()));
			Cut cut = new Cut(Operator.parallel, partition);
			
			//Miner.debug("  try cut " + cut, parameters);
			
			//split the log
			LogSplitResult logSplitResult = minerState.parameters.getLogSplitter().split(log, logInfo, cut, minerState);
			IMLog log1 = logSplitResult.sublogs.get(0);
			IMLog log2 = logSplitResult.sublogs.get(1);
			
			//create logInfos
			IMLogInfo logInfo2 = new IMLogInfo(log2);
			
			//see if a cut applies
			Cut cut2 = Miner.findCut(log2, logInfo2, minerState);
			if (cut2 != null && cut2.isValid()) {
				//the cut we made is a valid one, construct the parallel construction and recurse
				
				Miner.debug(" fall through: leave out activity " + leaveOutActivity, minerState);
				
				Block newNode = new AbstractBlock.And("");
				newNode.setProcessTree(tree);
				MinerMetrics.attachNumberOfTracesRepresented(newNode, logInfo);
				MinerMetrics.attachMovesOnLog(newNode, (long) 0);
				MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(newNode, (long) 0);
				MinerMetrics.attachProducer(newNode, "fall through: leave out activity");
				
				Node child1 = Miner.mineNode(log1, tree, minerState);
				newNode.addChild(child1);
				
				Node child2 = Miner.mineNode(log2, tree, minerState);
				newNode.addChild(child2);
				
				return newNode;
			}
		}
		
		return null;
	}
	
}
