package org.processmining.plugins.InductiveMiner.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.impl.AbstractBlock;

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

	public static List<ReductionPattern> patterns = new ArrayList<ReductionPattern>(Arrays.asList(new RPFlattenXor(),
			new RPFlattenAnd(), new RPFlattenSeq(), new RPDoubleTausUnderXor(), new RPFlattenLoop(), new RPOneChild()));

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
				} else {
					//non-root
					Block parent = node.getParents().iterator().next();
					for (int i = 0; i < parent.getChildren().size();i++) {
						if (parent.getChildren().get(i) == node) {
							flattenChildAt(parent, (Block) node, i, tree);
							return true;
						}
					}
				}
				return true;
			}
			return false;
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
				for (Automatic tau : taus) {
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
				}
			} else {
				//only the non-first taus can be removed
				Iterator<Automatic> it = taus.iterator();
				while (it.hasNext()) {
					Automatic tau = it.next();
					removeChild(xor, tau, tree);
					tree.removeNode(tau);
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
				if (child instanceof And && !(child instanceof Interleaved)) {
					changed = true;
					i = flattenChildAt((Block) node, (Block) child, i, tree);

					//for parallel, the number of traces represented is the same, so don't sum
				} else if (child instanceof Automatic) {
					//remove tau
					removeChild((Block) node, child, tree);
					tree.removeNode(child);
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
				} else if (child instanceof Automatic) {
					//remove tau
					removeChild((Block) node, child, tree);
					tree.removeNode(child);
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

		//remove old node
		tree.removeNode(child);

		return i;
	}

	private static boolean reduceNode(Node node, ProcessTree tree) {
		boolean changed = true;
		boolean changed2 = false;
		while (changed) {
			changed = false;
			for (ReductionPattern pattern : patterns) {
				boolean x = pattern.apply(node, tree);
				changed = changed || x;
				changed2 = changed2 || x;
			}
		}
		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				changed2 = changed2 || reduceNode(child, tree);
			}
		}
		return changed2;
	}
}
