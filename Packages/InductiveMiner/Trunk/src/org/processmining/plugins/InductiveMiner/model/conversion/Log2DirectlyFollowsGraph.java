package org.processmining.plugins.InductiveMiner.model.conversion;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Create directly-follows graph from log", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = { "Log" }, userAccessible = true)
public class Log2DirectlyFollowsGraph {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Create directly-follows graph from log, default", requiredParameterLabels = { 0 })
	public Dot log2directlyFollowsGraph(PluginContext context, XLog log) {
		return log2directlyFollowsGraph(context, log, new MiningParameters());
	}

	public Dot log2directlyFollowsGraph(PluginContext context, XLog log, MiningParameters parameters) {
		Filteredlog filteredLog = new Filteredlog(log, parameters);
		return log2directlyFollowsGraph(context, filteredLog, parameters);
	}
	
	public Dot log2directlyFollowsGraph(PluginContext context, Filteredlog filteredLog, MiningParameters parameters) {
		DirectlyFollowsRelation directlyFollowsRelation = new DirectlyFollowsRelation(filteredLog, parameters);
		Dot dot = new Dot();
		dot.append(directlyFollowsRelation.toDot(false));
		return dot;
	}
}
