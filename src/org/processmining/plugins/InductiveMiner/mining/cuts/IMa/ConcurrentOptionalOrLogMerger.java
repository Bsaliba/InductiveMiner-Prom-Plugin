package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.graphs.Components;

public class ConcurrentOptionalOrLogMerger {

	/**
	 * Merge the mentioned components in both the log and the components object.
	 * 
	 * @param components
	 *            This is changed in place.
	 * @param oldLog
	 *            This is not changed.
	 * @param componentA
	 * @param componentB
	 * @return The new log.
	 */
	public static ConcurrentOptionalOrLog merge(Components<XEventClass> components, ConcurrentOptionalOrLog oldLog,
			int componentA, int componentB) {
		components.mergeComponents(componentA, componentB);
		int[] old2new = components.normalise();
		old2new[componentA] = old2new[componentB];
		return ConcurrentOptionalOrLog.mergeConcurrent(oldLog, old2new, components.getNumberOfComponents());
	}
}
