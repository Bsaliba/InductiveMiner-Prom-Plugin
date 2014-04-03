package org.processmining.plugins.InductiveMiner.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

@Plugin(name = "Reduce process tree language-equivalently", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Process Tree" }, userAccessible = true)
public class ReduceTree {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Reduce Process Tree Language-equivalently, default", requiredParameterLabels = { 0 })
	public ProcessTree reduceTree(PluginContext context, ProcessTree tree) {
		reduceTree(tree);
		return tree;
	}

	public static List<ReductionPattern> patterns = new ArrayList<ReductionPattern>(Arrays.asList(new RPFlattenXor(),
			new RPFlattenAnd(), new RPFlattenSeq(), new RPDoubleTausUnderXor(), new RPFlattenLoop(), new RPOneChild()));

	public static abstract class ReductionPattern {
		public abstract boolean apply(Node node, ProcessTree tree);
	}

	public static class RPOneChild extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Block)) {
				return false;
			}

			int i = 0;
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof Block && ((Block) node).getChildren().size() == 1) {
					
					changed = true;
					i = flattenChildAt((Block) node, (Block) child, i, tree);

					//for all of these, the number of traces represented remains the same
				} else {
					i++;
				}
			}
			return changed;
		}
	}

	public static class RPFlattenXor extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Xor)) {
				return false;
			}
			int i = 0;
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof Xor) {
					changed = true;
					i = flattenChildAt((Block) node, (Block) child, i, tree);

					//for xor, the number of traces represented remains the same
				} else {
					i++;
				}
			}
			return changed;
		}
	}

	public static class RPDoubleTausUnderXor extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Xor)) {
				return false;
			}

			Xor xor = (Xor) node;

			//gather taus and children that can produce the empty trace
			List<Automatic> taus = new ArrayList<Automatic>();
			Node childProducingTau = null;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof Automatic) {
					taus.add((Automatic) child);
				} else if (childProducingTau == null && MinerMetrics.getShortestTrace(child) == 0) {
					//this child can produce tau, save it for later reference
					childProducingTau = child;
				}
			}

			if (taus.size() == 0) {
				return false;
			}

			if (taus.size() == 1 && childProducingTau == null) {
				return false;
			}

			//reduce
			if (childProducingTau != null) {
				//all taus can be removed
				int emptyTraces = 0;
				for (Automatic tau : taus) {
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
					MinerMetrics.saveMovesSumInto(childProducingTau, tau);
					emptyTraces += MinerMetrics.getNumberOfTracesRepresented(tau);
				}
				addEmptyTraces(childProducingTau, emptyTraces);
			} else {
				//only the non-first taus can be removed
				Iterator<Automatic> it = taus.iterator();
				Automatic keepTau = it.next();
				while (it.hasNext()) {
					Automatic tau = it.next();
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
					MinerMetrics.saveMovesSumInto(keepTau, tau);
				}
			}

			return true;
		}
	}

	public static class RPFlattenAnd extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof And)) {
				return false;
			}
			int i = 0;
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof And) {
					changed = true;
					i = flattenChildAt((Block) node, (Block) child, i, tree);

					//for parallel, the number of traces represented is the same, so don't sum
				} else {
					i++;
				}
			}
			return changed;
		}
	}

	public static class RPFlattenSeq extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Seq)) {
				return false;
			}
			int i = 0;
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof Seq) {
					changed = true;
					i = flattenChildAt((Block) node, (Block) child, i, tree);

					//for sequence, the number of traces represented is the same, so don't sum
				} else {
					i++;
				}
			}
			return changed;
		}
	}

	public static class RPFlattenLoop extends ReductionPattern {

		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof XorLoop)) {
				return false;
			}

			Node b = ((Block) node).getChildren().get(0);

			if (!(b instanceof XorLoop)) {
				return false;
			}

			XorLoop loop = (XorLoop) node;
			XorLoop oldBody = (XorLoop) b;
			Node oldRedo = ((Block) node).getChildren().get(1);
			Node exit = ((Block) node).getChildren().get(2);
			Node bodyBody = oldBody.getChildren().get(0);
			Node bodyRedo = oldBody.getChildren().get(1);
			Node bodyExit = oldBody.getChildren().get(2);

			if (!(exit instanceof Automatic)) {
				return false;
			}

			if (!(oldBody.getChildren().get(2) instanceof Automatic)) {
				return false;
			}

			//clean up the old body
			removeChild(oldBody, bodyBody, tree);
			removeChild(oldBody, bodyRedo, tree);
			removeChild(oldBody, bodyExit, tree);
			tree.removeNode(bodyExit);
			removeChild(loop, oldBody, tree);
			tree.removeNode(oldBody);

			//reconnect bodybody
			loop.addChildAt(bodyBody, 0);

			//reconnect redo and bodyredo
			removeChild(loop, oldRedo, tree);
			Xor redoXor = new Xor("");
			redoXor.setProcessTree(tree);
			loop.addChildAt(redoXor, 1);
			redoXor.addChild(oldRedo);
			redoXor.addChild(bodyRedo);

			//set metrics
			MinerMetrics.attachProducer(redoXor, "reduceTree, " + MinerMetrics.getProducer(oldBody));
			MinerMetrics.attachEpsilonTracesSkipped(redoXor, MinerMetrics.getEpsilonTracesSkipped(oldBody));
			MinerMetrics.attachMovesOnLog(redoXor, MinerMetrics.getMovesOnLog(oldBody));
			MinerMetrics.attachMovesOnModelWithoutEpsilonTracesFiltered(redoXor,
					MinerMetrics.getMovesOnModelWithoutEpsilonTracesFiltered(oldBody));
			MinerMetrics.attachNumberOfTracesRepresented(redoXor, 0l);
			MinerMetrics.attachNumberOfTracesRepresented(redoXor, getNumberOfTracesRepresentedChildrenXor(redoXor));

			return true;
		}

	}

	public static void reduceTree(ProcessTree tree) {
		reduceNode(tree.getRoot(), tree);

		/*
		 * ProcessTree newTree = new ProcessTreeImpl(); Node root =
		 * tree.getRoot(); newTree.setRoot(reduceNode(root, newTree));
		 * System.out.println("after reduction " + tree.getRoot());
		 */

	}

	private static void removeChild(Block node, Node child, ProcessTree tree) {
		//remove grandChild <-> childXor connection\
		List<Edge> edges = new ArrayList<Edge>();
		for (Edge edge : node.getOutgoingEdges()) {
			if (edge.getTarget() == child) {
				edges.add(edge);
			}
		}
		for (Edge edge : edges) {
			node.removeOutgoingEdge(edge);
			tree.removeEdge(edge);
			child.removeIncomingEdge(edge);
		}
	}

	private static int flattenChildAt(Block node, Block child, int i, ProcessTree tree) {

		//merge structure
		for (Node grandChild : child.getChildren()) {
			node.addChildAt(grandChild, i);
			i++;
			removeChild(child, grandChild, tree);
		}
		removeChild(node, child, tree);

		//merge move metrics
		MinerMetrics.saveMovesSumInto(node, child);

		//remove old node
		tree.removeNode(child);

		return i;
	}

	private static void reduceNode(Node node, ProcessTree tree) {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (ReductionPattern pattern : patterns) {
				changed = changed || pattern.apply(node, tree);
			}
		}
		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				reduceNode(child, tree);
			}
		}
	}

	private static List<Node> getPathToTau(Node node) {
		List<Node> result = new ArrayList<Node>(Arrays.asList(node));
		if (node instanceof Manual) {
			return null;
		} else if (node instanceof Automatic) {
			return result;
		} else if (node instanceof And || node instanceof Seq) {
			for (Node child : ((Block) node).getChildren()) {
				List<Node> childPath = getPathToTau(child);
				if (childPath != null) {
					result.addAll(childPath);
				} else {
					return null;
				}
			}
			return result;
		} else if (node instanceof Xor || node instanceof Def) {
			for (Node child : ((Block) node).getChildren()) {
				List<Node> childPath = getPathToTau(child);
				if (childPath != null) {
					result.addAll(childPath);
					return result;
				}
			}
			return null;
		} else if (node instanceof DefLoop || node instanceof XorLoop) {
			{
				List<Node> childPath = getPathToTau(((Block) node).getChildren().get(0));
				if (childPath != null) {
					result.addAll(childPath);
				} else {
					return null;
				}
			}

			{
				List<Node> childPath = getPathToTau(((Block) node).getChildren().get(2));
				if (childPath != null) {
					result.addAll(childPath);
				} else {
					return null;
				}
			}
			return result;
		}
		assert (false);
		return null;
	}

	/*
	 * Adds empty traces to metrics of nodes. Returns whether successful.
	 */
	private static void addEmptyTraces(Node node, int emptyTraces) {
		List<Node> pathToTau = getPathToTau(node);
		if (pathToTau == null) {
			System.out.println(pathToTau);
			System.out.println(MinerMetrics.getShortestTrace(node));
			System.out.println(node);
			throw new RuntimeException("no path to tau found while it should be there");
		}
		for (Node child : pathToTau) {
			MinerMetrics.attachNumberOfTracesRepresented(child, MinerMetrics.getNumberOfTracesRepresented(child));
		}
	}

	//	
	//	private static Node reduceNode(Node node, ProcessTree newTree) {
	//
	//		Node newNode = null;
	//		List<Node> children = null;
	//		if (node instanceof Seq) {
	//			newNode = new Seq("");
	//			children = flattenSequence((Seq) node);
	//		} else if (node instanceof Xor || node instanceof DummyXor) {
	//			newNode = new Xor("");
	//			children = flattenXor((AbstractBlock) node);
	//		} else if (node instanceof XorLoop) {
	//			newNode = new XorLoop("");
	//			Pair<Node, Set<Node>> p = flattenLoop((XorLoop) node);
	//
	//			//construct a children array
	//			children = new ArrayList<Node>();
	//			children.add(p.getLeft());
	//			if (p.getRight().size() == 1) {
	//				//if only one right-side child remains, add it
	//				children.add(p.getRight().iterator().next());
	//			} else {
	//				//else, add all children as an xor
	//				DummyXor middleChild = new DummyXor("dummy");
	//				for (Node grandChild : p.getRight()) {
	//					middleChild.addChild(grandChild);
	//				}
	//
	//				children.add(middleChild);
	//				MinerMetrics.attachNumberOfTracesRepresented(middleChild,
	//						getNumberOfTracesRepresentedChildrenXor(middleChild));
	//			}
	//			children.add(((XorLoop) node).getChildren().get(2));
	//		} else if (node instanceof And) {
	//			newNode = new And("");
	//			children = flattenParallel((And) node);
	//		} else {
	//
	//			//copy the node
	//			Class<? extends Node> c = node.getClass();
	//
	//			try {
	//				Constructor<? extends Node> ctor = c.getConstructor(String.class);
	//				newNode = ctor.newInstance(new Object[] { node.getName() });
	//			} catch (NoSuchMethodException e) {
	//				try {
	//					Constructor<? extends Node> ctor = c.getConstructor(String.class, Originator[].class);
	//					newNode = ctor.newInstance(new Object[] { node.getName(), new Originator[0] });
	//				} catch (NoSuchMethodException e1) {
	//					e1.printStackTrace();
	//				} catch (SecurityException e1) {
	//					e1.printStackTrace();
	//				} catch (InstantiationException e1) {
	//					e1.printStackTrace();
	//				} catch (IllegalAccessException e1) {
	//					e1.printStackTrace();
	//				} catch (IllegalArgumentException e1) {
	//					e1.printStackTrace();
	//				} catch (InvocationTargetException e1) {
	//					e1.printStackTrace();
	//				}
	//			} catch (SecurityException e) {
	//				e.printStackTrace();
	//			} catch (InstantiationException e) {
	//				e.printStackTrace();
	//			} catch (IllegalAccessException e) {
	//				e.printStackTrace();
	//			} catch (IllegalArgumentException e) {
	//				e.printStackTrace();
	//			} catch (InvocationTargetException e) {
	//				e.printStackTrace();
	//			}
	//
	//			if (newNode instanceof Block) {
	//				children = ((Block) node).getChildren();
	//			}
	//		}
	//
	//		newNode.setProcessTree(newTree);
	//		newTree.addNode(newNode);
	//
	//		//copy statistics
	//		MinerMetrics.copyStatistics(node, newNode);
	//
	//		if (newNode instanceof Block) {
	//			if (children.size() >= 2) {
	//				for (Node child : children) {
	//					Node newChild = reduceNode(child, newTree);
	//					connectChildToParent(newChild, newNode, newTree);
	//				}
	//			} else {
	//				newNode = reduceNode(children.get(0), newTree);
	//			}
	//		}
	//
	//		return newNode;
	//	}
	//
	//	private static List<Node> flattenSequence(Seq node) {
	//		List<Node> result = new ArrayList<Node>();
	//		for (Node child : node.getChildren()) {
	//			if (child instanceof Seq) {
	//				//this child is a sequence-child of a sequence
	//				result.addAll(flattenSequence((Seq) child));
	//			} else {
	//				result.add(child);
	//			}
	//		}
	//		return result;
	//	}
	//
	//	private static List<Node> flattenXor(AbstractBlock node) {
	//		List<Node> result = new ArrayList<Node>();
	//		for (Node child : node.getChildren()) {
	//			if (child instanceof Xor) {
	//				//this child is an xor-child of an xor
	//				result.addAll(flattenXor((Xor) child));
	//			} else {
	//				result.add(child);
	//			}
	//		}
	//
	//		//remove superfluous taus
	//		//if one of the children can produce tau, then no explicit tau is necessary
	//		boolean oneOfChildrenCanProduceTau = false;
	//		int numberOfEmptyTracesProduced = 0;
	//		Node childTau = null;
	//		//Node childThatCanProduceTau = null;
	//		Iterator<Node> it = result.iterator();
	//		while (it.hasNext()) {
	//			Node child = it.next();
	//			if (child instanceof Task.Automatic) {
	//				if (MinerMetrics.getNumberOfTracesRepresented(child) != null) {
	//					numberOfEmptyTracesProduced += MinerMetrics.getNumberOfTracesRepresented(child);
	//				}
	//				childTau = child;
	//				it.remove();
	//			} else {
	//				if (canProduceTau(child)) {
	//					oneOfChildrenCanProduceTau = true;
	//					//childThatCanProduceTau = child;
	//				}
	//			}
	//		}
	//		if (childTau != null) {
	//			//there is at least one tau child
	//			if (!oneOfChildrenCanProduceTau) {
	//				//no non-tau child can produce the empty trace, so we must add the tau
	//				MinerMetrics.attachNumberOfTracesRepresented(childTau, numberOfEmptyTracesProduced);
	//				result.add(childTau);
	//			} else {
	//				//another non-tau child can produce the empty trace
	//				//one of such children should represent the empty traces produced by the tau-children,
	//				//but in order to know which tau was executed, we need an alignment
	//			}
	//		}
	//		return result;
	//	}
	//
	//	private static Pair<Node, Set<Node>> flattenLoop(XorLoop node) {
	//		Node leftChild = node.getChildren().get(0);
	//		Node middleChild = node.getChildren().get(1);
	//		Set<Node> result = new HashSet<Node>();
	//
	//		//this reduction only works on loops with an empty third branch
	//		if (!(node.getChildren().get(2) instanceof Automatic)) {
	//			result.add(middleChild);
	//			return new Pair<Node, Set<Node>>(leftChild, result);
	//		}
	//
	//		if (leftChild instanceof XorLoop) {
	//
	//			Pair<Node, Set<Node>> p = flattenLoop((XorLoop) leftChild);
	//
	//			result.addAll(p.getRight());
	//			if (middleChild instanceof Xor) {
	//				result.addAll(flattenXor((Xor) middleChild));
	//			} else {
	//				result.add(middleChild);
	//			}
	//			return new Pair<Node, Set<Node>>(p.getLeft(), result);
	//		} else {
	//			if (middleChild instanceof Xor) {
	//				result.addAll(flattenXor((Xor) middleChild));
	//			} else {
	//				result.add(middleChild);
	//			}
	//			return new Pair<Node, Set<Node>>(leftChild, result);
	//		}
	//	}
	//
	//	private static List<Node> flattenParallel(And node) {
	//		List<Node> result = new ArrayList<Node>();
	//		for (Node child : node.getChildren()) {
	//			if (child instanceof And) {
	//				//this child is an xor-child of an xor
	//				result.addAll(flattenParallel((And) child));
	//			} else {
	//				result.add(child);
	//			}
	//		}
	//		return result;
	//	}
	//
	//	private static void connectChildToParent(Node child, Node parent, ProcessTree newTree) {
	//		Edge edge = ((Block) parent).addChild(child);
	//		child.addIncomingEdge(edge);
	//		newTree.addEdge(edge);
	//	}
	//
	//	private static class DummyXor extends AbstractBlock {
	//
	//		private List<Node> children2;
	//
	//		public DummyXor(String name) {
	//			super(name);
	//			children2 = new ArrayList<Node>();
	//		}
	//
	//		public List<Node> getChildren() {
	//			return children2;
	//		}
	//
	//		public Edge addChild(Node child) {
	//			children2.add(child);
	//			return null;
	//		}
	//
	//		public boolean orderingOfChildernMatters() {
	//			return false;
	//		}
	//
	//		public boolean expressionsOfOutgoingEdgesMatter() {
	//			return false;
	//		}
	//
	//		public String toStringShort() {
	//			return null;
	//		}
	//	}

	public static Long getNumberOfTracesRepresentedChildrenXor(Node node) {
		long sum = 0;
		if (MinerMetrics.getNumberOfTracesRepresented(node) != null) {
			return MinerMetrics.getNumberOfTracesRepresented(node);
		} else if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				if (getNumberOfTracesRepresentedChildrenXor(child) == null) {
					return null;
				}
				sum += getNumberOfTracesRepresentedChildrenXor(child);
			}
			return sum;
		} else {
			return null;
		}
	}
}
