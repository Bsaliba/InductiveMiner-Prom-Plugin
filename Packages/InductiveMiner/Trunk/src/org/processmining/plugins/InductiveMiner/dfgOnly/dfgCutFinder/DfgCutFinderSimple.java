package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;

public class DfgCutFinderSimple implements DfgCutFinder {

	private static List<DfgCutFinder> cutFinders = new ArrayList<DfgCutFinder>(Arrays.asList(
			new CutFinderIMExclusiveChoice(),
			new CutFinderIMSequence(),
			new CutFinderIMParallel(),
			new CutFinderIMLoop()
			));

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		Cut c = null;
		Iterator<DfgCutFinder> it = cutFinders.iterator();
		while (it.hasNext() && (c == null || !c.isValid())) {
			c = it.next().findCut(dfg, minerState);
		}
		return c;
	}

}
