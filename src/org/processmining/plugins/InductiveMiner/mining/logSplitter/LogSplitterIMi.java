package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;

public class LogSplitterIMi implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		List<IMLog> result = new LinkedList<IMLog>();
		MultiSet<XEventClass> noise = new MultiSet<XEventClass>();

		//map activities to sigmas
		HashMap<Set<XEventClass>, IMLog> mapSigma2sublog = new HashMap<Set<XEventClass>, IMLog>();
		HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		for (Set<XEventClass> sigma : cut.getPartition()) {
			IMLog sublog = new IMLog();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}

		for (IMTrace trace : log) {
			if (cut.getOperator() == Operator.xor) {
				splitXor(result, trace, cut.getPartition(), log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			} else if (cut.getOperator() == Operator.sequence) {
				splitSequence(result, trace, cut.getPartition(), log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			} else if (cut.getOperator() == Operator.parallel) {
				splitParallel(result, trace, cut.getPartition(), log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			} else if (cut.getOperator() == Operator.loop) {
				splitLoop(result, trace, cut.getPartition(), log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			}
		}

		return new LogSplitResult(result, noise);
	}

	public static void splitXor(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			int cardinality, HashMap<Set<XEventClass>, IMLog> mapSigma2sublog,
			HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {

		if (trace.size() == 0) {
			//an empty trace should have been filtered as a base case, but now we have to handle it
			//we cannot know in which branch the empty trace should go, so add it to all
			for (IMLog sublog : result) {
				sublog.add(trace, cardinality);
			}
			return;
		}

		//walk through the events and count how many go in each sigma
		HashMap<Set<XEventClass>, Integer> eventCounter = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : partition) {
			eventCounter.put(sigma, 0);
		}
		int maxCounter = 0;
		Set<XEventClass> maxSigma = null;
		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			int newCounter = eventCounter.get(sigma) + 1;
			if (newCounter > maxCounter) {
				maxCounter = newCounter;
				maxSigma = sigma;
			}
			eventCounter.put(sigma, newCounter);
		}

		//make a copy of the trace, leaving out the noise
		IMTrace newTrace = new IMTrace();
		for (XEventClass event : trace) {
			if (maxSigma.contains(event)) {
				//non-noise event
				newTrace.add(event);
			} else {
				//noise event
				noise.add(event, cardinality);
			}
		}

		IMLog sublog = mapSigma2sublog.get(maxSigma);
		sublog.add(newTrace, cardinality);
		mapSigma2sublog.put(maxSigma, sublog);
	}

	public static void splitSequence(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			int cardinality, HashMap<Set<XEventClass>, IMLog> mapSigma2sublog,
			HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {
		int atPosition = 0;
		int lastPosition = 0;
		IMTrace newTrace;
		Set<XEventClass> ignore = new HashSet<XEventClass>();

		int i = 0;
		for (Set<XEventClass> sigma : partition) {

			if (i < partition.size() - 1) {
				atPosition = findOptimalSplit(trace, sigma, atPosition, ignore);
			} else {
				//if this is the last sigma, there's no point in splitting
				atPosition = trace.size();
			}

			ignore.addAll(sigma);

			//split the trace
			newTrace = new IMTrace();
			for (XEventClass event : trace.subList(lastPosition, atPosition)) {
				if (sigma.contains(event)) {
					//non-noise event
					newTrace.add(event);
				} else {
					//noise
					noise.add(event, cardinality);
				}
			}

			IMLog sublog = mapSigma2sublog.get(sigma);
			sublog.add(newTrace, cardinality);

			lastPosition = atPosition;
			i++;
		}
	}

	private static int findOptimalSplit(List<XEventClass> trace, Set<XEventClass> sigma, int startPosition,
			Set<XEventClass> ignore) {
		int positionLeastCost = 0;
		int leastCost = 0;
		int cost = 0;
		int position = 0;

		Iterator<XEventClass> it = trace.iterator();

		//debug("find optimal split in " + trace.toString() + " for " + sigma.toString());

		//move to the start position
		while (position < startPosition && it.hasNext()) {
			position = position + 1;
			positionLeastCost = positionLeastCost + 1;
			it.next();
		}

		XEventClass event;
		while (it.hasNext()) {
			event = it.next();
			if (ignore.contains(event)) {
				//skip
			} else if (sigma.contains(event)) {
				cost = cost - 1;
			} else {
				cost = cost + 1;
			}

			position = position + 1;

			if (cost < leastCost) {
				leastCost = cost;
				positionLeastCost = position;
			}
		}

		return positionLeastCost;
	}

	public static void splitParallel(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			int cardinality, HashMap<Set<XEventClass>, IMLog> mapSigma2sublog,
			HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {

		//add a new trace to every sublog
		HashMap<Set<XEventClass>, IMTrace> mapSigma2subtrace = new HashMap<Set<XEventClass>, IMTrace>();
		for (Set<XEventClass> sigma : partition) {
			IMTrace subtrace = new IMTrace();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			mapSigma2subtrace.get(sigma).add(event);
		}

		for (Set<XEventClass> sigma : partition) {
			mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma), cardinality);
		}
	}

	public static void splitLoop(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			int cardinality, HashMap<Set<XEventClass>, IMLog> mapSigma2sublog,
			HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {
		IMTrace partialTrace = new IMTrace();

		Set<XEventClass> lastSigma = partition.iterator().next();
		for (XEventClass event : trace) {
			if (!lastSigma.contains(event)) {
				mapSigma2sublog.get(lastSigma).add(partialTrace, cardinality);
				partialTrace = new IMTrace();
				lastSigma = mapActivity2sigma.get(event);
			}
			partialTrace.add(event);
		}
		mapSigma2sublog.get(lastSigma).add(partialTrace, cardinality);

		//add an empty trace if the last event was not of sigma_1
		if (lastSigma != partition.iterator().next()) {
			mapSigma2sublog.get(lastSigma).add(new IMTrace(), cardinality);
		}
	}
}