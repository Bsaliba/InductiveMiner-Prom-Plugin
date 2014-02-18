package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiEmptyLog implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		if (logInfo.getNumberOfEvents() == 0) {
			//empty log, return tau

			Node node = new AbstractTask.Automatic("tau");
			node.setProcessTree(tree);
			MinerMetrics.attachStatistics(node, 0);

			return node;
		}
		return null;
	}

}
