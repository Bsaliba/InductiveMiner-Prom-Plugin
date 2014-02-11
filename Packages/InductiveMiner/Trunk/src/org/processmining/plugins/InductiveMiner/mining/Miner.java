package org.processmining.plugins.InductiveMiner.mining;

import java.util.Collection;
import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class Miner {
	/*
	 * Do not directly call this class, use one of the plugins from the
	 * InductiveMiner.plugins folder
	 */

	public static ProcessTree mine(IMLog log, MiningParameters parameters) {
		//create process tree
		ProcessTree tree = new ProcessTreeImpl();
		Node root = mineNode(log, tree, parameters);

		root.setProcessTree(tree);
		tree.setRoot(root);

		return tree;
	}

	public static Node mineNode(IMLog log, ProcessTree tree, MiningParameters parameters) {

		//construct basic information about log
		IMLogInfo logInfo = new IMLogInfo(log);

		//output information about the log
		debug("\nMine " + logInfo.getActivities(), parameters);
		//debug(log, parameters);
		//debug(logInfo, parameters);

		//find base cases
		Node baseCase = findBaseCases(log, logInfo, tree, parameters);
		if (baseCase != null) {

			debug(" base case: " + baseCase.getName(), parameters);

			return baseCase;
		}

		//find cut
		Cut cut = findCut(log, logInfo, tree, parameters);
		if (cut != null && cut.isValid()) {
			//cut is valid

			debug(" chosen cut: " + cut, parameters);

			//split logs
			Collection<IMLog> sublogs = splitLog(log, logInfo, cut, parameters);

			//make node
			Block newNode = (Block) MinerMetrics.attachStatistics(newNode(cut.getOperator()), logInfo);
			newNode.setProcessTree(tree);

			//recurse
			if (cut.getOperator() != Operator.loop) {
				for (IMLog sublog : sublogs) {
					Node child = mineNode(sublog, tree, parameters);
					newNode.addChild(child);
				}
			} else {
				//loop needs special treatment:
				//ProcessTree requires a ternary loop
				Iterator<IMLog> it = sublogs.iterator();

				//mine body
				{
					IMLog sublog = it.next();
					Node child = mineNode(sublog, tree, parameters);
					newNode.addChild(child);
				}

				//mine redo parts by, if necessary, putting them under an xor
				Block redoXor;
				if (sublogs.size() > 2) {
					redoXor = new Xor("");
					redoXor.setProcessTree(tree);
					newNode.addChild(redoXor);
				} else {
					redoXor = newNode;
				}
				while (it.hasNext()) {
					IMLog sublog = it.next();
					Node child = mineNode(sublog, tree, parameters);
					redoXor.addChild(child);
				}

				//add tau as third child
				{
					Node tau = new AbstractTask.Automatic("");
					tau.setProcessTree(tree);
					newNode.addChild(tau);
				}
			}

			return newNode;

		} else {
			//cut is not valid; fall through
			return findFallThrough(log, logInfo, tree, parameters);
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

	private static Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		Node n = null;
		Iterator<BaseCaseFinder> it = parameters.getBaseCaseFinders().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().findBaseCases(log, logInfo, tree, parameters);
		}
		return n;
	}

	private static Cut findCut(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		Cut c = null;
		Iterator<CutFinder> it = parameters.getCutFinders().iterator();
		while (it.hasNext() && (c == null || !c.isValid())) {
			c = it.next().findCut(log, logInfo, parameters);
		}
		return c;
	}

	private static Node findFallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		Node n = null;
		Iterator<FallThrough> it = parameters.getFallThroughs().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().fallThrough(log, logInfo, tree, parameters);
		}
		return n;
	}

	private static Collection<IMLog> splitLog(IMLog log, IMLogInfo logInfo, Cut cut, MiningParameters parameters) {
		return parameters.getLogSplitter().split(log, logInfo, cut);
	}

	public static void debug(Object x, MiningParameters parameters) {
		if (parameters.isDebug()) {
			System.out.println(x.toString());
		}
	}
}
