package org.processmining.plugins.flowerMiner;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

@Plugin(name = "Mine Process tree using Tau Miner", returnLabels = { "Process Tree" }, returnTypes = {
		ProcessTree.class }, parameterLabels = {}, userAccessible = true, level = PluginLevel.Regular)
public class TauMiner {

	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = {})
	public ProcessTree mine(PluginContext context) {
		return mine();
	}

	public static ProcessTree mine() {
		ProcessTree tree = new ProcessTreeImpl();
		Node root = new AbstractTask.Automatic("tau");
		root.setProcessTree(tree);
		tree.setRoot(root);
		return tree;
	}
}
