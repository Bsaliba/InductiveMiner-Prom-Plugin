package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
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
	
	public FallThroughLeaveOutActivitiesThenApplyOthers() {
		
	}
	
	public Node fallThrough(IMLog2 log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		
		if (logInfo.getActivities().toSet().size() < 3) {
			return null;
		}
		
		//leave out an activity
		DfgCutFinder dfgCutFinder = new DfgCutFinderSimple();
		for (XEventClass leaveOutActivity: logInfo.getActivities()) {
			
			//in a typical overcomplicated java-way, create a cut (parallel, [{a}, Sigma\{a}])
			Set<XEventClass> leaveOutSet = new THashSet<XEventClass>();
			leaveOutSet.add(leaveOutActivity);
			List<Set<XEventClass>> partition = new ArrayList<Set<XEventClass>>();
			partition.add(leaveOutSet);
			partition.add(Sets.complement(leaveOutSet, logInfo.getActivities().toSet()));
			Cut cut = new Cut(Operator.parallel, partition);
			
			Miner.debug("  try cut " + cut, minerState);

			//see if a cut applies
			//for performance reasons, only on the directly-follows graph
			Cut cut2 = dfgCutFinder.findCut(logInfo.getDfg(), null);
			if (cut2 != null && cut2.isValid()) {
				//the cut we made is a valid one; split the log, construct the parallel construction and recurse
				
				Miner.debug(" fall through: leave out activity " + leaveOutActivity, minerState);
				
				LogSplitResult logSplitResult = minerState.parameters.getLogSplitter().split(log, logInfo, cut, minerState);
				IMLog2 log1 = logSplitResult.sublogs.get(0);
				IMLog2 log2 = logSplitResult.sublogs.get(1);
				
				Block newNode = new AbstractBlock.And("");
				Miner.addNode(tree, newNode);
				
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
