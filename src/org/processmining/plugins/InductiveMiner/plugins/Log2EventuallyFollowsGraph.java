package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dot.LogInfo2Dot;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Create eventually-follows graph from log", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = { "Log" }, userAccessible = true)
public class Log2EventuallyFollowsGraph {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Create eventually-follows graph from log, default", requiredParameterLabels = { 0 })
	public Dot log2eventuallyFollowsGraph(PluginContext context, XLog log) {
		return log2eventuallyFollowsGraph(log, MiningParameters.getDefaultClassifier());
	}

	public Dot log2eventuallyFollowsGraph(XLog log, XEventClassifier classifier) {
		IMLog internalLog = new IMLog(log, classifier);
		IMLogInfo logInfo = new IMLogInfo(internalLog);
		return LogInfo2Dot.toDot(logInfo, true);
	}
}
