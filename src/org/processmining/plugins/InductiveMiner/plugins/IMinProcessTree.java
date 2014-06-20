package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Mine Process Tree with Inductive Miner-incompleteness", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log" }, userAccessible = false)
public class IMinProcessTree {
	
	public ProcessTree mineProcessTree(PluginContext context, XLog log) {
		return mineProcessTree(log);
	}
	
	public static ProcessTree mineProcessTree(XLog log) {
		return IMProcessTree.mineProcessTree(log, new MiningParametersIMin());
	}
	
	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(xlog, parameters);
	}
	
	public static ProcessTree mineProcessTree(IMLog log, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
}
