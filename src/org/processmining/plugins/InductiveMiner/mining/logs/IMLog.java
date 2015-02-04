package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

public class IMLog extends org.processmining.plugins.InductiveMiner.mining.IMLog {

	public IMLog(org.processmining.plugins.InductiveMiner.mining.logs.IMLog log) {
		super(log);
	}
	
	public IMLog(XLog log, XEventClassifier classifier) {
		super(log, classifier);
	}

	public void bla() {
		
	}
}

