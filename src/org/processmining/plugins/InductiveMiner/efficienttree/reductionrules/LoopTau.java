package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

/**
 * This reduction rule makes the tree longer. Termination is guaranteed as it is
 * reducing the number of tau's that are directly under a loop.
 * 
 * @author sleemans
 *
 */
public class LoopTau implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isLoop(node)) {
			int body = tree.getChild(node, 0);
			if (tree.isTau(body)) {

				//check whether the redo can produce something else than tau
				int redo = tree.getChild(node, 1);
				if (!EfficientTreeMetrics.canOnlyProduceTau(tree, redo)) {

					//before: loop tau redo exit

					//after: xor tau0 loop redo tau1 exit

					int exit = tree.getChild(node, 2);

					//make a new tree as we're making it longer
					int[] newTree = new int[tree.getTree().length + 2];

					//copy the part up to the node
					System.arraycopy(tree.getTree(), 0, newTree, 0, node);

					//set the xor
					newTree[node] = EfficientTree.xor - EfficientTree.childrenFactor * 2;

					//set tau0
					newTree[node + 1] = EfficientTree.tau;

					//set the node
					newTree[node + 2] = EfficientTree.loop - EfficientTree.childrenFactor * 3;

					//copy the redo part (which becomes the body)
					System.arraycopy(tree.getTree(), redo, newTree, node + 3, exit - (body + 1));

					//set tau1
					newTree[(exit - 1) + 2] = EfficientTree.tau;

					//copy the remaining part of the tree
					System.arraycopy(tree.getTree(), exit, newTree, exit + 2, newTree.length - (exit + 2));

					tree.replaceTree(newTree);
					return true;
				}
			}
		}
		return false;
	}

}