package org.processmining.plugins.InductiveMiner.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

@Plugin(name = "Reduce process tree language-equivalently", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Process Tree" }, userAccessible = true)
public class ReduceTree {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Reduce Process Tree Language-equivalently, default", requiredParameterLabels = { 0 })
	public ProcessTree reduceTree(PluginContext context, ProcessTree tree) {
		reduceTree(tree);
		return tree;
	}

	public static void reduceTree(ProcessTree tree) {
		reduceNode(tree.getRoot());
	}

	public static void reduceNode(Node node) {
		boolean changed = true;
		while (changed) {
			changed = reduceNode(node, node.getProcessTree());
		}
	}

	public static List<ReductionPattern> patterns = new ArrayList<ReductionPattern>(Arrays.asList(new RPOneChild(),
			new RPFlattenSeqAndAboveTau(), new RPFlattenXor(), new RPFlattenAnd(), new RPFlattenSeq(),
			new RPDoubleTausUnderXor(), new RPFlattenLoop(), new RPThreeTauLoop(), new RPTwoTausAndActivityUnderLoop(),
			new RPXorTauAlmostFlower()));

	public static abstract class ReductionPattern {
		public abstract boolean apply(Node node, ProcessTree tree);
	}

	public static class RPOneChild extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (node instanceof Block && ((Block) node).getChildren().size() == 1) {
				Node child = ((Block) node).getChildren().get(0);
				if (node.getProcessTree().getRoot() == node) {
					//root
					removeChild((Block) node, child, tree);
					tree.setRoot(child);
					tree.removeNode(node);
					return true;
				} else {
					//non-root
					Block parent = node.getParents().iterator().next();
					for (int i = 0; i < parent.getChildren().size(); i++) {
						if (parent.getChildren().get(i) == node) {
							flattenChildAt(parent, (Block) node, i, tree);
							return true;
						}
					}
				}
			}
			return false;
		}

		public String toString() {
			return "one child";
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
				} else {
					i++;
				}
			}
			return changed;
		}
	}

	/**
	 * xor(tau, xorLoop(A, tau, tau), ...) => xor(xorLoop(tau, A, tau), ...)
	 * 
	 * @author sleemans
	 *
	 */
	public static class RPXorTauAlmostFlower extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Xor || node instanceof Def)) {
				return false;
			}

			//at least one child must be tau
			Node tauChild = null;
			{
				for (Node child : ((Block) node).getChildren()) {
					if (child instanceof Automatic) {
						tauChild = child;
					}
				}
				if (tauChild == null) {
					return false;
				}
			}

			//at least one child must be loop(A, tau, tau)
			Block loopChild = null;
			{
				for (Node child : ((Block) node).getChildren()) {
					if (child instanceof XorLoop || child instanceof DefLoop) {
						Block child2 = (Block) child;
						//second and third child are tau
						if (child2.getChildren().get(1) instanceof Automatic
								&& child2.getChildren().get(2) instanceof Automatic) {
							loopChild = child2;
						}
					}
				}
				if (loopChild == null) {
					return false;
				}
			}

			//cut away the first tau
			removeChild((Block) node, tauChild, tree);
			tree.removeNode(node);
			return true;
		}
	}

	public static class RPTwoTausAndActivityUnderLoop extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof XorLoop || node instanceof DefLoop)) {
				return false;
			}

			if (((Block) node).getChildren().isEmpty()) {
				//this node is already removed
				return false;
			}

			//first child is a tau
			if (!(((Block) node).getChildren().get(0) instanceof Automatic)) {
				return false;
			}

			//second child is a xor
			if (!(((Block) node).getChildren().get(1) instanceof Xor || ((Block) node).getChildren().get(1) instanceof Def)) {
				return false;
			}
			Block xorChild = (Block) ((Block) node).getChildren().get(1);

			//second child contains a tau
			Node grandChildTau = null;
			{
				for (Node child : xorChild.getChildren()) {
					if (child instanceof Automatic) {
						grandChildTau = child;
						break;
					}
				}
				if (grandChildTau == null) {
					return false;
				}
			}

			//remove the tau
			removeChild(xorChild, grandChildTau, tree);
			tree.removeNode(grandChildTau);
			return true;
		}
	}

	public static class RPThreeTauLoop extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof XorLoop || node instanceof DefLoop)) {
				return false;
			}

			if (((Block) node).getChildren().isEmpty()) {
				//this node is already removed
				return false;
			}

			if (!(((Block) node).getChildren().get(0) instanceof Automatic
					&& ((Block) node).getChildren().get(1) instanceof Automatic && ((Block) node).getChildren().get(2) instanceof Automatic)) {
				return false;
			}

			//this is a loop with three tau children; replace with a single tau

			removeChild((Block) node, ((Block) node).getChildren().get(0), tree);
			removeChild((Block) node, ((Block) node).getChildren().get(0), tree);
			removeChild((Block) node, ((Block) node).getChildren().get(0), tree);

			Node newChild = new AbstractTask.Automatic(getUUID(), "tau");
			tree.addNode(newChild);
			if (node.getProcessTree().getRoot() == node) {
				//root
				tree.setRoot(newChild);
				tree.removeNode(node);
				return true;
			} else {
				//non-root
				Block parent = node.getParents().iterator().next();
				for (int i = 0; i < parent.getChildren().size(); i++) {
					if (parent.getChildren().get(i) == node) {
						parent.addChildAt(newChild, i);

						//remove old node
						removeChild(parent, node, tree);
						tree.removeNode(node);
						return true;
					}
				}
				return false;
			}
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

			//reduce
			if (childProducingTau != null) {
				//all taus can be removed
				if (taus.size() == 0) {
					return false;
				}

				for (Automatic tau : taus) {
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
				}
			} else {
				//only the non-first taus can be removed
				if (taus.size() <= 1) {
					return false;
				}

				Iterator<Automatic> it = taus.iterator();
				it.next();
				while (it.hasNext()) {
					Automatic tau = it.next();
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
				}
			}

			return true;
		}

		public String toString() {
			return "double taus under xor";
		}
	}

	public static class RPFlattenAnd extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof And) || node instanceof Interleaved) {
				return false;
			}
			int i = 0;
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof And && !(child instanceof Interleaved)) {
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

	public static class RPFlattenSeqAndAboveTau extends ReductionPattern {
		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof Seq || node instanceof And)) {
				return false;
			}
			boolean changed = false;
			for (Node child : ((Block) node).getChildren()) {
				if (child instanceof Automatic && ((Block) node).getChildren().size() > 1) {
					//remove tau
					removeChild((Block) node, child, tree);
					tree.removeNode(child);
					changed = true;
				}
			}
			return changed;
		}

		public String toString() {
			return "taus under sequence/parallel";
		}
	}

	public static class RPFlattenLoop extends ReductionPattern {

		public boolean apply(Node node, ProcessTree tree) {
			if (!(node instanceof XorLoop)) {
				return false;
			}

			if (((Block) node).getChildren().isEmpty()) {
				//this function might be called on an already-removed node; in that case skip. 
				return false;
			}

			Node b = ((Block) node).getChildren().get(0);

			if (!(b instanceof XorLoop)) {
				return false;
			}

			XorLoop loop = (XorLoop) node;
			XorLoop oldBody = (XorLoop) b;
			Node oldRedo = ((Block) node).getChildren().get(1);
			Node bodyBody = oldBody.getChildren().get(0);
			Node bodyRedo = oldBody.getChildren().get(1);
			Node bodyExit = oldBody.getChildren().get(2);

			if (!(bodyExit instanceof Automatic)) {
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
			Xor redoXor = new AbstractBlock.Xor("");
			redoXor.setProcessTree(tree);
			tree.addNode(redoXor);
			loop.addChildAt(redoXor, 1);
			redoXor.addChild(oldRedo);
			redoXor.addChild(bodyRedo);

			return true;
		}

	}

	private static void removeChild(Block node, Node child, ProcessTree tree) {
		//remove grandChild <-> childXor connection
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

	private static Random random = new Random();

	private static UUID getUUID() {
		return new UUID(random.nextLong(), random.nextLong());
	}

	private static int flattenChildAt(Block node, Block child, int i, ProcessTree tree) {

		//merge structure
		for (Node grandChild : child.getChildren()) {

			node.addChildAt(grandChild, i);
			i++;
			removeChild(child, grandChild, tree);
		}
		removeChild(node, child, tree);

		//remove old node
		tree.removeNode(child);

		return i;
	}

	private static boolean reduceNode(Node node, ProcessTree tree) {
		boolean changed = true;
		boolean changed2 = false;

		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				changed2 |= reduceNode(child, tree);
			}
		}

		while (changed) {
			changed = false;
			//			System.out.println("apply patterns to " + node);
			for (ReductionPattern pattern : patterns) {
				boolean x = pattern.apply(node, tree);
				//				if (x) {
				//					System.out.println(" used: " + pattern);
				//					System.out.println(" after reduction  " + node);
				//				}
				changed = changed || x;
				changed2 = changed2 || x;
			}
			//			System.out.println("");
		}

		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				changed2 = changed2 |= reduceNode(child, tree);
			}
		}
		return changed2;
	}
}
