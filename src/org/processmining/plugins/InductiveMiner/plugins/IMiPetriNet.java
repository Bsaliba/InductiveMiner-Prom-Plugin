package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMiMiningDialog;

public class IMiPetriNet {
	
	public Object[] minePetriNet(PluginContext context, XLog log) {
		return IMPetriNet.minePetriNet(context, log, new MiningParametersIMi());
	}
	
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMi();
		IMiMiningDialog dialog = new IMiMiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine using Inductive Miner - infrequent", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMPetriNet.minePetriNet(context, log, parameters);
	}
	
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(context, log, parameters);
	}
	
	public static Object[] minePetriNet(XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(log, parameters);
	}
	
	
}
