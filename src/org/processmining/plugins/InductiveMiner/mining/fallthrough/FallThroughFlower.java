package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class FallThroughFlower implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		
		Miner.debug(" fall through: flower model", parameters);
		
		Block loopNode = new AbstractBlock.XorLoop("");
		loopNode.setProcessTree(tree);
		
		//body: tau
		Node body = new AbstractTask.Automatic("");
		body.setProcessTree(tree);
		loopNode.addChild(body);
		//count the number of times this tau was used
		MinerMetrics.attachStatistics(body, logInfo.getNumberOfTraces() + logInfo.getNumberOfEvents());
		
		//redo: xor/activity
		Block xorNode;
		if (logInfo.getActivities().setSize() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			xorNode.setProcessTree(tree);
			loopNode.addChild(xorNode);
			MinerMetrics.attachStatistics(xorNode, logInfo.getNumberOfEvents());
		}
		
		for (XEventClass activity: logInfo.getActivities()) {
			Node child = new AbstractTask.Manual(activity.toString());
			child.setProcessTree(tree);
			xorNode.addChild(child);
			
			MinerMetrics.attachStatistics(child, logInfo.getActivities().getCardinalityOf(activity));
		}
		
		Node tau2 = new AbstractTask.Automatic("");
		tau2.setProcessTree(tree);
		loopNode.addChild(tau2);
		
		return MinerMetrics.attachStatistics(loopNode, logInfo);
	}
	
}
