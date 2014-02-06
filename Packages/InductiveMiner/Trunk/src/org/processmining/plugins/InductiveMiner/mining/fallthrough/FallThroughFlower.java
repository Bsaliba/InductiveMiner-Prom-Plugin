package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class FallThroughFlower implements FallThrough {

	public Node fallThrough(IMLog log, LogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		Block loopNode = new AbstractBlock.XorLoop("");
		loopNode.setProcessTree(tree);
		
		Block xorNode;
		if (logInfo.getActivities().size() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			xorNode.setProcessTree(tree);
			loopNode.addChild(xorNode);
		}
		
		for (XEventClass activity: logInfo.getActivities()) {
			Node child = new AbstractTask.Manual(activity.toString());
			child.setProcessTree(tree);
			xorNode.addChild(child);
		}
		
		Node tau1 = new AbstractTask.Automatic("");
		tau1.setProcessTree(tree);
		loopNode.addChild(tau1);
		
		Node tau2 = new AbstractTask.Automatic("");
		tau2.setProcessTree(tree);
		loopNode.addChild(tau2);
		
		return loopNode;
	}
	
}
