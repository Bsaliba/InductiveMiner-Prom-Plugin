package org.processmining.plugins.InductiveMiner.mining.SAT.solve;

import java.lang.reflect.InvocationTargetException;

import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.SAT.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.SATResult;
import org.processmining.plugins.InductiveMiner.mining.SAT.solve.single.SATSolveSingle;

public abstract class SATSolve {

	protected final ThreadPool pool;
	protected final AtomicResult bestTillNow;
	protected final DirectlyFollowsRelation directlyFollowsRelation;
	protected final MiningParameters parameters;

	public SATSolve(DirectlyFollowsRelation directlyFollowsRelation, MiningParameters parameters, ThreadPool pool,
			AtomicResult bestTillNow) {
		this.directlyFollowsRelation = directlyFollowsRelation;
		this.parameters = parameters;
		this.pool = pool;
		this.bestTillNow = bestTillNow;
	}
	
	public abstract void solve();

	public void solveDefault(final Class<? extends SATSolveSingle> c, boolean commutative) {
		double maxCut;
		if (commutative) {
			maxCut = 0.5 + directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size() / 2;
		} else {
			maxCut = directlyFollowsRelation.getDirectlyFollowsGraph().vertexSet().size();
		}
		for (int i = 1; i < maxCut; i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = null;
					try {
						solver = c.getConstructor(DirectlyFollowsRelation.class, MiningParameters.class).newInstance(
								directlyFollowsRelation, parameters);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
					SATResult result = solver.solveSingle(j, bestTillNow.get().getProbability());
					if (result != null && result.getProbability() >= bestTillNow.get().getProbability()) {
						if (bestTillNow.maximumAndGet(result)) {
							debug("new maximum " + result);
						}
					}
				}
			});
		}
	}

	protected void debug(String x) {
		if (parameters.isDebug()) {
			System.out.println(x);
		}
	}
}
