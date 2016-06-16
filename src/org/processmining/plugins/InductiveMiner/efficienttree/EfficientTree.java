package org.processmining.plugins.InductiveMiner.efficienttree;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Or;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;

/**
 * Class to store a process tree memory efficient and perform operations cpu
 * efficient.
 * 
 * Idea: keep an array of int. An activity is a >= 0 value. A node is a negative
 * value. Some bits encode the operator, the other bits the number of children.
 * 
 * @author sleemans
 *
 */
public class EfficientTree {

	private int[] tree;
	private final TObjectIntMap<String> activity2int;
	private final String[] int2activity;

	public static final int tau = -1;
	public static final int xor = -2;
	public static final int sequence = -3;
	public static final int concurrent = -4;
	public static final int interleaved = -5;
	public static final int loop = -6;
	public static final int skip = -7;
	public static final int or = -8;

	public static final int childrenFactor = 10;

	/**
	 * Construct a new efficient tree using the given inputs. These inputs will
	 * not be copied and should not be altered outside the EfficientTree context
	 * after creating the tree.
	 * 
	 * @param tree
	 * @param activity2int
	 *            The mapping from activities (strings) to integers. The map
	 *            should be created such that the emptiness value is not 0 (as
	 *            that is a valid activity). Preferably, use
	 *            getEmptyActivity2int() to obtain such a map.
	 * @param int2activity
	 *            The mapping from integers to the activities (strings). Should
	 *            be consistent with activity2int.
	 */
	public EfficientTree(int[] tree, TObjectIntMap<String> activity2int, String[] int2activity) {
		this.tree = tree;
		this.activity2int = activity2int;
		this.int2activity = int2activity;
	}

	public Triple<int[], TObjectIntMap<String>, String[]> toTriple() {
		return Triple.of(tree, activity2int, int2activity);
	}

	public EfficientTree(ProcessTree processTree) throws UnknownTreeNodeException {
		Triple<int[], TObjectIntMap<String>, String[]> t = tree2efficientTree(processTree.getRoot());
		tree = t.getA();
		activity2int = t.getB();
		int2activity = t.getC();
	}

	public EfficientTree(Node node) throws UnknownTreeNodeException {
		Triple<int[], TObjectIntMap<String>, String[]> t = tree2efficientTree(node);
		tree = t.getA();
		activity2int = t.getB();
		int2activity = t.getC();
	}

	/**
	 * 
	 * @return the internal representation of the process tree. Do not edit the
	 *         returned object.
	 */
	public int[] getTree() {
		return tree;
	}

	public TObjectIntMap<String> getActivity2int() {
		return activity2int;
	}

	public String[] getInt2activity() {
		return int2activity;
	}

	/**
	 * Convert a process tree into a efficient tree
	 * 
	 * @param tree
	 * @param activity2int
	 *            map from activity names to ints >= 0
	 * @return
	 * @throws UnknownTreeNodeException
	 */
	public static Triple<int[], TObjectIntMap<String>, String[]> tree2efficientTree(Node node)
			throws UnknownTreeNodeException {
		TIntArrayList efficientTree = new TIntArrayList();
		TObjectIntMap<String> activity2int = getEmptyActivity2int();
		List<String> int2activity = new ArrayList<>();
		node2efficientTree(node, efficientTree, activity2int, int2activity);

		return Triple.of(efficientTree.toArray(new int[efficientTree.size()]), activity2int,
				int2activity.toArray(new String[int2activity.size()]));
	}

