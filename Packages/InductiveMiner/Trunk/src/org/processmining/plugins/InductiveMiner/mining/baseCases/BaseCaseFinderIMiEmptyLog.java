package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiEmptyLog implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, LogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		if (logInfo.getNumberOfEvents() == 0) {
			//empty log, return tau

			Node node = new AbstractTask.Automatic("");
			node.setProcessTree(tree);

			return node;
		}
		return null;
	}

}
