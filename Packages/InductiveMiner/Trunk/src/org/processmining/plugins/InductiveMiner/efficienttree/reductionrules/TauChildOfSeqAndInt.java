package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

public class TauChildOfSeqAndInt implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
			if (tree.getNumberOfChildren(node) > 1) {
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {
						//remove tau
						tree.removeChild(node, child);
						return true;
					}
				}
			}
		}
		return false;
	}

}
