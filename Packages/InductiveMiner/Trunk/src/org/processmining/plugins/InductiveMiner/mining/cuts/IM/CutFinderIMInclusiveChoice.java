package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.BitSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

public class CutFinderIMInclusiveChoice implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut cut = CutFinderIMConcurrentWithMinimumSelfDistance.findCutImpl(log, logInfo, minerState);

		if (!cut.isValid()) {
			return null;
		}

		/**
		 * We assume that the log does not contain empty traces, i.e. in each
		 * trace at least one part is executed. This is guaranteed by the empty
		 * log base case.
		 */

		/**
		 * A concurrent operator is an inclusive or operator if each of its
		 * children can be skipped. Walk through the event log to find this out.
		 */

		TObjectIntMap<XEventClass> activity2part = new TObjectIntHashMap<XEventClass>();
		{
			int i = 0;
			for (Set<XEventClass> part : cut.getPartition()) {
				for (XEventClass activity : part) {
					activity2part.put(activity, i);
				}
				i++;
			}
		}

		//walk through the traces to denote which parts see the empty trace
		int size = cut.getPartition().size();
		BitSet partsWithoutEmptyTraces = new BitSet(size);
		BitSet partsNotSeenEmpty = new BitSet(size);
		partsWithoutEmptyTraces.set(0, size);
		for (IMTrace trace : log) {
			partsNotSeenEmpty.set(0, size);
			for (IMEventIterator it = trace.iterator(); it.hasNext();) {
				it.next();
				partsNotSeenEmpty.set(activity2part.get(it.classify()), false);
			}
			partsWithoutEmptyTraces.andNot(partsNotSeenEmpty);
		}
		
		if (partsWithoutEmptyTraces.isEmpty()) {
			return new Cut(Operator.or, cut.getPartition());
		}
		return cut;
	}
}
