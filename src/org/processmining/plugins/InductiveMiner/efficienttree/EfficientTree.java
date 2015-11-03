package org.processmining.plugins.InductiveMiner.efficienttree;

import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.hash.TObjectShortHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
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
 * Idea: keep an array of short. An activity is a >= 0 value. A node is a
 * negative value. Some bits encode the operator, the other bits the number of
 * children.
 * 
 * @author sleemans
 *
 */
public class EfficientTree {

	private final short[] tree;
	private final TObjectShortMap<String> activity2short;
	private final String[] short2activity;

	public EfficientTree(short[] tree, TObjectShortMap<String> activity2short, String[] short2activity) {
		this.tree = tree;
		this.activity2short = activity2short;
		this.short2activity = short2activity;
	}

	public Triple<short[], TObjectShortMap<String>, String[]> toTriple() {
		return Triple.of(tree, activity2short, short2activity);
	}

	public EfficientTree(ProcessTree processTree) {
		Triple<short[], TObjectShortMap<String>, String[]> t = tree2efficientTree(processTree.getRoot());
		tree = t.getA();
		activity2short = t.getB();
		short2activity = t.getC();
	}

	public EfficientTree(Node node) {
		Triple<short[], TObjectShortMap<String>, String[]> t = tree2efficientTree(node);
		tree = t.getA();
		activity2short = t.getB();
		short2activity = t.getC();
	}

	public short[] getTree() {
		return tree;
	}

	public TObjectShortMap<String> getActivity2short() {
		return activity2short;
	}

	public String[] getShort2activity() {
		return short2activity;
	}

	public static final short tau = -1;
	public static final short xor = -2;
	public static final short sequence = -3;
	public static final short concurrent = -4;
	public static final short interleaved = -5;
	public static final short loop = -6;
	public static final short skip = -7;

	public static final short childrenFactor = 10;

	/**
	 * Convert a process tree into a efficient tree
	 * 
	 * @param tree
	 * @param activity2short
	 *            map from activity names to shorts >= 0
	 * @return
	 */
	public static Triple<short[], TObjectShortMap<String>, String[]> tree2efficientTree(Node node) {
		TShortArrayList efficientTree = new TShortArrayList();
		TObjectShortMap<String> activity2short = getEmptyActivity2short();
		List<String> short2activity = new ArrayList<>();
		node2efficientTree(node, efficientTree, activity2short, short2activity);

		return Triple.of(efficientTree.toArray(new short[efficientTree.size()]), activity2short,
				short2activity.toArray(new String[short2activity.size()]));
	}

	private static void node2efficientTree(Node node, TShortArrayList efficientTree,
			TObjectShortMap<String> activity2short, List<String> short2activity2) {
		if (node instanceof Automatic) {
			efficientTree.add(tau);
		} else if (node instanceof Manual) {
			short max = (short) short2activity2.size();
			String name = node.getName();
			if (!activity2short.containsKey(name)) {
				activity2short.put(name, max);
				short2activity2.add(name);
			}
			efficientTree.add(activity2short.get(name));
		} else if (node instanceof Xor || node instanceof Def) {
			node2efficientTreeChildren(xor, node, efficientTree, activity2short, short2activity2);
		} else if (node instanceof Seq) {
			node2efficientTreeChildren(sequence, node, efficientTree, activity2short, short2activity2);
		} else if (node instanceof Interleaved) {
			node2efficientTreeChildren(interleaved, node, efficientTree, activity2short, short2activity2);
		} else if (node instanceof And) {
			node2efficientTreeChildren(concurrent, node, efficientTree, activity2short, short2activity2);
		} else if (node instanceof XorLoop || node instanceof DefLoop) {
			node2efficientTreeChildren(loop, node, efficientTree, activity2short, short2activity2);
		} else {
			throw new RuntimeException("Node operator translation not implemented.");
		}
	}

	private static void node2efficientTreeChildren(short operator, Node node, TShortArrayList result,
			TObjectShortMap<String> activity2short, List<String> short2activity) {
		result.add((short) (operator - (childrenFactor * ((Block) node).getChildren().size())));
		for (Node child : ((Block) node).getChildren()) {
			node2efficientTree(child, result, activity2short, short2activity);
		}
	}

	/**
	 * Add a child to the tree, as a child of parent, at the given position. The
	 * caller has to ensure there's enough space in the array of the tree.
	 * 
	 * @param parent
	 * @param asChildNr
	 */
	public void addChild(int parent, int asChildNr, short operatorOrActivity) {
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
			//short underflow happened
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
	public short getActivity(int node) {
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
		return short2activity[tree[node]];
	}

	/**
	 * 
	 * @param node
	 * @return whether the node at position i is an operator
	 */
	public boolean isOperator(int node) {
		return tree[node] < tau && tree[node] != skip;
	}

	/**
	 *
	 * @param node
	 * @return the number of children of the current node. Only call when the
	 *         node is an operator.
	 */
	public short getNumberOfChildren(int node) {
		return (short) (tree[node] / -childrenFactor);
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
	 * @return the operator of the node (only call if the node is an operator)
	 */
	public short getOperator(int node) {
		return (short) (tree[node] % childrenFactor);
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
		short[] newTree = new short[length];
		System.arraycopy(tree, 0, newTree, 0, length);
		return new EfficientTree(tree, activity2short, short2activity);
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

	public void setOperator(int node, short operator) {
		tree[node] = (short) (operator - (childrenFactor * getNumberOfChildren(node)));
	}

	public short[] getChildTree(int node) {
		int next = traverse(node);
		short[] result = new short[next - node];
		System.arraycopy(tree, node, result, 0, next - node);
		return result;
	}
	
	public static TObjectShortMap<String> getEmptyActivity2short() {
		return new TObjectShortHashMap<String>(8, 0.5f, (short) -1);
	}
}
