package org.processmining.plugins.flowerMiner;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

import gnu.trove.map.TObjectIntMap;

public class FlowerMinerDfg {
	@Plugin(name = "Mine process tree using Flower Miner - directly follows", returnLabels = { "Process tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Directly-follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = { 0 })
	public ProcessTree mine(PluginContext context, Dfg dfg) {
		return EfficientTree2processTree.convert(mine(dfg.getActivities()));
	}

	public static EfficientTree mine(XEventClass[] vertices) {

		//construct activity structures
		String[] int2activity = new String[vertices.length];
		TObjectIntMap<String> activity2int = EfficientTree.getEmptyActivity2int();
		for (int i = 0; i < vertices.length; i++) {
			int2activity[i] = vertices[i].getId();
			activity2int.put(vertices[i].getId(), i);
		}

		//construct the tree
		int[] tree = new int[int2activity.length + 4];
		tree[0] = EfficientTree.loop - 3 * EfficientTree.childrenFactor;
		assert(tree[0] < 0);
		tree[1] = EfficientTree.xor - int2activity.length * EfficientTree.childrenFactor;
		assert(tree[1] < 0);
		for (int i = 0; i < int2activity.length; i++) {
			tree[2 + i] = i;
		}
		tree[2 + int2activity.length] = EfficientTree.tau;
		tree[3 + int2activity.length] = EfficientTree.tau;
		
		return new EfficientTree(tree, activity2int, int2activity);
	}
}
