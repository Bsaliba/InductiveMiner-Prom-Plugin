package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IM {
	
	@Plugin(name = "Mine process tree with Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
	"Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, dialog.getMiningParameters());
	}
	
	@Plugin(name = "Mine Petri net with Inductive Miner", returnLabels = { "Petri net", "Initial marking", "final marking" }, returnTypes = {
			Petrinet.class, Marking.class, Marking.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMPetriNet.minePetriNet(context, log, dialog.getMiningParameters());
	}
}
