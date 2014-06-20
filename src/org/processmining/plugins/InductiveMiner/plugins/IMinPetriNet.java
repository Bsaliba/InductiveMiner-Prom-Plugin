package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;

public class IMinPetriNet {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Petri Net", requiredParameterLabels = { 0 })
	public Object[] minePetriNet(PluginContext context, XLog log) {
		return IMPetriNet.minePetriNet(context, log, new MiningParametersIMi());
	}
	
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(context, log, parameters);
	}
	
	public static Object[] minePetriNet(XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(log, parameters);
	}
	
	
}
