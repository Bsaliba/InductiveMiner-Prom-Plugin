package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class And2Or implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isConcurrent(node)) {
			TIntList childrenThatCanProduceTau = new TIntArrayList();
			for (int child : tree.getChildren(node)) {
				if (EfficientTreeMetrics.canProduceTau(tree, child)) {
					childrenThatCanProduceTau.add(child);
				}
			}
			if (childrenThatCanProduceTau.size() < 2) {
				return false;
			}

			//there are more than two children that can produce tau

			//before: and ... Q2 ... Q4 ... skip skip skip

			//find the number of skips at the end
			int numberOfSkips = 0;
			for (int i = tree.getTree().length - 1; i >= 0; i--) {
				if (!tree.isSkip(i)) {
					break;
				}
				numberOfSkips++;
			}

			int[] newTree = new int[(tree.getTree().length + 3) - numberOfSkips];

			//copy the part up to the node
			System.arraycopy(tree.getTree(), 0, newTree, 0, node);

			//set the number of children of and
			newTree[node] = EfficientTree.concurrent - EfficientTree.childrenFactor
					* (tree.getNumberOfChildren(node) - (childrenThatCanProduceTau.size() - 1));

			int pos = node + 1;
			for (int child : tree.getChildren(node)) {
				int childLength = tree.traverse(child) - child;
				if (!childrenThatCanProduceTau.contains(child)) {
					//this child should stay in place, however it should be shifted to the left (pos)
					System.arraycopy(tree.getTree(), child, newTree, pos, childLength);
					pos += childLength;
				}
			}

			//set the xor
			newTree[pos] = EfficientTree.xor - EfficientTree.childrenFactor * 2;

			//set the tau
			newTree[pos + 1] = EfficientTree.tau;

			//set the or
			newTree[pos + 2] = EfficientTree.or - EfficientTree.childrenFactor * childrenThatCanProduceTau.size();

			pos += 3;

			for (int child : childrenThatCanProduceTau.toArray()) {
				int childLength = tree.traverse(child) - child;
				System.arraycopy(tree.getTree(), child, newTree, pos, childLength);
				pos += childLength;
			}

			//copy the part after the node
			int afterNode = tree.traverse(node);
			System.arraycopy(tree.getTree(), afterNode, newTree, pos, newTree.length - pos);

			tree.replaceTree(newTree);

			return true;
		}
		return false;
	}
}
