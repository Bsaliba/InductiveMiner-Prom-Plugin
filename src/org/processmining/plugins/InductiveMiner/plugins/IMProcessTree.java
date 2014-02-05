package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.Miner2;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.model.conversion.ReduceTree;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Mine Process Tree with Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log" }, userAccessible = true)
public class IMProcessTree {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public ProcessTree mineProcessTree(PluginContext context, XLog log) {
		return mineProcessTree(log);
	}
	
	public ProcessTree mineProcessTree(XLog log) {
		return mineProcessTree(log, new MiningParametersIM());
	}
	
	public ProcessTree mineProcessTree(XLog log, MiningParameters parameters) {
		ProcessTree tree = Miner2.mine(log, parameters);
		return ReduceTree.reduceTree(tree);
	}
}
