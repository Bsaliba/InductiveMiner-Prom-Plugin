package org.processmining.plugins.InductiveMiner.plugins;

import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IM {

	@Plugin(name = "Mine process tree with Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Log" }, userAccessible = true)
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

	@Plugin(name = "Mine Petri net with Inductive Miner", returnLabels = { "Petri net", "Initial marking",
			"final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = { "Log" }, userAccessible = true)
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

	@Plugin(name = "Mine Process tree with Inductive Miner, with parameters", returnLabels = { "Process tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
			"Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static ProcessTree mineProcessTree(PluginContext context, XLog log, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(log, parameters);
	}

	@Plugin(name = "Mine Petri net with Inductive Miner, with parameters", returnLabels = { "Petri net",
			"Initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
			"Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(context, log, parameters);
	}

	public static XEventClassifier[] getClassifiers(XLog log) {
		List<XEventClassifier> logClassifiers = log.getClassifiers();
		XEventClassifier[] result = new XEventClassifier[logClassifiers.size() + 1];
		result[0] = new XEventNameClassifier();
		for (int i = 0; i < logClassifiers.size(); i++) {
			result[i+1] = logClassifiers.get(i);
		}
		return result;
	}
}
