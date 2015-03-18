package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

public class IMLog extends IMLog2 {

	public IMLog(IMLog2 log) {
		super(log);
	}
	
	public IMLog(XLog xLog, XEventClassifier activityClassifier) {
		super(xLog, activityClassifier);
	}

}
