package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.BitSet;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

public class IMLogStartEndComplete extends IMLogImpl {
	private final BitSet startComplete;
	private final BitSet endComplete;
	private final BitSet addedTracesStartComplete;
	private final BitSet addedTracesEndComplete;

	public IMLogStartEndComplete(XLog log, XEventClassifier activityClassifier) {
		super(log, activityClassifier);

		startComplete = new BitSet(log.size());
		endComplete = new BitSet(log.size());
		addedTracesStartComplete = new BitSet(0);
		addedTracesEndComplete = new BitSet(0);

		int traceNr = 0;
		for (XTrace trace : log) {
			Boolean startComplete = getBooleanAttrFromTrace(trace, "startcomplete");
			this.startComplete.set(traceNr, startComplete == null || startComplete);

			Boolean endComplete = getBooleanAttrFromTrace(trace, "endcomplete");
			this.endComplete.set(traceNr, endComplete == null || endComplete);

			traceNr++;
		}
	}

	public IMLogStartEndComplete(IMLogStartEndComplete log) {
		super(log);

		startComplete = (BitSet) log.startComplete.clone();
		endComplete = (BitSet) log.endComplete.clone();
		addedTracesStartComplete = (BitSet) log.addedTracesStartComplete.clone();
		addedTracesEndComplete = (BitSet) log.addedTracesEndComplete.clone();
	}
	
	public static IMLog fromIMLog(IMLog log) {
		if (log instanceof IMLogImpl) {
			return new IMLogStartEndComplete(((IMLogImpl) log).xLog, ((IMLogImpl) log).activityClassifier);
		}
		return log;
	}

	@Override
	public IMLogStartEndComplete clone() {
		return new IMLogStartEndComplete(this);
	}

	@Override
	public IMTrace copyTrace(IMTrace trace, BitSet traceOutEvents) {
		IMTrace result = super.copyTrace(trace, traceOutEvents);

		boolean isStartComplete = isStartComplete(trace.getIMTraceIndex());
		boolean isEndComplete = isEndComplete(trace.getIMTraceIndex());

		int addedTraceIndex = -addedTraces.size();
		setStartComplete(addedTraceIndex, isStartComplete);
		setEndComplete(addedTraceIndex, isEndComplete);

		return result;
	}

	@Override
	public Iterator<IMTrace> iterator() {
		final Iterator<IMTrace> old = super.iterator();
		return new Iterator<IMTrace>() {
			IMTrace next;

			public IMTrace next() {
				next = old.next();
				return next;
			}

			public boolean hasNext() {
				return old.hasNext();
			}

			public void remove() {
				old.remove();
				if (next.getIMTraceIndex() < 0) {
					removeFromBitSet(addedTracesStartComplete, -(next.getIMTraceIndex() + 1));
					removeFromBitSet(addedTracesEndComplete, -(next.getIMTraceIndex() + 1));
				}
			}
		};
	}

	@Override
	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
		for (IMTrace trace : this) {
			if (trace.isEmpty()) {
				XAttributeMap map2 = new XAttributeMapImpl();
				map2.put("startcomplete",
						new XAttributeBooleanImpl("startcomplete", isStartComplete(trace.getIMTraceIndex())));
				map2.put("endcomplete",
						new XAttributeBooleanImpl("endcomplete", isEndComplete(trace.getIMTraceIndex())));
				result.add(new XTraceImpl(map2));
			} else {
				XAttributeMap map2 = new XAttributeMapImpl();
				map2.put("startcomplete",
						new XAttributeBooleanImpl("startcomplete", isStartComplete(trace.getIMTraceIndex())));
				map2.put("endcomplete",
						new XAttributeBooleanImpl("endcomplete", isEndComplete(trace.getIMTraceIndex())));
				XTrace xTrace = new XTraceImpl(map2);
				for (XEvent e : trace) {
					xTrace.add(e);
				}
				result.add(xTrace);
			}
		}

		return result;
	}
	
	@Override
	public IMLogImpl decoupleFromXLog() {
		XLog xLog = toXLog();
		return new IMLogStartEndComplete(xLog, activityClassifier);
	}

	public boolean isStartComplete(int traceIndex) {
		if (traceIndex >= 0) {
			return startComplete.get(traceIndex);
		} else {
			return addedTracesStartComplete.get(-(traceIndex + 1));
		}
	}

	public void setStartComplete(int traceIndex, boolean isStartComplete) {
		if (traceIndex >= 0) {
			startComplete.set(traceIndex, isStartComplete);
		} else {
			addedTracesStartComplete.set(-(traceIndex + 1), isStartComplete);
		}
	}

	public boolean isEndComplete(int traceIndex) {
		if (traceIndex >= 0) {
			return endComplete.get(traceIndex);
		} else {
			return addedTracesEndComplete.get(-(traceIndex + 1));
		}
	}

	public void setEndComplete(int traceIndex, boolean isEndComplete) {
		if (traceIndex >= 0) {
			endComplete.set(traceIndex, isEndComplete);
		} else {
			addedTracesEndComplete.set(-(traceIndex + 1), isEndComplete);
		}
	}

	public static Boolean getBooleanAttrFromTrace(XTrace trace, String attrKey) {
		if (trace.getAttributes().containsKey(attrKey)) {
			return Boolean.valueOf(trace.getAttributes().get(attrKey).toString().trim());
		} else {
			return null;
		}
	}

	public static void removeFromBitSet(BitSet set, int index) {
		for (int i = index; i < set.length(); i++) {
			set.set(i, set.get(i + 1));
		}
	}
}
