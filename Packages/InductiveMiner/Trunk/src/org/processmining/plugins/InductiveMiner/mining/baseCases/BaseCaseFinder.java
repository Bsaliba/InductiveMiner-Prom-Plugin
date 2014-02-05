package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface BaseCaseFinder {
	
	/*
	 * usage: if there is no base case in this log, returns null
	 * if there is a base case, returns a Node. 
	 * Each (in)direct child of that Node must be attached to tree.
	 * 
	 * Must be thread-safe and abstract, i.e, no side-effects allowed.
	 */
	
	public Node findBaseCases(IMLog log, LogInfo logInfo, ProcessTree tree, MiningParameters parameters);
}
