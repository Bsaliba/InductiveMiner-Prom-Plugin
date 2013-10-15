package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public abstract class SATSolve {
	
	protected final ThreadPool pool;
	protected final AtomicResult bestTillNow;
	protected final DirectlyFollowsRelation directlyFollowsRelation;
	protected final MiningParameters parameters;

	public SATSolve(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPool pool, AtomicResult bestTillNow) {
		this.directlyFollowsRelation = directlyFollowsRelation;
		this.parameters = parameters;
		this.pool = pool;
		this.bestTillNow = bestTillNow;
	}

	public abstract void solve();

	protected void debug(String x) {
		if (parameters.isDebug()) {
			System.out.println(x);
		}
	}
}
