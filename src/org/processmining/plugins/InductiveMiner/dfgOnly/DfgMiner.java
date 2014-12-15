package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.conversion.ReduceTree;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.DfgSplitter.DfgSplitResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class DfgMiner {
	public static ProcessTree mine(Dfg dfg, DfgMiningParameters parameters) {
		//create process tree
		ProcessTree tree = new ProcessTreeImpl();
		DfgMinerState minerState = new DfgMinerState(parameters);
		Node root = mineNode(dfg, tree, minerState);

		root.setProcessTree(tree);
		tree.setRoot(root);

		debug(tree.getRoot(), minerState);

		//reduce if necessary
		if (parameters.isReduce()) {
			ReduceTree.reduceTree(tree);
			debug("after reduction " + tree.getRoot(), minerState);
		}

		return tree;
	}
	
	public static Node mineNode(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		
		//find base cases
		Node baseCase = findBaseCases(dfg, tree, minerState);
		if (baseCase != null) {
			return baseCase;
		}

		//find cut
		Cut cut = findCut(dfg, minerState);
		if (cut != null && cut.isValid()) {
			//cut is valid

			debug(" chosen cut: " + cut, minerState);

			//split logs
			DfgSplitResult splitResult = splitLog(dfg, cut, minerState);

			//make node
			Block newNode = newNode(cut.getOperator());
			addNode(tree, newNode);

			//recurse
			if (cut.getOperator() != Operator.loop) {
				for (Dfg subDfg : splitResult.subDfgs) {
					Node child = mineNode(subDfg, tree, minerState);
					newNode.addChild(child);
				}
			} else {
				//loop needs special treatment:
				//ProcessTree requires a ternary loop
				Iterator<Dfg> it = splitResult.subDfgs.iterator();

				//mine body
				Dfg firstSubDfg = it.next();
				{
					Node firstChild = mineNode(firstSubDfg, tree, minerState);
					newNode.addChild(firstChild);
				}

				//mine redo parts by, if necessary, putting them under an xor
				Block redoXor;
				if (splitResult.subDfgs.size() > 2) {
					redoXor = new Xor("");
					addNode(tree, redoXor);
					newNode.addChild(redoXor);
				} else {
					redoXor = newNode;
				}
				while (it.hasNext()) {
					Dfg subDfg = it.next();
					Node child = mineNode(subDfg, tree, minerState);
					redoXor.addChild(child);
				}

				//add tau as third child
				{
					Node tau = new AbstractTask.Automatic("tau");
					addNode(tree, tau);
					newNode.addChild(tau);
				}
			}

			return newNode;

		} else {
			//cut is not valid; fall through
			return findFallThrough(dfg, tree, minerState);
		}
	}

	private static Block newNode(Operator operator) {
		if (operator == Operator.xor) {
			return new AbstractBlock.Xor("");
		} else if (operator == Operator.sequence) {
			return new AbstractBlock.Seq("");
		} else if (operator == Operator.parallel) {
			return new AbstractBlock.And("");
		} else if (operator == Operator.loop) {
			return new AbstractBlock.XorLoop("");
		}
		return null;
	}

	public static void addNode(ProcessTree tree, Node node) {
		node.setProcessTree(tree);
		tree.addNode(node);
	}
	
	public static Node findBaseCases(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		Node n = null;
		Iterator<DfgBaseCaseFinder> it = minerState.getParameters().getDfgBaseCaseFinders().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().findBaseCases(dfg, tree, minerState);
		}
		return n;
	}

	public static Cut findCut(Dfg dfg, DfgMinerState minerState) {
		return minerState.getParameters().getDfgCutFinder().findCut(dfg, minerState);
	}

	public static Node findFallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		Node n = null;
		Iterator<DfgFallThrough> it = minerState.getParameters().getDfgFallThroughs().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().fallThrough(dfg, tree, minerState);
		}
		return n;
	}

	public static DfgSplitResult splitLog(Dfg dfg, Cut cut, DfgMinerState minerState) {
		return minerState.getParameters().getDfgSplitter().split(dfg, cut, minerState);
	}

	public static void debug(Object x, DfgMinerState minerState) {
		if (minerState.getParameters().isDebug()) {
			System.out.println(x.toString());
		}
	}
}
