package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.processtree.ProcessTree;

public class IMProcessTree {
	
	public ProcessTree mineProcessTree(PluginContext context, XLog xlog) {
		return mineProcessTree(xlog);
	}
	
	public static ProcessTree mineProcessTree(XLog xlog) {
		return mineProcessTree(xlog, new MiningParametersIM());
	}
	
	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters) {
		//prepare the log
		IMLog2 log = new IMLog2(xlog, parameters.getClassifier());
		return mineProcessTree(log, parameters);
	}
	
	public static ProcessTree mineProcessTree(IMLog2 log, MiningParameters parameters) {
		return Miner.mine(log, parameters);
	}
}
