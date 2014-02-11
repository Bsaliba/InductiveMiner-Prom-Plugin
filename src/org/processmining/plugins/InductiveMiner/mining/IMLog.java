package org.processmining.plugins.InductiveMiner.mining;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
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
	
	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
		//int emptytraces = 0;
		for (IMTrace trace : cardinalities.keySet()) {
			if (trace.size() == 0) {
				//emptytraces += getCardinalityOf(trace);
			} else {
				for (int i = 0; i < getCardinalityOf(trace); i++) {
					XTrace xTrace = new XTraceImpl(map);
					for (XEventClass e : trace) {
						XAttributeMap attMap = new XAttributeMapImpl();
						putLiteral(attMap, "concept:name", e.toString());
						putLiteral(attMap, "lifecycle:transition", "complete");
						putLiteral(attMap, "org:resource", "artificial");
						xTrace.add(new XEventImpl(attMap));
					}
					result.add(xTrace);
				}
			}
		}

		//debug(emptytraces + " empty traces not converted to XLog");

		return result;
	}

	private static void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}
}
