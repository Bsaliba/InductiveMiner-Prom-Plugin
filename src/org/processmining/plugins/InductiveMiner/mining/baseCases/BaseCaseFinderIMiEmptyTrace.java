package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.Miner2;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiEmptyTrace implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		//this clause is not proven in the paper
		if (logInfo.getNumberOfEpsilonTraces() != 0) {
			//the log contains empty traces

			if (logInfo.getNumberOfEpsilonTraces() < logInfo.getLengthStrongestTrace() * parameters.getNoiseThreshold()) {
				//there are not enough empty traces, the empty traces are considered noise

				//filter the empty traces from the log and recurse
				log.remove(new IMTrace());

				return Miner2.mineNode(log, tree, parameters);

			} else {
				//There are too many empty traces to consider them noise.
				//Mine an xor(tau, ..) and recurse.
				Block newNode = new AbstractBlock.Xor("");
				newNode.setProcessTree(tree);

				//add tau
				Node tau = new AbstractTask.Automatic("");
				tau.setProcessTree(tree);
				newNode.addChild(tau);

				//filter empty traces
				log.remove(new IMTrace());

				//recurse
				Node child = Miner2.mineNode(log, tree, parameters);
				newNode.addChild(child);

				return newNode;
			}
		}
		return null;
	}
}
