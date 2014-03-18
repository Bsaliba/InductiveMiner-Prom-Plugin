package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class FallThroughFlower implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		
		Miner.debug(" fall through: flower model", minerState);
		
		Block loopNode = new AbstractBlock.XorLoop("");
		loopNode.setProcessTree(tree);
		MinerMetrics.attachNumberOfTracesRepresented(loopNode, logInfo);
		MinerMetrics.attachMovesOnLog(loopNode, (long) 0);
		MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(loopNode, (long) 0);
		MinerMetrics.attachProducer(loopNode, "fall through: flower model");
		
		//body: tau
		Node body = new AbstractTask.Automatic("tau");
		body.setProcessTree(tree);
		loopNode.addChild(body);
		//count the number of times this tau was used
		MinerMetrics.attachNumberOfTracesRepresented(body, logInfo.getNumberOfTraces() + logInfo.getNumberOfEvents());
		MinerMetrics.attachMovesOnLog(body, (long) 0);
		MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(body, (long) 0);
		MinerMetrics.attachProducer(body, "fall through: flower model");
		
		//redo: xor/activity
		Block xorNode;
		if (logInfo.getActivities().setSize() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			xorNode.setProcessTree(tree);
			loopNode.addChild(xorNode);
			MinerMetrics.attachNumberOfTracesRepresented(xorNode, logInfo.getNumberOfEvents());
			MinerMetrics.attachMovesOnLog(xorNode, (long) 0);
			MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(xorNode, (long) 0);
			MinerMetrics.attachProducer(xorNode, "fall through: flower model");
		}
		
		for (XEventClass activity: logInfo.getActivities()) {
			Node child = new AbstractTask.Manual(activity.toString());
			child.setProcessTree(tree);
			xorNode.addChild(child);
			
			MinerMetrics.attachNumberOfTracesRepresented(child, logInfo.getActivities().getCardinalityOf(activity));
			MinerMetrics.attachMovesOnLog(child, (long) 0);
			MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(child, (long) 0);
			MinerMetrics.attachProducer(child, "fall through: flower model");
		}
		
		Node tau2 = new AbstractTask.Automatic("tau");
		tau2.setProcessTree(tree);
		loopNode.addChild(tau2);
		MinerMetrics.attachNumberOfTracesRepresented(tau2, logInfo);
		MinerMetrics.attachMovesOnLog(tau2, (long) 0);
		MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(tau2, (long) 0);
		MinerMetrics.attachProducer(tau2, "fall through: flower model");
		
		return loopNode;
	}
	
}
