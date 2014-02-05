package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import java.lang.reflect.InvocationTargetException;

import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.SATResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve.single.SATSolveSingle;

public abstract class SATSolve {

	protected final JobList pool;
	protected final AtomicResult bestTillNow;
	protected final LogInfo logInfo;
	protected final MiningParameters parameters;

	public SATSolve(LogInfo logInfo, MiningParameters parameters, JobList pool,
			AtomicResult bestTillNow) {
		this.logInfo = logInfo;
		this.parameters = parameters;
		this.pool = pool;
		this.bestTillNow = bestTillNow;
	}
	
	public abstract void solve();

	public void solveDefault(final Class<? extends SATSolveSingle> c, boolean commutative) {
		double maxCut;
		if (commutative) {
			maxCut = 0.5 + logInfo.getActivities().size() / 2;
		} else {
			maxCut = logInfo.getActivities().size();
		}
		for (int i = 1; i < maxCut; i++) {
			final int j = i;
			pool.addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = null;
					try {
						solver = c.getConstructor(LogInfo.class, MiningParameters.class).newInstance(
								logInfo, parameters);
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
