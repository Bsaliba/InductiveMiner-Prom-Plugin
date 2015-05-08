package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles;

public class PreProcessLogForLifeCyclesPlugin {
	@Plugin(name = "Pre-process an event log for life cycle mining", returnLabels = { "Log" }, returnTypes = { XLog.class }, parameterLabels = { "Log" }, userAccessible = true, help = "Make all traces consistent by inserting a completion event directly after each unmatched start event.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public XLog preProcessLog(UIPluginContext context, XLog log) {
		return LifeCycles.preProcessLog(log, MiningParameters.getDefaultClassifier());
	}
}
