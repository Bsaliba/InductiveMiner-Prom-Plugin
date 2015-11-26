package org.processmining.plugins.InductiveMiner.mining;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;

import com.google.common.util.concurrent.MoreExecutors;

public class MinerState {
	public final MultiSet<XEventClass> discardedEvents;
	public final MiningParameters parameters;
	private final ExecutorService minerPool;
	private final ExecutorService satPool;

	public MinerState(MiningParameters parameters) {
		this.parameters = parameters;
		this.discardedEvents = new MultiSet<XEventClass>();
		
		if (!parameters.isUseMultithreading()) {
			minerPool = MoreExecutors.sameThreadExecutor();
			satPool = MoreExecutors.sameThreadExecutor();
		} else {
			minerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			satPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		}
	}
	
	public ExecutorService getMinerPool() {
		return minerPool;
	}
	
	public ExecutorService getSatPool() {
		return satPool;
	}
	
	public void shutdownThreadPools() {
		minerPool.shutdownNow();
		satPool.shutdownNow();
	}
}
