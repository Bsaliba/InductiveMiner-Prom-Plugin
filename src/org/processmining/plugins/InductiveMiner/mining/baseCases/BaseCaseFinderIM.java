package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIM implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {

		if (logInfo.getActivities().setSize() == 1 && logInfo.getNumberOfEpsilonTraces() == 0
				&& logInfo.getNumberOfEvents() == logInfo.getNumberOfTraces()) {
			Node node = new AbstractTask.Manual(logInfo.getActivities().iterator().next().toString());
			node.setProcessTree(tree);
			return MinerMetrics.attachStatistics(node, logInfo);
		} else if (logInfo.getActivities().setSize() == 0) {
			Node node = new AbstractTask.Automatic("");
			node.setProcessTree(tree);
			return MinerMetrics.attachStatistics(node, logInfo);
		}

		return null;
	}

}
