package org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo;

import java.util.Iterator;
import java.util.Random;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

public class IMLog2IMLogInfoBucketFilter implements IMLog2IMLogInfo {

	private final static double parameterAgreeAt = 0.5;

	private final MiningParameters miningParameters;

	public IMLog2IMLogInfoBucketFilter(MiningParameters miningParameters) {
		this.miningParameters = miningParameters;
	}

	public IMLogInfo createLogInfo(IMLog log) {
		int numberOfBuckets = (int) Math.round(Math.pow(log.size(), miningParameters.getNoiseThreshold()) - 2) + 1;
		int agreementAt = (int) Math.floor(numberOfBuckets * parameterAgreeAt);

		Random random = new Random(123);
		IMLog[] subLogs = makeBuckets(numberOfBuckets, random, log);
		IMLogInfo[] subLogInfos = makeIMLogInfo(subLogs);

		IMLogInfo logInfoComplete = (new IMLog2IMLogInfoLifeCycle()).createLogInfo(log);
		IMLogInfo result = (new IMLog2IMLogInfoLifeCycle()).createLogInfo(log);

		//filter start activities
		{
			//add all activities
			MultiSet<XEventClass> counting = new MultiSet<>();
			for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
				counting.addAll(subLogInfos[bucket].getStartActivities().toSet());
			}

			//remove every activity with a too low cardinality
			Iterator<XEventClass> it = result.getStartActivities().iterator();
			while (it.hasNext()) {
				if (counting.getCardinalityOf(it.next()) < agreementAt) {
					it.remove();
				}
			}
		}

		//filter end activities
		{
			//add all activities
			MultiSet<XEventClass> counting = new MultiSet<>();
			for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
				counting.addAll(subLogInfos[bucket].getEndActivities().toSet());
			}

			//remove every activity with a too low cardinality
			Iterator<XEventClass> it = result.getEndActivities().iterator();
			while (it.hasNext()) {
				if (counting.getCardinalityOf(it.next()) < agreementAt) {
					it.remove();
				}
			}
		}

		//filter directly-follows graph
		{
			//add all edges
			Graph<XEventClass> counting = GraphFactory.create(XEventClass.class, 0);
			counting.addVertices(logInfoComplete.getDirectlyFollowsGraph().getVertices());
			for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
				Graph<XEventClass> g = subLogInfos[bucket].getDirectlyFollowsGraph();
				for (long edge : g.getEdges()) {
					counting.addEdge(g.getEdgeSource(edge), g.getEdgeTarget(edge), 1);
				}
			}

			//create a new directly-follows graph
			Graph<XEventClass> newDfg = GraphFactory.create(XEventClass.class, 0);
			newDfg.addVertices(logInfoComplete.getDirectlyFollowsGraph().getVertices());
			for (long edge : counting.getEdges()) {
				if (counting.getEdgeWeight(edge) >= agreementAt) {
					XEventClass source = counting.getEdgeSource(edge);
					XEventClass target = counting.getEdgeTarget(edge);
					long completeCardinality = logInfoComplete.getDirectlyFollowsGraph().getEdgeWeight(source, target);
					newDfg.addEdge(source, target, completeCardinality);
				}
			}

			result.getDfg().setDirectlyFollowsGraph(newDfg);
		}

		return result;
	}

	public static IMLogInfo[] makeIMLogInfo(IMLog[] logs) {
		IMLogInfo[] result = new IMLogInfo[logs.length];
		for (int bucket = 0; bucket < logs.length; bucket++) {
			result[bucket] = (new IMLog2IMLogInfoLifeCycle()).createLogInfo(logs[bucket]);
		}
		return result;
	}

	/**
	 * 
	 * @param numberOfBuckets
	 * @param random
	 * @param log
	 * @return a random division of the log by traces
	 */
	public static IMLog[] makeBuckets(int numberOfBuckets, Random random, IMLog log) {
		//set up sub-logs and iterators
		IMLog[] subLogs = new IMLog[numberOfBuckets];
		@SuppressWarnings("unchecked")
		Iterator<IMTrace>[] its = (Iterator<IMTrace>[]) new Iterator<?>[numberOfBuckets];
		for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
			subLogs[bucket] = new IMLog(log);
			its[bucket] = subLogs[bucket].iterator();
		}

		//assign each trace a random bucket
		for (int i = 0; i < log.size(); i++) {
			//advance all iterators
			for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
				its[bucket].next();
			}

			//choose a bucket where this trace will be in
			int chosenBucket = random.nextInt(numberOfBuckets);

			//remove the trace from all other buckets
			for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
				if (bucket != chosenBucket) {
					its[bucket].remove();
				}
			}
		}

		return subLogs;
	}

}
