package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.BitSet;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.IntShortLanguage;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopATauTau2flower;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopTauATau2flower;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.SameOperator;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.SingleChild;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.TauChildOfSeqAnd;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorTauTau;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorTauTauLoop2flower;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EfficientTreeReduce {
	public static EfficientTreeReductionRule[] rulesXor = new EfficientTreeReductionRule[] { new SingleChild(),
			new XorTauTau(), new SameOperator(), new XorTauTauLoop2flower() };
	public static EfficientTreeReductionRule[] rulesSeq = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator() };
	public static EfficientTreeReductionRule[] rulesAnd = new EfficientTreeReductionRule[] { new SingleChild(),
			new TauChildOfSeqAnd(), new SameOperator() };
	public static EfficientTreeReductionRule[] rulesLoop = new EfficientTreeReductionRule[] { new LoopATauTau2flower(),
			new LoopTauATau2flower() };
	public static EfficientTreeReductionRule[] rulesInt = new EfficientTreeReductionRule[] { new SingleChild(),
		new TauChildOfSeqAnd(), new SameOperator(), new IntShortLanguage() };

	public static void reduce(EfficientTree tree) throws ReductionFailedException {
		//filter epsilon subtrees
		{
			BitSet map = EfficientTreeMetrics.canOnlyProduceTau(tree);
			for (int node = tree.getTree().length - 1; node >= 0; node--) {
				if (map.get(node) && tree.isOperator(node)) {
					tree.replaceNodeWithTau(node);
				}
			}
		}

		//filter superfluous taus under xor, and, seq
		//		{
		//			BitSet canProduceTau = EfficientTreeMetrics.canProduceTau(tree);
		//			Pair<BitSet, int[]> p = isSuperfluousTau(tree, canProduceTau);
		//			BitSet map = p.getA();
		//			int[] parents = p.getB();
		//			for (int node = tree.getTree().length - 1; node >= 0; node--) {
		//				if (map.get(node)) {
		//					tree.removeChild(parents[node], node);
		//				}
		//			}
		//		}
		//this code works, but does not make reducing faster in repeated experiments

		//apply other filters
		while (reduceOne(tree)) {

		}

	}

	private static boolean reduceOne(EfficientTree tree) throws ReductionFailedException {
		boolean changed = false;

		for (int node = 0; node < tree.getTree().length; node++) {
			if (tree.isOperator(node)) {
				EfficientTreeReductionRule[] rules;
				if (tree.isXor(node)) {
					rules = rulesXor;
				} else if (tree.isSequence(node)) {
					rules = rulesSeq;
				} else if (tree.isLoop(node)) {
					rules = rulesLoop;
				} else if (tree.isConcurrent(node)) {
					rules = rulesAnd;
				} else if (tree.isInterleaved(node)) {
					rules = rulesInt;
				} else {
					throw new NotImplementedException();
				}

				for (EfficientTreeReductionRule rule : rules) {
					changed = changed | rule.apply(tree, node);
					if (!tree.isConsistent()) {
						throw new ReductionFailedException();
					}
				}
			}
		}

		return changed;
	}

	public static Pair<BitSet, int[]> isSuperfluousTau(EfficientTree tree, BitSet canProduceTau) {
		BitSet superfluous = new BitSet(tree.getTree().length);
		int[] parents = new int[tree.getTree().length];
		for (int node = tree.getTree().length - 1; node >= 0; node--) {
			if (tree.isSequence(node) || tree.isConcurrent(node)) {
				//any tau under a sequence or parallel can be removed
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {
						superfluous.set(child, true);
						parents[child] = node;
					}
				}
			} else if (tree.isXor(node)) {
				//see whether there's a child that can produce epsilon
				boolean childProducingEpsilon = false;
				for (int child : tree.getChildren(node)) {
					if (!tree.isTau(child)) {
						childProducingEpsilon |= canProduceTau.get(child);
					}
				}

				//walk again through the children and mark removal
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {
						//if we have preserved a child that can produce epsilon already; we can remove this tau
						if (childProducingEpsilon) {
							superfluous.set(child, true);
							parents[child] = node;
						} else {
							childProducingEpsilon = true;
						}
					}
				}
			}
		}
		return Pair.of(superfluous, parents);
	}

	public static class ReductionFailedException extends Exception {
		private static final long serialVersionUID = -7417483651057438248L;
	}
}
