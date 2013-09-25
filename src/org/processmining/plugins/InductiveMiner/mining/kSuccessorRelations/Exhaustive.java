package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.ThreadPool;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.FilterResults;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;

public class Exhaustive {

	public class Result {
		int distance;
		public String cutType;
		public Collection<Set<XEventClass>> cut;
		public Collection<Filteredlog> sublogs;
	}

	private UpToKSuccessorRelation kSuccessor;
	private Filteredlog log;
	private MiningParameters parameters;
	private ThreadPool pool;
	private final AtomicInteger bestTillNow;

	public Exhaustive(Filteredlog log, UpToKSuccessorRelation kSuccessor, MiningParameters parameters) {
		this.kSuccessor = kSuccessor;
		this.log = log;
		this.parameters = parameters;
		bestTillNow = new AtomicInteger();
	}

	public Result tryAll() {
		final int nrOfBits = log.getEventClasses().size();

		final XEventClass[] activities = new XEventClass[log.getEventClasses().size()];
		int i = 0;
		for (XEventClass e : log.getEventClasses()) {
			activities[i] = e;
			i++;
		}

		pool = ThreadPool.useFactor(2);
		int threads = pool.getNumerOfThreads();
		final Result[] results = new Result[threads];
		bestTillNow.set(Integer.MAX_VALUE);

		long globalStartCutNr = 1;
		long globalEndCutNr = (int) (Math.pow(2, nrOfBits) - 1);

		long lastEnd = globalStartCutNr - 1;
		long step = (globalEndCutNr - globalStartCutNr) / threads;

		//debug("Start threads " + globalStartCutNr + " " + globalEndCutNr);

		for (int t = 0; t < threads; t++) {
			final long startCutNr = lastEnd + 1;
			final long endCutNr = startCutNr + step;
			lastEnd = endCutNr;
			final int threadNr = t;
			//debug("Start thread  " + startCutNr + " " + endCutNr);
			pool.addJob(new Runnable() {
				public void run() {
					results[threadNr] = tryRange(nrOfBits, activities, startCutNr, endCutNr);
				}
			});
		}

		try {
			pool.join();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}

		Result result = new Result();
		result.distance = Integer.MAX_VALUE;
		for (int t = 0; t < threads; t++) {
			if (results[t].distance < result.distance) {
				result = results[t];
			}
		}

		return result;
	}

	public Result tryRange(int nrOfBits, final XEventClass[] activities, long startCutNr, long endCutNr) {
		Result result = new Result();
		result.distance = Integer.MAX_VALUE;
		Result result2;
		List<Set<XEventClass>> cut;
		for (long cutNr = startCutNr; cutNr < Math.pow(2, nrOfBits) - 1 && result.distance > 0 && cutNr < endCutNr; cutNr++) {
			cut = generateCut(cutNr, nrOfBits, activities);

			//parallel
			result2 = processCutParallel(cut);
			if (result.distance > result2.distance) {
				result = result2;
				if (updateBestTillNow(result2.distance)) {
					debug(result2.distance + " " + result2.cutType + " " + cut.toString(), parameters);
				}
			}

			//loop
			result2 = processCutLoop(cut);
			if (result.distance > result2.distance) {
				result = result2;
				if (updateBestTillNow(result2.distance)) {
					debug(result2.distance + " " + result2.cutType + " " + cut.toString(), parameters);
				}
			}
		}

		return result;
	}

	public Result processCutParallel(Collection<Set<XEventClass>> cut) {

		Result result = new Result();

		//split log
		FilterResults filterResults = log.applyFilterParallel(new HashSet<Set<XEventClass>>(cut));
		result.sublogs = filterResults.sublogs;

		//make k-successor relations
		Iterator<Filteredlog> it = result.sublogs.iterator();
		UpToKSuccessorRelation successor0 = new UpToKSuccessorRelation(it.next(), parameters);
		UpToKSuccessorRelation successor1 = new UpToKSuccessorRelation(it.next(), parameters);

		//combine the logs
		UpToKSuccessorRelation combined = (new CombineParallel()).combine(successor0.getkSuccessors(),
				successor1.getkSuccessors());

		result.distance = (new DistanceEuclidian()).computeDistance(kSuccessor.getkSuccessors(),
				combined.getkSuccessors());

		result.cut = cut;
		result.cutType = "parallel";

		return result;
	}

	public Result processCutLoop(Collection<Set<XEventClass>> cut) {

		Result result = new Result();

		//split log
		FilterResults filterResults = log.applyFilterLoop(new ArrayList<Set<XEventClass>>(cut));
		result.sublogs = filterResults.sublogs;

		//make k-successor relations
		Iterator<Filteredlog> it = result.sublogs.iterator();
		UpToKSuccessorRelation successor0 = new UpToKSuccessorRelation(it.next(), parameters);
		UpToKSuccessorRelation successor1 = new UpToKSuccessorRelation(it.next(), parameters);

		//combine the logs
		UpToKSuccessorRelation combined = (new CombineLoop()).combine(successor0.getkSuccessors(),
				successor1.getkSuccessors());

		result.distance = (new DistanceEuclidian()).computeDistance(kSuccessor.getkSuccessors(),
				combined.getkSuccessors());

		result.cut = cut;
		result.cutType = "loop";

		return result;
	}

	public List<Set<XEventClass>> generateCut(long input, int nrOfBits, XEventClass[] activities) {

		List<Set<XEventClass>> result = new LinkedList<Set<XEventClass>>();
		Set<XEventClass> a = new HashSet<XEventClass>();
		Set<XEventClass> b = new HashSet<XEventClass>();

		for (int i = nrOfBits - 1; i >= 0; i--) {
			if ((input & (1 << i)) != 0) {
				a.add(activities[i]);
			} else {
				b.add(activities[i]);
			}
		}

		result.add(a);
		result.add(b);

		return result;
	}

	private boolean updateBestTillNow(int newBest) {
		int now = bestTillNow.get();
		if (now > newBest) {
			while (!bestTillNow.compareAndSet(now, newBest)) {
				now = bestTillNow.get();
				if (now <= newBest) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void debug(String x, MiningParameters parameters) {
		if (parameters.isDebug()) {
			System.out.println(x);
		}
	}

	private void debug(String x) {

		System.out.println(x);

	}
}
