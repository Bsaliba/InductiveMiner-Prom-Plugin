package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.IMdMiningDialog;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IMd {
	@Plugin(name = "Mine process tree with Inductive Miner - directly-follows  ", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Direclty-follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public ProcessTree mineProcessTree(UIPluginContext context, Dfg dfg) {
		IMdMiningDialog dialog = new IMdMiningDialog();
		InteractionResult result = context.showWizard("Mine using Inductive Miner - directly-follows", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMdProcessTree.mineProcessTree(dfg, dialog.getMiningParameters());
	}
}
