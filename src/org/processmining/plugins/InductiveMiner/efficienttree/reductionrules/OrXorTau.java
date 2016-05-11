package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class OrXorTau implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isOr(node)) {
			
			//one child is an xor
			for (int xorChild : tree.getChildren(node)) {
				if (tree.isXor(xorChild)) {
					
					//one child is a tau
					for (int tauChild : tree.getChildren(xorChild)) {
						if (tree.isTau(tauChild)) {
							//remove the tau
							tree.removeChild(xorChild, tauChild);
							
							//put node in xor tau
							int[] newTree = new int[tree.getTree().length + 2];
							
							//copy the part up to the node
							System.arraycopy(tree.getTree(), 0, newTree, 0, node);
							
							//set the xor
							newTree[node] = EfficientTree.xor - EfficientTree.childrenFactor * 2;
							
							//set the tau
							newTree[node + 1] = EfficientTree.tau;
							
							//copy the remaining part
							System.arraycopy(tree.getTree(), node, newTree, node + 2, tree.getTree().length - node);
							
							tree.replaceTree(newTree);
							
							return true;
						}
					}
					
				}
			}
		}
		return false;
	}

}
