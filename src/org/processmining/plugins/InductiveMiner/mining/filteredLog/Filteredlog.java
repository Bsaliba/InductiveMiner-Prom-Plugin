package org.processmining.plugins.InductiveMiner.mining.filteredLog;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
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
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class Filteredlog {

	private MultiSet<List<XEventClass>> internalLog;

	private Set<XEventClass> eventClasses;
	private final int eventSize;

	private Iterator<List<XEventClass>> iteratorTrace;
	private List<XEventClass> nextTrace;
	private Iterator<XEventClass> iteratorEvent;
	private XEventClass nextEvent;

	public Filteredlog(XLog log, MiningParameters parameters) {

		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		eventClasses = new LinkedHashSet<XEventClass>();
		for (XEventClass e : info.getEventClasses().getClasses()) {
			eventClasses.add(e);
		}

		int newEventSize = 0;

		//transform the log to the internal format
		internalLog = new MultiSet<List<XEventClass>>();
		for (XTrace trace : log) {
			List<XEventClass> internalTrace = new LinkedList<XEventClass>();
			for (XEvent event : trace) {
				internalTrace.add(info.getEventClasses().getClassOf(event));
				newEventSize++;
			}
			internalLog.add(internalTrace);
		}

		eventSize = newEventSize;
	}

	public Filteredlog(MultiSet<List<XEventClass>> log, Set<XEventClass> eventClasses, int eventSize) {
		this.internalLog = log;
		this.eventClasses = eventClasses;
		this.eventSize = eventSize;
	}

	public FilterResults applyEpsilonFilter() {
		MultiSet<List<XEventClass>> sublog = new MultiSet<List<XEventClass>>();
		int filteredEmptyTraces = 0;
		for (List<XEventClass> trace : internalLog) {
			if (trace.size() > 0) {
				sublog.add(trace, internalLog.getCardinalityOf(trace));
			} else {
				filteredEmptyTraces += internalLog.getCardinalityOf(trace);
			}
		}

		Collection<Filteredlog> list = new LinkedList<Filteredlog>();
		list.add(new Filteredlog(sublog, eventClasses, eventSize));
		return new FilterResults(list, null, filteredEmptyTraces);
	}

	public FilterResults applyFilterActivity(XEventClass activity) {
		//walk through the traces and count noise
		boolean seenActivity;
		MultiSet<XEventClass> noiseEvents = new MultiSet<XEventClass>();
		int noiseEmptyTracesSplit = 0;
		for (List<XEventClass> trace : internalLog.toSet()) {
			seenActivity = false;
			for (XEventClass event : trace) {
				if (event == activity && !seenActivity) {
					//correct activity, first time in this trace
					//not noise
					seenActivity = true;
				} else {
					//this event is noise
					noiseEvents.add(event, internalLog.getCardinalityOf(trace));
				}
			}

			//end of trace, if we have not seen the activity this trace must be represented by an empty trace
			if (!seenActivity) {
				noiseEmptyTracesSplit += internalLog.getCardinalityOf(trace);
			}
		}

		return new FilterResults(null, noiseEvents, noiseEmptyTracesSplit);
	}

	public FilterResults applyFilterExclusiveChoice(Set<Set<XEventClass>> sigmas) {
		FilteredlogLogSplitterExclusiveChoice logSplitter = new FilteredlogLogSplitterExclusiveChoice(sigmas);
		return applyFilter(logSplitter);
	}

	public FilterResults applyFilterSequence(List<Set<XEventClass>> sigmas) {
		FilteredlogLogSplitterSequence logSplitter = new FilteredlogLogSplitterSequence(sigmas);
		return applyFilter(logSplitter);
	}

	public FilterResults applyFilterParallel(Set<Set<XEventClass>> sigmas) {
		FilteredlogLogSplitterParallel logSplitter = new FilteredlogLogSplitterParallel(sigmas);
		return applyFilter(logSplitter);
	}

	public FilterResults applyFilterLoop(List<Set<XEventClass>> sigmas) {
		FilteredlogLogSplitterLoop logSplitter = new FilteredlogLogSplitterLoop(sigmas);
		return applyFilter(logSplitter);
	}

	public FilterResults applyFilterTauLoop(List<Set<XEventClass>> sigmas, Set<XEventClass> startActivities,
			Set<XEventClass> endActivities) {
		FilteredlogLogSplitterTauLoop logSplitter = new FilteredlogLogSplitterTauLoop(sigmas, startActivities,
				endActivities);
		return applyFilter(logSplitter);
	}

	private FilterResults applyFilter(FilteredlogLogSplitter logSplitter) {
		//walk through the traces and add them to the result
		for (List<XEventClass> trace : internalLog.toSet()) {
			logSplitter.filterTrace(trace, internalLog.getCardinalityOf(trace));
		}

		return new FilterResults(logSplitter.getSublogs(), logSplitter.getNoiseEvents(), 0);
	}

	public String toString() {
		String result = "";

		initIterator();
		while (hasNextTrace()) {
			nextTrace();
			while (hasNextEvent()) {
				XEventClass e = nextEvent();
				result += e.toString() + " ";
			}
			result += "(" + getCurrentCardinality() + ")\n";
		}
		return result;
	}

	public int getNumberOfTraces() {
		return internalLog.size();
	}

	public int getNumberOfEvents() {
		return eventSize;
	}

	public void initIterator() {
		iteratorTrace = internalLog.iterator();
		nextTrace = null;

		iteratorEvent = null;
		nextEvent = null;
	}

	public boolean hasNextTrace() {
		return iteratorTrace.hasNext();
	}

	public void nextTrace() {
		if (!hasNextTrace()) {
			throw new NoSuchElementException();
		}

		nextTrace = iteratorTrace.next();
		iteratorEvent = nextTrace.iterator();
	}

	public Integer getCurrentCardinality() {
		return internalLog.getCardinalityOf(nextTrace);
	}

	public boolean hasNextEvent() {
		if (nextTrace == null) {
			throw new NoSuchElementException();
		}

		return iteratorEvent.hasNext();
	}

	public XEventClass nextEvent() {
		if (nextTrace == null) {
			throw new NoSuchElementException();
		}

		nextEvent = iteratorEvent.next();
		return nextEvent;
	}

	public Set<XEventClass> getEventClasses() {
		return this.eventClasses;
	}

	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
		int emptytraces = 0;
		for (List<XEventClass> trace : internalLog) {
			if (trace.size() == 0) {
				emptytraces += internalLog.getCardinalityOf(trace);
			} else {
				for (int i = 0; i < internalLog.getCardinalityOf(trace); i++) {
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

		debug(emptytraces + " empty traces not converted to XLog");

		return result;
	}

	private static void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}

	private void debug(String x) {
		System.out.println(x);
	}
}
