package org.processmining.plugins.InductiveMiner.mining;

import java.util.BitSet;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

public class IMLog implements Iterable<IMTrace> {

	/*
	 * Memory-lightweight implementation of a filtering system.
	 */

	private final XLog xLog;
	private final BitSet outTraces;
	private final BitSet[] outEvents;
	private XLogInfo xLogInfo;

	/**
	 * Create an IMlog from an XLog.
	 * 
	 * @param xLog
	 */
	public IMLog(XLog xLog, XEventClassifier classifier) {
		this.xLog = xLog;
		outTraces = new BitSet(xLog.size());
		outEvents = new BitSet[xLog.size()];
		for (int i = 0; i < xLog.size(); i++) {
			outEvents[i] = new BitSet();
		}
		xLogInfo = XLogInfoFactory.createLogInfo(xLog, classifier);
	}

	/**
	 * Clone an existing IMlog
	 * 
	 * @param log
	 */
	public IMLog(IMLog log) {
		this.xLog = log.xLog;
		outTraces = (BitSet) log.outTraces.clone();
		outEvents = new BitSet[xLog.size()];
		for (int i = 0; i < xLog.size(); i++) {
			outEvents[i] = (BitSet) log.outEvents[i].clone();
		}
		xLogInfo = log.xLogInfo;
	}

	/**
	 * Classify an event
	 * 
	 * @return
	 */
	public XEventClass classify(XEvent e) {
		return xLogInfo.getEventClasses().getClassOf(e);
	}

	/**
	 * Return the number of traces in the log
	 * 
	 * @return
	 */
	public int size() {
		return xLog.size() - outTraces.cardinality();
	}

	public Iterator<IMTrace> iterator() {
		final IMLog t = this;
		return new Iterator<IMTrace>() {

			int now = -1;
			int next = outTraces.nextClearBit(0) < xLog.size() ? outTraces.nextClearBit(0) : -1;

			public boolean hasNext() {
				return next != -1 && next < xLog.size();
			}

			public void remove() {
				outTraces.set(now);
			}

			public IMTrace next() {
				now = next;
				next = outTraces.nextClearBit(now + 1);
				return new IMTrace(xLog.get(now), outEvents[now], t);
			}
		};
	}

	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
		int emptytraces = 0;
		for (IMTrace trace : this) {
			if (trace.isEmpty()) {
				emptytraces += 1;
			} else {
				XTrace xTrace = new XTraceImpl(map);
				for (XEvent e : trace) {
					xTrace.add(e);
				}
				result.add(xTrace);
			}
		}

		//		debug(emptytraces + " empty traces not converted to XLog");

		return result;
	}
}
