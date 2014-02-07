package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.Miner2;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoop implements FallThrough {

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {

		//try to find a tau loop
		IMLog sublog = new IMLog();
		
		for (IMTrace trace : log) {
			filterTrace(sublog, trace, log.getCardinalityOf(trace), logInfo.getStartActivities());
		}

		if (sublog.size() > log.size()) {
			Miner2.debug(" fall through: tau loop", parameters);
			//making a tau loop split makes sense
			Block loop = new XorLoop("");
			loop.setProcessTree(tree);
			
			Node body = Miner2.mineNode(sublog, tree, parameters);
			loop.addChild(body);
			
			Node redo = new Automatic("");
			redo.setProcessTree(tree);
			loop.addChild(redo);
			
			Node exit = new Automatic("");
			exit.setProcessTree(tree);
			loop.addChild(exit);
			
			return loop;
		}

		return null;
	}
	
	public static void filterTrace(IMLog sublog, IMTrace trace, int cardinality, MultiSet<XEventClass> startActivities) {
		boolean first = true;
		IMTrace partialTrace = new IMTrace();
		for (XEventClass event: trace) {
			
			if (!first && startActivities.contains(event)) {
				//we discovered a transition body -> body
				sublog.add(partialTrace, cardinality);
				partialTrace = new IMTrace();
				first = true;
			}
			
			partialTrace.add(event);
			first = false;
		}
		sublog.add(partialTrace, cardinality);
	}
}
