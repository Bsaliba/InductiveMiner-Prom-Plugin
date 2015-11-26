package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingleParallel;

public class SATSolveParallel extends SATSolve {

	public SATSolveParallel(CutFinderIMinInfo info, AtomicResult result, Canceller canceller) {
		super(info, result, canceller);
	}
	
	public void solve() {
		//debug("start SAT search for parallel cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleParallel.class, true);
	}
}
