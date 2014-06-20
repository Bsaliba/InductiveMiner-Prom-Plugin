package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMiMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IMiProcessTree {
	
	public ProcessTree mineProcessTree(PluginContext context, XLog log) {
		return mineProcessTree(log);
	}
	
	public ProcessTree mineGuiPetrinet(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMi();
		IMiMiningDialog dialog = new IMiMiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine using Inductive Miner - infrequent", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
	public static ProcessTree mineProcessTree(XLog log) {
		return IMProcessTree.mineProcessTree(log, new MiningParametersIMi());
	}
	
	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(xlog, parameters);
	}
	
	public static ProcessTree mineProcessTree(IMLog log, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
}
