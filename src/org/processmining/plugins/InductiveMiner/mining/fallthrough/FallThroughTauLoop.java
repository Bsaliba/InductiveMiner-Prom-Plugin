package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.metrics.MinerMetrics;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoop implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().toSet().size() > 1) {

			//try to find a tau loop
			IMLog sublog = new IMLog();
			long numberOfTimesTauTaken = 0;

			for (IMTrace trace : log) {
				numberOfTimesTauTaken += filterTrace(sublog, trace, log.getCardinalityOf(trace),
						logInfo.getStartActivities());
			}

			if (sublog.size() > log.size()) {
				Miner.debug(" fall through: tau loop", minerState);
				//making a tau loop split makes sense
				Block loop = new XorLoop("");
				Miner.addNode(tree, loop, logInfo.getNumberOfTraces(), 0l, 0l, "fall through: tau loop");

				{
					Node body = Miner.mineNode(sublog, tree, minerState);
					MinerMetrics.attachProducer(body, "fall through: tau loop, " + MinerMetrics.getProducer(body));
					loop.addChild(body);
				}

				{
					Node redo = new Automatic("tau");
					Miner.addNode(tree, redo, numberOfTimesTauTaken, 0l, 0l, "fall through: tau loop");
					loop.addChild(redo);
				}

				{
					Node exit = new Automatic("tau");
					Miner.addNode(tree, exit, logInfo.getNumberOfTraces(), 0l, 0l, "fall through: tau loop");
					loop.addChild(exit);
				}

				return loop;
			}
		}

		return null;
	}

	public static long filterTrace(IMLog sublog, IMTrace trace, long cardinality, MultiSet<XEventClass> startActivities) {
		boolean first = true;
		long numberOfTimesTauTaken = 0;
		IMTrace partialTrace = new IMTrace();
		for (XEventClass event : trace) {

			if (!first && startActivities.contains(event)) {
				//we discovered a transition body -> body
				sublog.add(partialTrace, cardinality);
				partialTrace = new IMTrace();
				first = true;

				numberOfTimesTauTaken += cardinality;
			}

			partialTrace.add(event);
			first = false;
		}
		sublog.add(partialTrace, cardinality);
		return numberOfTimesTauTaken;
	}
}
