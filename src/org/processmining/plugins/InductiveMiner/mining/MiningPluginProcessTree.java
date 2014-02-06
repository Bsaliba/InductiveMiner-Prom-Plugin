package org.processmining.plugins.InductiveMiner.mining;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.ProcessTreeModelConnection;
import org.processmining.plugins.InductiveMiner.model.ProcessTreeModel;
import org.processmining.plugins.InductiveMiner.model.conversion.ProcessTreeModel2ProcessTree;
import org.processmining.processtree.ProcessTree;

public class MiningPluginProcessTree {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineProcessTreeGui(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParameters();
		MiningDialog dialog = new MiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine a Petri net using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return mineProcessTree(context, log, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, parameterized", requiredParameterLabels = { 0, 1 })
	public ProcessTree mineProcessTree(PluginContext context, XLog log, MiningParameters parameters) {
		ProcessTree tree = mineProcessTree(log, parameters);
		context.addConnection(new ProcessTreeModelConnection(log, tree, parameters));
		return tree;
	}
	
	public ProcessTree mineProcessTree(XLog log) {
		return this.mineProcessTree(log, new MiningParameters());
	}
	
	public ProcessTree mineProcessTree(XLog log, MiningParameters parameters) {
		Miner miner = new Miner();
		ProcessTreeModel model = miner.mine(log, parameters);
		ProcessTree tree = ProcessTreeModel2ProcessTree.convert(model.root);
		return tree;
	}
}
