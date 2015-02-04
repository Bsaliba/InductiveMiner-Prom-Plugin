package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIM implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().setSize() == 1 && logInfo.getNumberOfEpsilonTraces() == 0
				&& logInfo.getNumberOfEvents() == log.size()) {
			//single activity
			
			Miner.debug(" base case: IM single activity", minerState);
			
			XEventClass activity = logInfo.getActivities().iterator().next();
			Node node = new AbstractTask.Manual(activity.toString());
			Miner.addNode(tree, node);
			
			return node;
		} else if (logInfo.getActivities().setSize() == 0) {
			//empty log
			
			Miner.debug(" base case: IM empty log", minerState);
			
			Node node = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, node);
			
			return node;
		}

		return null;
	}

}
