package bPrime.mining;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import bPrime.MultiSet;

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
		
		//test whether this is a problem
		System.out.println("=================================(filteredlog.java)");
		System.out.println(info.getEventClasses());
		System.out.println(info.getEventClasses().toString());
		System.out.println(info.getEventClasses().getClasses());
		System.out.println(info.getEventClasses().getClasses().toString());
		System.out.println(info.getEventClasses(parameters.getClassifier()));
		System.out.println(info.getEventClasses(parameters.getClassifier()).toString());
		System.out.println(info.getEventClasses(parameters.getClassifier()).getClasses());
		System.out.println(info.getEventClasses(parameters.getClassifier()).getClasses().toString());
		//apparently, it is not
		
		int newEventSize = 0;
		
		//transform the log to an internal format
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
		
		//test whether this is a problem
		System.out.println("=================================(filteredlog.java)(2)");
		System.out.println(info.getEventClasses());
		System.out.println(info.getEventClasses().toString());
		System.out.println(info.getEventClasses().getClasses());
		System.out.println(info.getEventClasses().getClasses().toString());
		System.out.println(info.getEventClasses(parameters.getClassifier()));
		System.out.println(info.getEventClasses(parameters.getClassifier()).toString());
		System.out.println(info.getEventClasses(parameters.getClassifier()).getClasses());
		System.out.println(info.getEventClasses(parameters.getClassifier()).getClasses().toString());
		//apparently, it is not
	}
	
	public Filteredlog(MultiSet<List<XEventClass>> log, Set<XEventClass> eventClasses, int eventSize) {
		this.internalLog = log;
		this.eventClasses = eventClasses;
		this.eventSize = eventSize;
	}
	
	public Filteredlog applyEpsilonFilter() {
		MultiSet<List<XEventClass>> result = new MultiSet<List<XEventClass>>();
		for (List<XEventClass> trace : internalLog) {
			if (trace.size() > 0) {
				result.add(trace, internalLog.getCardinalityOf(trace));
			}
		}
		return new Filteredlog(result, eventClasses, eventSize);
	}
	
	public Filteredlog applyFilter(Set<XEventClass> arguments) {
		MultiSet<List<XEventClass>> result = new MultiSet<List<XEventClass>>();
		
		//if the set to filter is empty, return the singleton empty trace
		if (arguments.size() == 0) {
			Set<XEventClass> eventClasses = new LinkedHashSet<XEventClass>(arguments);
			result.add(new LinkedList<XEventClass>());
			return new Filteredlog(result, eventClasses, 0);
		}
		
		//walk through the traces and add them to the result
		int newEventSize = 0;
		for (List<XEventClass> trace : internalLog) {
			List<XEventClass> newTrace = new LinkedList<XEventClass>();
			Boolean keep = false;
			for (XEventClass eventClass : trace) {
				if (arguments.contains(eventClass)) {
					newTrace.add(eventClass);
				}
				keep = true;
			}
			if (keep) {
				result.add(newTrace, internalLog.getCardinalityOf(trace));
				newEventSize += newTrace.size() * internalLog.getCardinalityOf(trace);
			}
		}
		
		//make a copy of the arguments
		Set<XEventClass> eventClasses = new LinkedHashSet<XEventClass>(arguments);
		
		return new Filteredlog(result, eventClasses, newEventSize);
	}
	
	public void applyFilterActivity(XEventClass activity, MultiSet<XEventClass> noiseEvents, AtomicInteger noiseEmptyTraces) {
		//walk through the traces and count noise
		boolean seenActivity;
		MultiSet<XEventClass> noiseEventsSplit = new MultiSet<XEventClass>();
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
					noiseEventsSplit.add(event, internalLog.getCardinalityOf(trace));
				}
			}
			
			//end of trace, if we have not seen the activity this trace must be represented by an empty trace
			if (!seenActivity) {
				noiseEmptyTracesSplit += internalLog.getCardinalityOf(trace);
			}
		}
		
		debug(" filtered empty traces: "+ noiseEmptyTracesSplit + ", noise events: (" + ((float) noiseEventsSplit.size()/eventSize*100) + "%) " + noiseEventsSplit.toString());
		
		noiseEmptyTraces.addAndGet(noiseEmptyTracesSplit);
		synchronized (noiseEvents) {
			noiseEvents.addAll(noiseEventsSplit);
		}
	}
	
	public Set<Filteredlog> applyFilterExclusiveChoice(Set<Set<XEventClass>> sigmas, MultiSet<XEventClass> noiseEvents) {
		FilteredlogLogSplitterExclusiveChoice logSplitter = new FilteredlogLogSplitterExclusiveChoice(sigmas);
		return new HashSet<Filteredlog>(applyFilter(logSplitter, noiseEvents));
	}
	
	public List<Filteredlog> applyFilterSequence(List<Set<XEventClass>> sigmas, MultiSet<XEventClass> noiseEvents) {
		FilteredlogLogSplitterSequence logSplitter = new FilteredlogLogSplitterSequence(sigmas);
		return applyFilter(logSplitter, noiseEvents);
	}
	
	public Set<Filteredlog> applyFilterParallel(Set<Set<XEventClass>> sigmas, MultiSet<XEventClass> noiseEvents) {
		FilteredlogLogSplitterParallel logSplitter = new FilteredlogLogSplitterParallel(sigmas);
		return new HashSet<Filteredlog>(applyFilter(logSplitter, noiseEvents));
	}
	
	public List<Filteredlog> applyFilterLoop(List<Set<XEventClass>> sigmas, MultiSet<XEventClass> noiseEvents) {
		FilteredlogLogSplitterLoop logSplitter = new FilteredlogLogSplitterLoop(sigmas);
		return applyFilter(logSplitter, noiseEvents);
	}
	
	private List<Filteredlog> applyFilter(FilteredlogLogSplitter logSplitter, MultiSet<XEventClass> noiseEvents) {
		//walk through the traces and add them to the result
		for (List<XEventClass> trace : internalLog.toSet()) {
			logSplitter.filterTrace(trace, internalLog.getCardinalityOf(trace));
		}
		
		MultiSet<XEventClass> noiseEventsSplit = logSplitter.getNoiseEvents();
		synchronized (noiseEvents) {
			noiseEvents.addAll(noiseEventsSplit);
		}
		if (noiseEventsSplit.size() > 0) {
			debug(" filtered noise events: (" + ((float) noiseEventsSplit.size()/eventSize*100) + "%) " + noiseEventsSplit.toString());
		}
		
		return logSplitter.getSublogs();
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
	
	private void debug(String x) {
		System.out.println(x);
	}
}
