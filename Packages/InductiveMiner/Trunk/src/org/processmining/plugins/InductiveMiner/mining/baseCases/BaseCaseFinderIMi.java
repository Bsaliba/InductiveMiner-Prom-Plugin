package org.processmining.plugins.InductiveMiner.mining.baseCases;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class BaseCaseFinderIMi implements BaseCaseFinder {

	private static List<BaseCaseFinder> baseCaseFinders = new LinkedList<BaseCaseFinder>(Arrays.asList(
			new BaseCaseFinderIMiEmptyLog(),
			new BaseCaseFinderIMiEmptyTrace(),
			new BaseCaseFinderIMiSingleActivity()
			));
	
	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {

		Node n = null;
		Iterator<BaseCaseFinder> it = baseCaseFinders.iterator();
		while (n == null && it.hasNext()) {
			n = it.next().findBaseCases(log, logInfo, tree, parameters);
		}
		return n;
	}

}
