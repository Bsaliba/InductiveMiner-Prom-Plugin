package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingleParallel;

public class SATSolveParallel extends SATSolve {

	public SATSolveParallel(CutFinderIMinInfo info, AtomicResult result) {
		super(info, result);
	}
	
	public void solve() {
		//debug("start SAT search for parallel cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleParallel.class, true);
	}
}
