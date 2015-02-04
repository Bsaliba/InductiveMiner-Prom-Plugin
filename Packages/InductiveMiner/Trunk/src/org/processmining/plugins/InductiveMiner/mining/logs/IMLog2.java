package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

public class IMLog2 extends org.processmining.plugins.InductiveMiner.mining.IMLog {

	public IMLog2(org.processmining.plugins.InductiveMiner.mining.logs.IMLog2 log) {
		super(log);
	}

	public IMLog2(XLog xLog, XEventClassifier classifier) {
		super(xLog, classifier);
	}
		
}
