package org.processmining.plugins.InductiveMiner.jobList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolSingleton3 {

	//singleton
	private static ExecutorService instance = null;

	//constructor
	public static ExecutorService getInstance() {
		if (instance == null) {
			int numberOfThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
			instance = Executors.newFixedThreadPool(numberOfThreads);
		}
		return instance;
	}
	
	
}
