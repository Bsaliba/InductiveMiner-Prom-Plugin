package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2;

public class LogSplitterParallel implements LogSplitter {

	public LogSplitResult split(IMLog2 log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return new LogSplitResult(split(log, cut.getPartition()), new MultiSet<XEventClass>());
	}
	
	public static List<IMLog> split(IMLog2 log, Collection<Set<XEventClass>> partition) {
		List<IMLog> result = new ArrayList<>();
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = new IMLog(log);
			for (IMTrace2 trace : sublog) {
				for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
					XEventClass c = sublog.classify(it.next());
					if (!sigma.contains(c)) {
						it.remove();
					}
				}
			}
			result.add(sublog);
		}
		return result;
	}

}
