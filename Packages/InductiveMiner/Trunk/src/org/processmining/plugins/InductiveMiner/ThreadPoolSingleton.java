package org.processmining.plugins.InductiveMiner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolSingleton {
	private ExecutorService pool;
	private int numberOfThreads;

	//singleton
	//private static ThreadPoolSingleton instance = null;
	private ThreadPoolSingleton instance = null;

	private ThreadPoolSingleton(int numberOfThreads) {
		// Exists only to defeat instantiation.
		pool = Executors.newFixedThreadPool(numberOfThreads);
		this.numberOfThreads = numberOfThreads;
		System.out.println("new thread pool of " + numberOfThreads);
	}

	//constructor, takes a number of threads. Provide 1 to execute synchronously.
	public static ThreadPoolSingleton getInstance(int numberOfThreads) {
		//if (instance == null) {
		ThreadPoolSingleton instance = new ThreadPoolSingleton(numberOfThreads);
		//}
		return instance;
	}

	//constructor, makes an estimate of the number of threads.
	public static ThreadPoolSingleton getInstance() {
		return getInstance(Runtime.getRuntime().availableProcessors());
	}

	//constructor, use a percentage of available cores
	public static ThreadPoolSingleton useFactor(double factor) {
		return getInstance((int) (Runtime.getRuntime().availableProcessors() * factor));
	}

	public synchronized Future<?> submit(Runnable job) {
		//System.out.println("submit job");
		/*if (numberOfThreads > 1) {
			return pool.submit(job);
		} else {*/
			//System.out.println("run synchronous");
			job.run();
			return null;
		//}
	}
}