	private static void node2efficientTree(Node node, TIntArrayList efficientTree, TObjectIntMap<String> activity2int,
			List<String> int2activity2) throws UnknownTreeNodeException {
		if (node instanceof Automatic) {
			efficientTree.add(tau);
		} else if (node instanceof Manual) {
			int max = int2activity2.size();
			String name = node.getName();
			if (!activity2int.containsKey(name)) {
				activity2int.put(name, max);
				int2activity2.add(name);
			}
			efficientTree.add(activity2int.get(name));
		} else if (node instanceof Xor || node instanceof Def) {
			node2efficientTreeChildren(xor, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Seq) {
			node2efficientTreeChildren(sequence, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Interleaved) {
			node2efficientTreeChildren(interleaved, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof And) {
			node2efficientTreeChildren(concurrent, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof XorLoop || node instanceof DefLoop) {
			node2efficientTreeChildren(loop, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Or) {
			node2efficientTreeChildren(or, node, efficientTree, activity2int, int2activity2);
		} else {
			throw new UnknownTreeNodeException();
		}
	}

	private static void node2efficientTreeChildren(int operator, Node node, TIntArrayList result,
			TObjectIntMap<String> activity2int, List<String> int2activity) throws UnknownTreeNodeException {
		result.add(operator - (childrenFactor * ((Block) node).getChildren().size()));
		for (Node child : ((Block) node).getChildren()) {
			node2efficientTree(child, result, activity2int, int2activity);
		}
	}

	/**
	 * Add a child to the tree, as a child of parent, at the given position. The
	 * caller has to ensure there's enough space in the array of the tree.
	 * 
	 * @param parent
	 * @param asChildNr
	 */
	public void addChild(int parent, int asChildNr, int operatorOrActivity) {
		assert (tree[parent] < 0);

		//find the tree index where the new child is to go
		int insertAt = parent + 1;
		for (int j = 0; j < asChildNr; j++) {
			insertAt = traverse(insertAt);
		}

		//make space in the array
		System.arraycopy(tree, insertAt, tree, insertAt + 1, tree.length - insertAt - 1);

		//insert the new node
		tree[insertAt] = operatorOrActivity;

		//increase the children of the parent
		tree[parent] -= childrenFactor;
		if (tree[parent] > 0) {
			//int underflow happened
			throw new RuntimeException("child cannot be added");
		}
	}

	/**
	 * 
	 * @param node
	 * @return the first node after node i.
	 */
	public int traverse(int node) {
		if (tree[node] >= 0) {
			return node + 1;
		} else if (tree[node] == EfficientTree.tau) {
			return node + 1;
		} else {
			int numberOfChildren = -tree[node] / EfficientTree.childrenFactor;
			node++;
			for (int j = 0; j < numberOfChildren; j++) {
				node = traverse(node);
			}
			return node;
		}
	}

	/**
	 * 
	 * @param node
	 * @return the activity number denoted at position node. Only call if the
	 *         node is an activity.
	 */
	public int getActivity(int node) {
		return tree[node];
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @return the activity name denoted at position node. Only call if the node
	 *         is an activity.
	 */
	public String getActivityName(int node) {
		return int2activity[tree[node]];
	}

	/**
	 * 
	 * @param node
	 * @return whether the node at position i is an operator
	 */
	public boolean isOperator(int node) {
		return tree[node] < 0 && tree[node] != skip && tree[node] != tau;
	}

	/**
	 *
	 * @param node
	 * @return the number of children of the current node. Only call when the
	 *         node is an operator.
	 */
	public int getNumberOfChildren(int node) {
		return tree[node] / -childrenFactor;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is the root of the tree.
	 */
	public boolean isRoot(int node) {
		return node == 0;
	}

	/**
	 * 
	 * @param parent
	 * @param numberOfChild
	 * @return the position of the #nrOfChild child of parent.
	 */
	public int getChild(int parent, int numberOfChild) {
		int i = parent + 1;
		for (int j = 0; j < numberOfChild; j++) {
			i = traverse(i);
		}
		return i;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is a tau
	 */
	public boolean isTau(int node) {
		return tree[node] == tau;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is an activity
	 */
	public boolean isActivity(int node) {
		return tree[node] >= 0;
	}

	/**
	 * 
	 * @param i
	 * @return whether the given node is a sequence
	 */
	public boolean isSequence(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == sequence;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is a xor
	 */
	public boolean isXor(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == xor;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is an and
	 */
	public boolean isConcurrent(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == concurrent;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is an interleaved node
	 */
	public boolean isInterleaved(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == interleaved;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is a loop
	 */
	public boolean isLoop(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == loop;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given node is an or
	 */
	public boolean isOr(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == or;
	}

	/**
	 * 
	 * @param node
	 * @return the operator of the node (only call if the node is an operator)
	 */
	public int getOperator(int node) {
		return tree[node] % childrenFactor;
	}

	/**
	 * 
	 * @return The index of the root of the tree.
	 */
	public int getRoot() {
		return 0;
	}

	/**
	 * 
	 * @param node
	 * @return an iterable over all children of the given node.
	 */
	public Iterable<Integer> getChildren(final int node) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = -1;
					int next = node + 1;
					int count = 0;

					public void remove() {
						throw new RuntimeException("not implemented");
					}

					public Integer next() {
						count++;
						now = next;
						next = traverse(now);
						return now;
					}

					public boolean hasNext() {
						return count < getNumberOfChildren(node);
					}
				};
			}
		};
	}

	/**
	 * Remove a child of a node. Only call when the child has no children
	 * itself. Note that activity names may be left behind outside of the tree.
	 * 
	 * @param parent
	 * @param child
	 */
	public void removeChild(int parent, int child) {
		assert (isActivity(child) || isTau(child) || getNumberOfChildren(child) == 0);

		//move everything over the child
		System.arraycopy(tree, child + 1, tree, child, tree.length - child - 1);
		tree[tree.length - 1] = skip;

		//update the children counter of the parent
		tree[parent] += childrenFactor;
	}

	/**
	 * Replace a node and all of its children by a single tau.
	 * 
	 * @param tree
	 * @param node
	 */
	public void replaceNodeWithTau(int node) {
		if (isTau(node)) {
			return;
		}
		int nextNode = traverse(node);
		int length = nextNode - node;
		if (nextNode != tree.length) {
			System.arraycopy(tree, nextNode - 1, tree, node, tree.length - (nextNode - 1));
		}

		for (int i = tree.length - (length - 1); i < tree.length; i++) {
			tree[i] = skip;
		}

		tree[node] = tau;
	}

	/**
	 * Copy the tree into a tight array
	 * 
	 * @param tree
	 */
	public EfficientTree shortenTree() {
		int length = traverse(0);
		int[] newTree = new int[length];
		System.arraycopy(tree, 0, newTree, 0, length);
		return new EfficientTree(tree, activity2int, int2activity);
	}

	public boolean isConsistent() {
		int treeLength = traverse(0);

		if (treeLength != tree.length && tree[treeLength] != skip) {
			return false;
		}

		if (tree[treeLength - 1] == skip) {
			return false;
		}

		for (int node = 0; node < treeLength; node++) {
			if (isLoop(node) && getNumberOfChildren(node) != 3) {
				System.out.println("tree inconsistent at " + node);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param node
	 * @return whether the given position is not in the tree (any nodes after it
	 *         are not either)
	 */
	public boolean isSkip(int node) {
		return tree[node] == skip;
	}

	public void setOperator(int node, int operator) {
		tree[node] = operator - (childrenFactor * getNumberOfChildren(node));
	}

	public int[] getChildTree(int node) {
		int next = traverse(node);
		int[] result = new int[next - node];
		System.arraycopy(tree, node, result, 0, next - node);
		return result;
	}

	public static TObjectIntMap<String> getEmptyActivity2int() {
		return new TObjectIntHashMap<String>(8, 0.5f, -1);
	}

	/**
	 * Returns the parent of node. Do not call if node is the root. Notice that
	 * this is an expensive operation; avoid if possible.
	 * 
	 * @param node
	 * @return The parent of node.
	 */
	public int getParent(int node) {
		assert (node != getRoot());

		int potentialParent = node - 1;
		while (traverse(potentialParent) <= node) {
			potentialParent--;
		}
		return potentialParent;
	}

	/**
	 * Replace the tree structure.
	 * 
	 * @param newTree
	 */
	public void replaceTree(int[] tree) {
		this.tree = tree;
	}

	/**
	 * Return a string representation of this tree. This string representation
	 * is not unique for equivalent trees, e.g. the trees xor(a, b) and xor(b,
	 * a) are equivalent, but do not have the same string representation.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		try {
			toString(0, result);
		} catch (UnknownTreeNodeException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public void toString(int node, StringBuilder result) throws UnknownTreeNodeException {
		if (isActivity(node)) {
			result.append(getActivityName(node));
		} else if (isTau(node)) {
			result.append("tau");
		} else if (isOperator(node)) {
			if (isXor(node)) {
				result.append("xor(");
			} else if (isSequence(node)) {
				result.append("seq(");
			} else if (isConcurrent(node)) {
				result.append("and(");
			} else if (isInterleaved(node)) {
				result.append("int(");
			} else if (isLoop(node)) {
				result.append("loop(");
			} else if (isOr(node)) {
				result.append("or(");
			} else {
				throw new UnknownTreeNodeException();
			}
			for (int i = 0; i < getNumberOfChildren(node); i++) {
				int child = getChild(node, i);
				toString(child, result);
				if (i < getNumberOfChildren(node) - 1) {
					result.append(",");
				}
			}
			result.append(")");
		} else {
			throw new UnknownTreeNodeException();
		}

	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity2int == null) ? 0 : activity2int.hashCode());
		result = prime * result + Arrays.hashCode(int2activity);
		result = prime * result + Arrays.hashCode(tree);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EfficientTree other = (EfficientTree) obj;
		if (activity2int == null) {
			if (other.activity2int != null)
				return false;
		} else if (!activity2int.equals(other.activity2int))
			return false;
		if (!Arrays.equals(int2activity, other.int2activity))
			return false;
		if (!Arrays.equals(tree, other.tree))
			return false;
		return true;
	}

}
