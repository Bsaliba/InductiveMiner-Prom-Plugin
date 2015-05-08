package org.processmining.plugins.InductiveMiner.mining.logs;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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
	
	private final TIntArrayList addedTraces;
	private final List<BitSet> addedTracesOutEvents;
	
	private final XEventClassifier activityClassifier;
	private final XLogInfo xLogInfo;
	private final XLogInfo xLogInfoLifecycle;
	
	public final static XEventClassifier lifeCycleClassifier = new LifeCycleClassifier();

	/**
	 * Create an IMlog from an XLog.
	 * 
	 * @param xLog
	 */
	public IMLog(XLog xLog, XEventClassifier activityClassifier) {
		this.xLog = xLog;
		outTraces = new BitSet(xLog.size());
		outEvents = new BitSet[xLog.size()];
		for (int i = 0; i < xLog.size(); i++) {
			outEvents[i] = new BitSet();
		}
		
		addedTraces = new TIntArrayList();
		addedTracesOutEvents = new ArrayList<>();
		
		this.activityClassifier = activityClassifier;
		xLogInfo = XLogInfoFactory.createLogInfo(xLog, activityClassifier);
		xLogInfoLifecycle = XLogInfoFactory.createLogInfo(xLog, lifeCycleClassifier);
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
		
		addedTraces = new TIntArrayList(log.addedTraces);
		addedTracesOutEvents = new ArrayList<>(addedTraces.size());
		for (int i = 0; i < addedTraces.size() ; i++) {
			addedTracesOutEvents.add((BitSet) log.addedTracesOutEvents.get(i).clone());
		}
		
		activityClassifier = log.activityClassifier;
		xLogInfo = log.xLogInfo;
		xLogInfoLifecycle = log.xLogInfoLifecycle;
	}

	/**
	 * Classify an event
	 * 
	 * @return
	 */
	public XEventClass classify(XEvent event) {
		return xLogInfo.getEventClasses().getClassOf(event);
	}
	
	public XEventClassifier getClassifier() {
		return activityClassifier;
	}
	
	public boolean isStart(XEvent event) {
		return lifeCycleClassifier.getClassIdentity(event).equalsIgnoreCase("start");
	}
	
	public boolean isComplete(XEvent event) {
		return !isStart(event);
	}
	
	public XTrace getTraceWithIndex(int traceIndex) {
		return xLog.get(traceIndex);
	}

	/**
	 * Return the number of traces in the log
	 * 
	 * @return
	 */
	public int size() {
		return (xLog.size() - outTraces.cardinality()) + addedTraces.size();
	}
	
	/**
	 * Copy a trace and return the copy.
	 * @param index
	 * @return
	 */
	public IMTrace copyTrace(int index, BitSet traceOutEvents) {
		assert(index >= 0);
		
		addedTraces.add(index);
		BitSet newOutEvents = (BitSet) traceOutEvents.clone();
		addedTracesOutEvents.add(newOutEvents);
		return new IMTrace(index, newOutEvents, this);
	}

	public Iterator<IMTrace> iterator() {
		final IMLog t = this;
		return new Iterator<IMTrace>() {

			int next = init();
			int now = next - 1;
			
			private int init() {
				if (addedTraces.isEmpty()) {
					//start with normal traces
					return outTraces.nextClearBit(0) < xLog.size() ? outTraces.nextClearBit(0) : xLog.size();
				} else {
					//start with added traces
					return -addedTraces.size();
				}
			}

			public boolean hasNext() {
				return next < xLog.size();
			}

			public void remove() {
				if (now >= 0) {
					//we are in the normal traces
					outTraces.set(now);
				} else {
					//we are in the added traces
					int x = -now - 1;
					addedTraces.removeAt(x);
					addedTracesOutEvents.remove(x);
				}
			}

			public IMTrace next() {
				now = next;
				if (next < -1) {
					//we are in the added traces
					next = next + 1;
				} else {
					//we are in the normal traces
					next = outTraces.nextClearBit(next + 1);
				}
				
				if (now < 0) {
					//we are in the added traces
					return new IMTrace(addedTraces.get(-now - 1), addedTracesOutEvents.get(-now - 1), t);
				} else {
					//we are in the normal traces
					return new IMTrace(now, outEvents[now], t);
				}
			}
		};
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (IMTrace trace : this) {
			result.append(trace.toString());
			result.append("\n");
		}
		return result.toString();
	}

	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
//		int emptytraces = 0;
		for (IMTrace trace : this) {
			if (trace.isEmpty()) {
//				emptytraces += 1;
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
