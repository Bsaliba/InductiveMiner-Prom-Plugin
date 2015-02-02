package org.processmining.plugins.InductiveMiner.mining;

import java.util.BitSet;
import java.util.Iterator;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class IMTrace implements Iterable<XEvent> {

	private final XTrace xTrace;
	private final BitSet outEvents;
	private final IMLog log;

	public IMTrace(XTrace xTrace, BitSet outEvents, IMLog log) {
		this.xTrace = xTrace;
		this.outEvents = outEvents;
		this.log = log;
	}

	/**
	 * 
	 * @return Whether the trace contains no events.
	 */
	public boolean isEmpty() {
		int next = outEvents.nextClearBit(0);
		return next == -1 || next >= xTrace.size();
	}

	/**
	 * @return The number of events in the trace.
	 */
	public int size() {
		return xTrace.size() - outEvents.cardinality();
	}

	public Iterator<XEvent> iterator() {
		return new Iterator<XEvent>() {

			int now = -1;
			int next = outEvents.nextClearBit(0) < xTrace.size() ? outEvents.nextClearBit(0) : -1;

			public boolean hasNext() {
				return next != -1 && next < xTrace.size();
			}

			public void remove() {
				outEvents.set(now);
			}

			public XEvent next() {
				now = next;
				next = outEvents.nextClearBit(now + 1);
				return xTrace.get(now);
			}
		};
	}

	/**
	 * Returns a sublist. This is O(n) in the number of events in the trace.
	 * 
	 * @param from
	 *            index at which the sub list starts. Inclusive.
	 * @param to
	 *            index at which the sub list ends. Exclusive.
	 * @return
	 */
	public Iterable<XEvent> subList(final int from, final int to) {
		return new Iterable<XEvent>() {

			public Iterator<XEvent> iterator() {
				Iterator<XEvent> it = new Iterator<XEvent>() {

					int now = -1;
					int next = outEvents.nextClearBit(0) < xTrace.size() ? outEvents.nextClearBit(0) : -1;
					int counter = 0;

					public boolean hasNext() {
						return next != -1 && next < xTrace.size() && counter < to;
					}

					public void remove() {
						outEvents.set(now);
					}

					public XEvent next() {
						now = next;
						next = outEvents.nextClearBit(now + 1);
						counter++;
						return xTrace.get(now);
					}
				};
				for (int i = 0; i < from; i++) {
					it.next();
				}
				return it;
			}
		};
	}

	@Deprecated
	public XEvent get(int index) {
		Iterator<XEvent> it = iterator();
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it.next();
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (XEvent e : this) {
			result.append(log.classify(e));
		}
		return result.toString();
	}

	public XAttributeMap getAttributes() {
		return xTrace.getAttributes();
	}
}
