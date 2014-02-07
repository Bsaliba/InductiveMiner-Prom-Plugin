package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dot.IMLogInfo2Dot;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Create directly-follows graph from log", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = { "Log" }, userAccessible = true)
public class Log2DirectlyFollowsGraph {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Create directly-follows graph from log, default", requiredParameterLabels = { 0 })
	public Dot log2directlyFollowsGraph(PluginContext context, XLog log) {
		return log2directlyFollowsGraph(log, MiningParameters.getDefaultClassifier());
	}

	public Dot log2directlyFollowsGraph(XLog log, XEventClassifier classifier) {
		IMLog internalLog = new IMLog(log, classifier);
		IMLogInfo logInfo = new IMLogInfo(internalLog);
		return IMLogInfo2Dot.toDot(logInfo, false);
	}
}
