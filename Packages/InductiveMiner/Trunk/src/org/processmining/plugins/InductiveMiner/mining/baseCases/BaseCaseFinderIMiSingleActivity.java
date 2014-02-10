package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiSingleActivity implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {

		if (logInfo.getActivities().setSize() == 1) {
			//the log contains just one activity

			//assuming application of the activity follows a geometric distribution, we estimate parameter ^p

			//calculate the event-per-trace size of the log
			double p = logInfo.getNumberOfTraces() / ((logInfo.getNumberOfEvents() + logInfo.getNumberOfTraces()) * 1.0);

			if (0.5 - parameters.getNoiseThreshold() <= p && p <= 0.5 + parameters.getNoiseThreshold()) {
				//^p is close enough to 0.5, consider it as a single activity

				Node node = new AbstractTask.Manual(logInfo.getActivities().iterator().next().toString());
				node.setProcessTree(tree);
				return node;
			}
			//else, the probability to stop is too low or too high, and we better output a flower model
		}
		
		return null;
	}
}
