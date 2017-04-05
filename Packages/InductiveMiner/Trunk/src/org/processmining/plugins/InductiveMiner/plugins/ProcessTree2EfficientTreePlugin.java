package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class ProcessTree2EfficientTreePlugin {
	@Plugin(name = "convert process tree to efficient tree", returnLabels = { "Efficient Tree" }, returnTypes = {
			EfficientTree.class }, parameterLabels = {
					"Process tree" }, userAccessible = true, help = "Convert a process into an efficient tree.", level = PluginLevel.BulletProof)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public EfficientTree convert(PluginContext context, ProcessTree tree) {
		return new EfficientTree(tree);
	}
}
