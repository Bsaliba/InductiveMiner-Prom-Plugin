package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

@Deprecated
public class IMLog2 extends IMLog {

	public IMLog2(IMLog log) {
		super(log);
	}
	
	public IMLog2(XLog xLog, XEventClassifier activityClassifier) {
		super(xLog, activityClassifier);
	}
		
}
