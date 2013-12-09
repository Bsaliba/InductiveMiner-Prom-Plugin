package org.processmining.plugins.InductiveMiner;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ThreadPoolConcurrentJobs {

	private ThreadPoolSingleton pool;
	private ConcurrentLinkedQueue<Future<?>> jobs;
	private int id;

	//constructor, makes an estimate of the number of threads.
	public ThreadPoolConcurrentJobs(int numberOfThreads) {
		pool = ThreadPoolSingleton.getInstance(numberOfThreads);
		jobs = new ConcurrentLinkedQueue<Future<?>>();
		
		id = (new Random()).nextInt();
		//System.out.println("start job pool " + id);
	}

	public static ThreadPoolConcurrentJobs useAsMuchThreadsAsCores() {
		return new ThreadPoolConcurrentJobs(Runtime.getRuntime().availableProcessors());
	}

	public static ThreadPoolConcurrentJobs useBlocking() {
		return new ThreadPoolConcurrentJobs(1);
	}

	//add a job to be executed. Will block if executed synchronously
	public synchronized void addJob(Runnable job) {
		//System.out.println("add job to pool " + id + ", " + jobs.size() + " jobs remaining");
		job.run();
		/*Future<?> x = pool.submit(job);
		if (x != null) {
			jobs.add(x);
		}*/
	}

	//wait till all jobs have finished execution. While waiting, new jobs can still be added and will be executed.
	//Hence, will block until the thread pool is idle
	public void join() throws ExecutionException {

		/*
		//System.out.println("join pool " + id + ", " + jobs.size() + " jobs remaining");
		
		//wait for all jobs to finish
		while (!jobs.isEmpty()) {
			Future<?> job = jobs.poll();
			//System.out.println("job ended in pool " + id + ", " + jobs.size() + " jobs remaining");
			try {
				job.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		*/
		
		//System.out.println("terminate job pool " + id);

	}
}
