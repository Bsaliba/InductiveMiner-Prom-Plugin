package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

public class SingleChild implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isOperator(node) && tree.getNumberOfChildren(node) == 1) {
			//remove this node
			System.arraycopy(tree.getTree(), node + 1, tree.getTree(), node, tree.getTree().length - node - 1);
			tree.getTree()[tree.getTree().length - 1] = EfficientTree.skip;
			return true;
		}
		return false;
	}
}
