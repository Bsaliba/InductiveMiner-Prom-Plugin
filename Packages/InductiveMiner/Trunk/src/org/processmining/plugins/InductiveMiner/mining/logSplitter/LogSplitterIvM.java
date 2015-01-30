package org.processmining.plugins.InductiveMiner.mining.logSplitter;


public class LogSplitterIvM extends LogSplitterIMi implements LogSplitter {
/*
	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		List<IMLog> result = new ArrayList<IMLog>();
		MultiSet<XEventClass> noise = new MultiSet<XEventClass>();

		//map activities to sigmas
		Map<Set<XEventClass>, IMLog> mapSigma2sublog = new THashMap<Set<XEventClass>, IMLog>();
		Map<XEventClass, Set<XEventClass>> mapActivity2sigma = new THashMap<XEventClass, Set<XEventClass>>();
		for (Set<XEventClass> sigma : cut.getPartition()) {
			IMLog sublog = new IMLog();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}

		for (IMTrace trace : log) {
			if (cut.getOperator() == Operator.loop) {
				splitLoop(result, trace, cut.getPartition(), (int) log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			} else if (cut.getOperator() == Operator.xor) {
				splitXor(result, trace, cut.getPartition(), (int) log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			} else {
				splitParallel(result, trace, cut.getPartition(), (int) log.getCardinalityOf(trace), mapSigma2sublog,
						mapActivity2sigma, noise);
			}
		}

		return new LogSplitResult(result, noise);
	}

	public static void splitXor(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			int cardinality, Map<Set<XEventClass>, IMLog> mapSigma2sublog,
			Map<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {

		if (trace.size() == 0) {
			//an empty trace should have been filtered as a base case, but now we have to handle it
			//we cannot know in which branch the empty trace should go, so add it to all
			for (IMLog sublog : result) {
				sublog.add(trace, cardinality);
			}
			return;
		}

		//add a new trace to every sublog
		Map<Set<XEventClass>, IMTrace> mapSigma2subtrace = new THashMap<Set<XEventClass>, IMTrace>();
		for (Set<XEventClass> sigma : partition) {
			IMTrace subtrace = new IMTrace();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (XEventClass event : trace) {
			Set<XEventClass> sigma = mapActivity2sigma.get(event);
			mapSigma2subtrace.get(sigma).add(event);
		}

		for (Set<XEventClass> sigma : partition) {
			if (!mapSigma2subtrace.get(sigma).isEmpty()) {
				mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma), cardinality);
			}
		}
	}

	public static void splitParallel(List<IMLog> result, IMTrace trace, Collection<Set<XEventClass>> partition,
			long cardinality, Map<Set<XEventClass>, IMLog> mapSigma2sublog,
			Map<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {

		//add a new trace to every sublog
		Map<Set<XEventClass>, IMTrace> mapSigma2subtrace = new THashMap<Set<XEventClass>, IMTrace>();
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
			long cardinality, Map<Set<XEventClass>, IMLog> mapSigma2sublog,
			Map<XEventClass, Set<XEventClass>> mapActivity2sigma, MultiSet<XEventClass> noise) {
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
	}*/
}