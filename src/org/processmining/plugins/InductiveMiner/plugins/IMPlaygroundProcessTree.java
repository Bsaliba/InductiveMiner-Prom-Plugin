package org.processmining.plugins.InductiveMiner.plugins;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMPlayground;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMiMiningDialog;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Mine Process Tree with Inductive Miner-playground", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log" }, userAccessible = false)
public class IMPlaygroundProcessTree {
	
	public ProcessTree mineGuiProcessTree(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMPlayground();
		IMiMiningDialog dialog = new IMiMiningDialog(log, parameters);
		
		JOptionPane.showMessageDialog(dialog, "Please note that this is a plug-in to test new ideas.\nIt can change without notice.");
		
		InteractionResult result = context.showWizard("Mine using Inductive Miner - playground", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
}
