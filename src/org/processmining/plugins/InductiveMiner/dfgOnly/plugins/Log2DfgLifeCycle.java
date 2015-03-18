package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;

public class Log2DfgLifeCycle {
	@Plugin(name = "Convert log to directly-follows graph using lifecycle", returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel(
				"Directly-follows graph of " + XConceptExtension.instance().extractName(log));
		return IMLog2IMLogInfoLifeCycle.log2Dfg(new IMLog(log, MiningParameters.getDefaultClassifier()));
	}

	public static Dfg log2Dfg(IMLog2 log) {
		return IMLog2IMLogInfoLifeCycle.log2Dfg(log);
	}

}
