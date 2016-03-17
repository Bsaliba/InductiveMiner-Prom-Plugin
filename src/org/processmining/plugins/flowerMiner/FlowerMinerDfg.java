package org.processmining.plugins.flowerMiner;

import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.hash.TObjectShortHashMap;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.processtree.ProcessTree;

public class FlowerMinerDfg {
	@Plugin(name = "Mine a Process tree using Flower Miner - dfg", returnLabels = { "Process tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Directly-follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = { 0 })
	public ProcessTree mine(PluginContext context, Dfg dfg) {
		return EfficientTree2processTree.convert(mine(dfg));
	}

	public static EfficientTree mine(Dfg dfg) {
		XEventClass[] vertices = dfg.getDirectlyFollowsGraph().getVertices();

		//construct activity structures
		String[] short2activity = new String[vertices.length];
		TObjectShortMap<String> activity2short = new TObjectShortHashMap<>();
		for (short i = 0; i < vertices.length; i++) {
			short2activity[i] = vertices[i].getId();
			activity2short.put(vertices[i].getId(), i);
		}

		//construct the tree
		short[] tree = new short[short2activity.length + 4];
		tree[0] = EfficientTree.loop - 3 * EfficientTree.childrenFactor;
		tree[1] = (short) (EfficientTree.xor - short2activity.length * EfficientTree.childrenFactor);
		for (short i = 0; i < short2activity.length; i++) {
			tree[2 + i] = i;
		}
		tree[2 + short2activity.length] = EfficientTree.tau;
		tree[3 + short2activity.length] = EfficientTree.tau;
		
		return new EfficientTree(tree, activity2short, short2activity);
	}
}
