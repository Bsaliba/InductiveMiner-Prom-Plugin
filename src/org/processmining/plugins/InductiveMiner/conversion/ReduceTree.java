package org.processmining.plugins.InductiveMiner.conversion;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Reduce process tree language-equivalently", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Process Tree" }, userAccessible = true)
public class ReduceTree {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Reduce Process Tree Language-equivalently, default", requiredParameterLabels = { 0 })
	public ProcessTree reduceTree(PluginContext context, ProcessTree tree) {
		return reduceTree(tree);
	}
	
	public static void reduceChildrenOf(Block node) {
		for (Node child : node.getChildren()) {
			//convert child to an efficient tree
			EfficientTree partialTree = new EfficientTree(child);
			try {
				EfficientTreeReduce.reduce(partialTree);
			} catch (ReductionFailedException e) {
				return;
			}
			EfficientTree2processTree.replaceNode(child, partialTree);
		}
	}

	public static ProcessTree reduceTree(ProcessTree tree) {
		EfficientTree efficientTree = new EfficientTree(tree);
		try {
			EfficientTreeReduce.reduce(efficientTree);
			return EfficientTree2processTree.convert(efficientTree);
		} catch (Exception e) {
			return tree;
		}
	}
}
