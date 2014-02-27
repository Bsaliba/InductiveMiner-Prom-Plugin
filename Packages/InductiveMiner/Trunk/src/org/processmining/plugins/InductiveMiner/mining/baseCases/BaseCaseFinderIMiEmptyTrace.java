package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiEmptyTrace implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		//this clause is not proven in the paper
		if (logInfo.getNumberOfEpsilonTraces() != 0) {
			//the log contains empty traces

			if (logInfo.getNumberOfEpsilonTraces() < logInfo.getLengthStrongestTrace() * minerState.parameters.getNoiseThreshold()) {
				//there are not enough empty traces, the empty traces are considered noise
				
				int numberOfEpsilonTraces = log.getCardinalityOf(new IMTrace());
				

				//filter the empty traces from the log and recurse
				log.remove(new IMTrace());
				
				Node newNode = Miner.mineNode(log, tree, minerState);
				MinerMetrics.attachNumberOfEventsDiscarded(newNode, numberOfEpsilonTraces);
				
				return newNode;

			} else {
				//There are too many empty traces to consider them noise.
				//Mine an xor(tau, ..) and recurse.
				Block newNode = new AbstractBlock.Xor("");
				newNode.setProcessTree(tree);
				MinerMetrics.attachNumberOfTracesRepresented(newNode, logInfo);

				//add tau
				Node tau = new AbstractTask.Automatic("tau");
				tau.setProcessTree(tree);
				newNode.addChild(tau);
				MinerMetrics.attachNumberOfTracesRepresented(tau, (int) logInfo.getNumberOfEpsilonTraces());
				MinerMetrics.attachNumberOfEventsDiscarded(tau, 0);

				//filter empty traces
				log.remove(new IMTrace());

				//recurse
				Node child = Miner.mineNode(log, tree, minerState);
				newNode.addChild(child);

				return newNode;
			}
		}
		return null;
	}
}
