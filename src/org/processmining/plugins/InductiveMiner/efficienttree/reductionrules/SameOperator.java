package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

public class SameOperator implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isXor(node) || tree.isSequence(node) || tree.isConcurrent(node) || tree.isOr(node)) {
			int operator = tree.getOperator(node);

			for (int child : tree.getChildren(node)) {
				if (tree.isOperator(child) && tree.getOperator(child) == operator) {
					//before: op( op2( A, B ), ...)
					//after:  op( A, B, ...)
					
					int numberOfChildren2 = tree.getNumberOfChildren(child);
					
					//remove op2
					tree.getTree()[child] = EfficientTree.tau;
					tree.removeChild(node, child);
					
					//correct the number of children of node
					tree.getTree()[node] -= numberOfChildren2 * EfficientTree.childrenFactor;
					return true;
				}
			}
		}

		return false;
	}

}
