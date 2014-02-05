package org.processmining.plugins.InductiveMiner.mining.filteredLog;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class IMLog extends MultiSet<IMTrace> {
	
	public IMLog() {
		super();
	}
	
	public IMLog(XLog log, XEventClassifier classifier) {
		super();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);

		//transform the log to the internal format
		for (XTrace trace : log) {
			IMTrace internalTrace = new IMTrace();
			for (XEvent event : trace) {
				internalTrace.add(info.getEventClasses().getClassOf(event));
			}
			add(internalTrace);
		}
	}
	
	public String toString() {
		if (cardinalities.isEmpty()) {
			return "empty log";
		} else {
			StringBuilder result = new StringBuilder();
			for (IMTrace trace : cardinalities.keySet()) {
				result.append(getCardinalityOf(trace) + "x ");
				result.append(trace.toString());
				result.append("\n");
			}
			return result.toString();
		}
	}
}
