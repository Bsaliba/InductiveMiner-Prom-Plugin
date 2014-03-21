package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class FallThroughFlower implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		
		Miner.debug(" fall through: flower model", minerState);
		
		Block loopNode = new AbstractBlock.XorLoop("");
		Miner.addNode(tree, loopNode, logInfo.getNumberOfTraces(), 0l, 0l, "fall through: flower model");
		
		//body: tau
		Node body = new AbstractTask.Automatic("tau");
		Miner.addNode(tree, body, logInfo.getNumberOfTraces() + logInfo.getNumberOfEvents(), 0l, 0l, "fall through: flower model");
		loopNode.addChild(body);
		
		//redo: xor/activity
		Block xorNode;
		if (logInfo.getActivities().setSize() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			Miner.addNode(tree, xorNode, logInfo.getNumberOfEvents(), 0l, 0l, "fall through: flower model");
			loopNode.addChild(xorNode);
		}
		
		for (XEventClass activity: logInfo.getActivities()) {
			Node child = new AbstractTask.Manual(activity.toString());
			Miner.addNode(tree, child, logInfo.getActivities().getCardinalityOf(activity), 0l, 0l, "fall through: flower model");
			xorNode.addChild(child);
		}
		
		Node tau2 = new AbstractTask.Automatic("tau");
		Miner.addNode(tree, tau2, logInfo.getNumberOfTraces(), 0l, 0l, "fall through: flower model");
		loopNode.addChild(tau2);
		
		return loopNode;
	}
}
