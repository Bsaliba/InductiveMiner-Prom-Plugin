package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import org.processmining.plugins.InductiveMiner.ThreadPoolMiner;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingleXor;

public class SATSolveXor extends SATSolve {

	public SATSolveXor(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPoolMiner pool, AtomicResult result) {
		super(directlyFollowsRelation, parameters, pool, result);
	}
	
	public void solve() {
		//debug("start SAT search for exclusive choice cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleXor.class, true);
	}

}
