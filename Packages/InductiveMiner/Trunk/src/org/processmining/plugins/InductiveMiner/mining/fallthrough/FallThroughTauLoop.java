package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoop implements FallThrough {

	public Node fallThrough(IMLog2 log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().toSet().size() > 1) {

			//try to find a tau loop
			XLog sublog = new XLogImpl(new XAttributeMapImpl());

			for (IMTrace2 trace : log) {
				filterTrace(log, sublog, trace, logInfo.getStartActivities());
			}

			if (sublog.size() > log.size()) {
				Miner.debug(" fall through: tau loop", minerState);
				//making a tau loop split makes sense
				Block loop = new XorLoop("");
				Miner.addNode(tree, loop);

				{
					Node body = Miner.mineNode(new IMLog(sublog, minerState.parameters.getClassifier()), tree, minerState);
					loop.addChild(body);
				}

				{
					Node redo = new Automatic("tau");
					Miner.addNode(tree, redo);
					loop.addChild(redo);
				}

				{
					Node exit = new Automatic("tau");
					Miner.addNode(tree, exit);
					loop.addChild(exit);
				}

				return loop;
			}
		}

		return null;
	}

	public static void filterTrace(IMLog2 log, XLog sublog, IMTrace2 trace, MultiSet<XEventClass> startActivities) {
		boolean first = true;
		XTrace partialTrace = new XTraceImpl(new XAttributeMapImpl());
		for (XEvent event : trace) {

			if (!first && startActivities.contains(log.classify(event))) {
				//we discovered a transition body -> body
				sublog.add(partialTrace);
				partialTrace = new XTraceImpl(new XAttributeMapImpl());
				first = true;
			}

			partialTrace.add(event);
			first = false;
		}
		sublog.add(partialTrace);
	}
}
