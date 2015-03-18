package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class FallThroughFlower implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		
		Miner.debug(" fall through: flower model", minerState);
		
		Block loopNode = new AbstractBlock.XorLoop("");
		Miner.addNode(tree, loopNode);
		
		//body: tau
		Node body = new AbstractTask.Automatic("tau");
		Miner.addNode(tree, body);
		loopNode.addChild(body);
		
		//redo: xor/activity
		Block xorNode;
		if (logInfo.getActivities().setSize() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			Miner.addNode(tree, xorNode);
			loopNode.addChild(xorNode);
		}
		
		for (XEventClass activity: logInfo.getActivities()) {
			Node child = new AbstractTask.Manual(activity.toString());
			Miner.addNode(tree, child);
			xorNode.addChild(child);
		}
		
		Node tau2 = new AbstractTask.Automatic("tau");
		Miner.addNode(tree, tau2);
		loopNode.addChild(tau2);
		
		return loopNode;
	}
}
