package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.DebugProbabilities;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Create probabilities matrix from log", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = { "Log" }, userAccessible = true)
public class Log2Probabilities {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Create directly-follows graph from log, default", requiredParameterLabels = { 0 })
	public Dot log2probabilities(PluginContext context, XLog log) {
		return log2probabilities(log);
	}
	
	public Dot log2probabilities(XLog log) {
		MiningParameters parameters = new MiningParametersIMin();
		IMLog log2 = new IMLog(log, parameters.getClassifier());
		IMLogInfo logInfo = new IMLogInfo(log2);
		Dot dot = new Dot();
		dot.append(parameters.getSatProbabilities().toString());
		dot.append("<br>\n");
		dot.append(DebugProbabilities.debug(logInfo, parameters, true));
		return dot;
	}
}
