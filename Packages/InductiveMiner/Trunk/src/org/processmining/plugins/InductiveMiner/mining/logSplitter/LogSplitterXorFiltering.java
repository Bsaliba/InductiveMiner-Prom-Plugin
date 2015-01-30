package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class LogSplitterXorFiltering implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return split(log, cut.getPartition());
	}

	public static LogSplitResult split(IMLog log, Collection<Set<XEventClass>> partition) {

		//map activities to sigmas
		Map<XEventClass, Set<XEventClass>> mapActivity2sigma = new THashMap<XEventClass, Set<XEventClass>>();
		for (Set<XEventClass> sigma : partition) {
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}
		
		MultiSet<XEventClass> noise = new MultiSet<>();

		List<IMLog> result = new ArrayList<>();
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = new IMLog(log);
			for (Iterator<IMTrace> it = sublog.iterator(); it.hasNext();) {
				IMTrace trace = it.next();

				//walk through the events and count how many go in each sigma
				TObjectIntHashMap<Set<XEventClass>> eventCounter = new TObjectIntHashMap<Set<XEventClass>>();
				for (Set<XEventClass> sigma2 : partition) {
					eventCounter.put(sigma2, 0);
				}
				int maxCounter = 0;
				Set<XEventClass> maxSigma = null;
				for (XEvent event : trace) {
					Set<XEventClass> sigma2 = mapActivity2sigma.get(log.classify(event));
					int newCounter = eventCounter.get(sigma2) + 1;
					if (newCounter > maxCounter) {
						maxCounter = newCounter;
						maxSigma = sigma2;
					}
					eventCounter.put(sigma2, newCounter);
				}

				//determine whether this trace should go in this sublog
				if (trace.isEmpty()) {
					/*
					 * An empty trace should have been filtered out before
					 * reaching here. We have no information what trace could
					 * have produced it, so we keep it in all traces.
					 */
				} else if (maxSigma != sigma) {
					//remove trace
					it.remove();
				} else {
					//keep trace, remove all events not from sigma
					for (Iterator<XEvent> it2 = trace.iterator(); it2.hasNext();) {
						XEventClass c = sublog.classify(it2.next());
						if (!sigma.contains(c)) {
							it.remove();
						} else {
							noise.add(c);
						}
					}
				}
			}
			result.add(sublog);
		}
		
		return new LogSplitResult(result, noise);
	}
}
