package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.Iterator;

public class EfficientTreeUtils {
	/**
	 * 
	 * @param parent
	 * @param child
	 * @return Whether the child is a direct or indirect child of parent.
	 */
	public static boolean isParentOf(EfficientTree tree, int parent, int child) {
		if (parent > child) {
			return false;
		}
		return tree.traverse(parent) > child;
	}

	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The child of parent that contains grandChild. If grandChild is
	 *         not a child of parent, will return -1.
	 */
	public static int getChildWith(EfficientTree tree, int parent, int grandChild) {
		for (int child : tree.getChildren(parent)) {
			if (isParentOf(tree, child, grandChild)) {
				return child;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The number of the child within parent that contains grandChild.
	 *         If grandChild is not a child of parent, will return -1.
	 */
	public static int getChildNumberWith(EfficientTree tree, int parent, int grandChild) {
		int childNumber = 0;
		for (int child : tree.getChildren(parent)) {
			if (isParentOf(tree, child, grandChild)) {
				return childNumber;
			}
			childNumber++;
		}
		return -1;
	}

	/**
	 * 
	 * @param tree
	 * @param nodeA
	 * @param nodeB
	 * @return The node that is a parent of both nodeA and nodeB, or is nodeA or
	 *         nodeB itself.
	 */
	public static int getLowestCommonParent(EfficientTree tree, int nodeA, int nodeB) {
		if (nodeA > nodeB) {
			return getLowestCommonParent(tree, nodeB, nodeA);
		}
		if (nodeA == nodeB) {
			return nodeA;
		}

		int lowestCommonParent = nodeA;
		while (!isParentOf(tree, lowestCommonParent, nodeB)) {
			lowestCommonParent = tree.getParent(lowestCommonParent);
		}
		return lowestCommonParent;
	}
	
	public static Iterable<Integer> getAllNodes(EfficientTree tree) {
		return getAllNodes(tree, tree.getRoot());
	}

	public static Iterable<Integer> getAllNodes(final EfficientTree tree, final int child) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = child - 1;

					public int findNext() {
						int next = now + 1;
						while (next < tree.getTree().length && tree.getTree()[next] == EfficientTree.skip) {
							next++;
						}
						if (next == tree.getTree().length) {
							return -1;
						}
						return next;
					}

					public Integer next() {
						now = findNext();
						return now;
					}

					public boolean hasNext() {
						return findNext() != -1;
					}

					public void remove() {

					}
				};
			}
		};
	}
}
