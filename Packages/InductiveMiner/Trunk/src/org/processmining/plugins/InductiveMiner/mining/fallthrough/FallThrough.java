package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface FallThrough {
	
	/*
	 * usage: returns a Node.
	 * Each (in)direct child of that Node must be attached to tree.
	 * 
	 * Must be thread-safe and abstract, i.e, no side-effects allowed.
	 */
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters);
}
