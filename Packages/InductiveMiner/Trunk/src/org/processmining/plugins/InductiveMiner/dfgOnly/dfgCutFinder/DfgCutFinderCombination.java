package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class DfgCutFinderCombination implements DfgCutFinder {
	private final DfgCutFinder[] cutFinders;

	public DfgCutFinderCombination(DfgCutFinder... cutFinders) {
		this.cutFinders = cutFinders;
	}

	public Cut findCut(Dfg dfg, DfgMinerState minerState, Canceller canceller) {
		for (int i = 0; i < cutFinders.length; i++) {
			Cut c = cutFinders[i].findCut(dfg, minerState, canceller);
			if (c != null && c.isValid()) {
				return c;
			}
		}
		return null;
	}
}
