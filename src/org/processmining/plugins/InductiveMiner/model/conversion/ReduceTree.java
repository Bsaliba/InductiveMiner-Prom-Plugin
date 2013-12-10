package org.processmining.plugins.InductiveMiner.model.conversion;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.model.Binoperator;
import org.processmining.plugins.InductiveMiner.model.EventClass;
import org.processmining.plugins.InductiveMiner.model.ExclusiveChoice;
import org.processmining.plugins.InductiveMiner.model.Loop;
import org.processmining.plugins.InductiveMiner.model.Node;
import org.processmining.plugins.InductiveMiner.model.Parallel;
import org.processmining.plugins.InductiveMiner.model.ProcessTreeModel;
import org.processmining.plugins.InductiveMiner.model.Sequence;
import org.processmining.plugins.InductiveMiner.model.Tau;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Reduce process tree language-equivalently", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Process Tree" }, userAccessible = true)
public class ReduceTree {

	public static ProcessTreeModel reduceTree(ProcessTreeModel tree) {
		ProcessTreeModel newTree = new ProcessTreeModel();
		Node root = tree.root;
		newTree.root = reduceNode(root);

		return newTree;
	}

	public static Node reduceNode(Node node) {

		Node newNode = null;
		if (node instanceof Tau) {
			newNode = new Tau();
		} else if (node instanceof EventClass) {
			newNode = new EventClass(((EventClass) node).eventClass);
		} else if (node instanceof Binoperator) {
			newNode = reduceBinoperator((Binoperator) node);
		}

		return newNode;
	}

	private static Node reduceBinoperator(Binoperator node) {
		List<Node> children = null;
		if (node instanceof Sequence) {
			children = flattenSequence((Sequence) node);

			if (children.size() >= 2) {
				return new Sequence(children);
			}
		} else if (node instanceof ExclusiveChoice) {
			children = flattenXor((ExclusiveChoice) node);

			if (children.size() >= 2) {
				return new ExclusiveChoice(children);
			}
		} else if (node instanceof Loop) {
			Pair<Node, Set<Node>> p = flattenLoop((Loop) node);

			//construct a children array
			children = new LinkedList<Node>();
			children.add(p.getLeft());
			for (Node child : p.getRight()) {
				children.add(child);
			}

			if (children.size() >= 2) {
				return new Loop(children);
			}
		} else if (node instanceof Parallel) {
			children = flattenParallel((Parallel) node);

			if (children.size() >= 2) {
				return new Parallel(children);
			}
		}

		//there are less than two children available
		if (children.size() == 0) {
			//no child, return tau
			return new Tau();
		} else {
			//one child, return that child
			return children.get(0);
		}

	}

	private static List<Node> flattenSequence(Sequence node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof Sequence) {
				//this child is a sequence-child of a sequence
				result.addAll(flattenSequence((Sequence) child));
			} else {
				result.add(reduceNode(child));
			}
		}

		//remove  taus
		List<Node> result2 = new LinkedList<Node>();
		for (Node child : result) {
			if (child instanceof Tau) {

			} else {
				result2.add(child);
			}
		}

		return result2;
	}

	private static List<Node> flattenXor(ExclusiveChoice node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof ExclusiveChoice) {
				//this child is an xor-child of an xor
				result.addAll(flattenXor((ExclusiveChoice) child));
			} else {
				result.add(reduceNode(child));
			}
		}

		//remove double taus
		boolean seenTau = false;
		List<Node> result2 = new LinkedList<Node>();
		for (Node child : result) {
			if (child instanceof Tau) {
				if (!seenTau) {
					result2.add(child);
				}
				seenTau = true;
			} else {
				result2.add(child);
			}
		}

		return result2;
	}

	private static Pair<Node, Set<Node>> flattenLoop(Loop node) {
		Node leftChild = node.getChildren().get(0);
		List<Node> rightChildren = node.getChildren().subList(1, node.getChildren().size() - 1);

		Set<Node> newRightChildren = new HashSet<Node>();
		Node newLeftChild = null;
		if (leftChild instanceof Loop) {

			Pair<Node, Set<Node>> p = flattenLoop((Loop) leftChild);

			newLeftChild = p.getLeft();
			newRightChildren.addAll(p.getRight());

		} else {
			newLeftChild = reduceNode(leftChild);
		}

		for (Node child : rightChildren) {
			if (child instanceof ExclusiveChoice) {
				newRightChildren.addAll(flattenXor((ExclusiveChoice) child));
			} else {
				newRightChildren.add(reduceNode(child));
			}
		}

		return new Pair<Node, Set<Node>>(newLeftChild, newRightChildren);
	}

	private static List<Node> flattenParallel(Parallel node) {
		List<Node> result = new LinkedList<Node>();
		for (Node child : node.getChildren()) {
			if (child instanceof Parallel) {
				//this child is an and-child of an and
				result.addAll(flattenParallel((Parallel) child));
			} else {
				result.add(child);
			}
		}

		//remove  taus
		List<Node> result2 = new LinkedList<Node>();
		for (Node child : result) {
			if (child instanceof Tau) {

			} else {
				result2.add(child);
			}
		}

		return result2;
	}

}
