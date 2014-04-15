package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMStateOfArt;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMiMiningDialog;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Mine Process Tree with Inductive Miner-state of art", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log" }, userAccessible = true)
public class IMStateOfArtProcessTree {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Petri net, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMStateOfArt();
		IMiMiningDialog dialog = new IMiMiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine using Inductive Miner - state of art", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
}
