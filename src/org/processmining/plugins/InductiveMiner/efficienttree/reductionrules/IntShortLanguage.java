package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class IntShortLanguage implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isInterleaved(node)) {
			//no child should produce a trace of length two or longer
			for (int child : tree.getChildren(node)) {
				if (!traceLengthAtMostOne(tree, child)) {
					return false;
				}
			}

			//transform the interleaved operator into a parallel operator
			tree.getTree()[node] += EfficientTree.concurrent - EfficientTree.interleaved;
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @return whether each trace of the node has a length of at most one.
	 */
	public boolean traceLengthAtMostOne(EfficientTree tree, int node) {
		if (tree.isActivity(node)) {
			return true;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isOperator(node)) {
			if (tree.isXor(node)) {
				for (int child : tree.getChildren(node)) {
					if (!traceLengthAtMostOne(tree, child)) {
						return false;
					}
				}
				return true;
			} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
				//one child can produce a singleton trace, the others cannot anymore then
				boolean singletonTraceChildSeen = false;
				for (int child : tree.getChildren(node)) {
					if (!onlyEmptyTrace(tree, node)) {
						//empty children are not worrying
						if (!traceLengthAtMostOne(tree, child)) {
							return false;
						}
						if (singletonTraceChildSeen) {
							return false;
						}
						singletonTraceChildSeen = true;
					}
				}
				return true;
			} else if (tree.isLoop(node)) {
				return onlyEmptyTrace(tree, node);
			}
		}
		throw new NotImplementedException();
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @return whether each trace of the node has a length of at most zero.
	 */
	public boolean onlyEmptyTrace(EfficientTree tree, int node) {
		if (tree.isActivity(node)) {
			return false;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isOperator(node)) {
			for (int child : tree.getChildren(node)) {
				if (!onlyEmptyTrace(tree, child)) {
					return false;
				}
			}
			return true;
		}
		throw new NotImplementedException();
	}
}
