package org.processmining.plugins.InductiveMiner.model.conversion;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.DebugProbabilities;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Create probabilities matrix from log", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = { "Log" }, userAccessible = true)
public class Log2Probabilities {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Create directly-follows graph from log, default", requiredParameterLabels = { 0 })
	public Dot log2probabilities(PluginContext context, XLog log) {
		return log2probabilities(context, log, new MiningParameters());
	}
	
	public Dot log2probabilities(PluginContext context, XLog log, MiningParameters parameters) {
		Filteredlog filteredLog = new Filteredlog(log, parameters);
		return log2probabilities(context, filteredLog, parameters);
	}
	
	public Dot log2probabilities(PluginContext context, Filteredlog filteredLog, MiningParameters parameters) {
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(filteredLog, parameters);
		Dot dot = new Dot();
		dot.append(parameters.getSatProbabilities().toString());
		dot.append("<br>\n");
		dot.append(DebugProbabilities.debug(directlyFollowsRelation, parameters, true));
		return dot;
	}
}
