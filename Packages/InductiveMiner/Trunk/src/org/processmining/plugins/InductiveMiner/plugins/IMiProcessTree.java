package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMPlaygroundMiningDialog;
import org.processmining.processtree.ProcessTree;

@Deprecated
public class IMiProcessTree {
	
	@Deprecated
	public ProcessTree mineProcessTree(PluginContext context, XLog log) {
		return mineProcessTree(log);
	}
	
	@Deprecated
	public ProcessTree mineGuiPetrinet(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMi();
		IMPlaygroundMiningDialog dialog = new IMPlaygroundMiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine using Inductive Miner - infrequent", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
	@Deprecated
	public static ProcessTree mineProcessTree(XLog log) {
		return IMProcessTree.mineProcessTree(log, new MiningParametersIMi());
	}
	
	@Deprecated
	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(xlog, parameters);
	}
	
	@Deprecated
	public static ProcessTree mineProcessTree(IMLog log, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(log, parameters);
	}
	
}
