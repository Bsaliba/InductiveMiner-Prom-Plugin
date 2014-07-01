package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMPlaygroundMiningDialog;

@Deprecated
public class IMiPetriNet {
	
	@Deprecated
	public Object[] minePetriNet(PluginContext context, XLog log) {
		return IMPetriNet.minePetriNet(context, log, new MiningParametersIMi());
	}
	
	@Deprecated
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParametersIMi();
		IMPlaygroundMiningDialog dialog = new IMPlaygroundMiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine using Inductive Miner - infrequent", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMPetriNet.minePetriNet(context, log, parameters);
	}
	
	@Deprecated
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(context, log, parameters);
	}
	
	@Deprecated
	public static Object[] minePetriNet(XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(log, parameters);
	}
	
	
}
