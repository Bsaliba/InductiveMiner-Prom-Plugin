package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoopStrict implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().toSet().size() > 1) {

			//try to find a tau loop
			IMLog sublog = log.clone();
			if (filterLog(sublog, logInfo.getDfg())) {

				Miner.debug(" fall through: tau loop strict", minerState);
				//making a tau loop split makes sense
				Block loop = new XorLoop("");
				Miner.addNode(tree, loop);

				{
					Node body = Miner.mineNode(sublog, tree, minerState);
					Miner.addChild(loop, body, minerState);
				}

				{
					Node redo = new Automatic("tau");
					Miner.addNode(tree, redo);
					Miner.addChild(loop, redo, minerState);
				}

				{
					Node exit = new Automatic("tau");
					Miner.addNode(tree, exit);
					Miner.addChild(loop, exit, minerState);
				}

				return loop;
			}
		}

		return null;
	}

	/**
	 * Split the trace on a crossing end -> start
	 * 
	 * @param log
	 * @param dfg
	 * @return
	 */
	public static boolean filterLog(IMLog log, Dfg dfg) {
		boolean somethingSplit = false;

		for (IMTrace trace : log) {
			boolean lastEnd = false;
			IMEventIterator it = trace.iterator();
			while (it.hasNext()) {

				it.next();

				XEventClass activity = it.classify();

				if (lastEnd && dfg.isStartActivity(activity)) {
					it.split();
					somethingSplit = true;
				}

				lastEnd = dfg.isEndActivity(activity);
			}
		}
		return somethingSplit;
	}
}
