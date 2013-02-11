package bPrime.mining;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import bPrime.MultiSet;
import bPrime.Pair;
import bPrime.ProcessTreeModelParameters;
import bPrime.model.ProcessTreeModel.Operator;

public class Filteredlog {
	
	//protected XLog log;
	protected MultiSet<List<XEventClass>> internalLog;
	
	private Set<XEventClass> eventClasses;
	
	private Iterator<Pair<List<XEventClass>, Integer>> iteratorTrace;
	private Pair<List<XEventClass>, Integer> nextTrace;
	private Iterator<XEventClass> iteratorEvent;
	private XEventClass nextEvent;
	
	public Filteredlog(XLog log, ProcessTreeModelParameters parameters) {
		
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		eventClasses = new HashSet<XEventClass>(info.getEventClasses().getClasses());
		
		//transform the log to an internal format
		internalLog = new MultiSet<List<XEventClass>>();
		for (XTrace trace : log) {
			List<XEventClass> internalTrace = new LinkedList<XEventClass>();
			for (XEvent event : trace) {
				internalTrace.add(info.getEventClasses().getClassOf(event));
			}
			internalLog.add(internalTrace);
		}
	}
	
	public Filteredlog(MultiSet<List<XEventClass>> log, Set<XEventClass> eventClasses) {
		//this.log = filteredLog.log;
		this.internalLog = log;
		this.eventClasses = eventClasses;
	}
	
	public Filteredlog applyTauFilter() {
		MultiSet<List<XEventClass>> result = new MultiSet<List<XEventClass>>();
		for (Pair<List<XEventClass>, Integer> pair : internalLog) {
			List<XEventClass> trace = pair.getLeft();
			Integer cardinality = pair.getRight();
			if (trace.size() > 0) {
				result.add(trace, cardinality);
			}
		}
		return new Filteredlog(result, eventClasses);
	}
	
	public Filteredlog applyFilter(Operator operator, Set<XEventClass> arguments) {
		MultiSet<List<XEventClass>> result = new MultiSet<List<XEventClass>>();
		
		//if the set to filter is empty, return the singleton empty trace
		if (arguments.size() == 0) {
			Set<XEventClass> eventClasses = new HashSet<XEventClass>(arguments);
			result.add(new LinkedList<XEventClass>());
			return new Filteredlog(result, eventClasses);
		}
		
		//walk through the traces and add them to the result
		for (Pair<List<XEventClass>, Integer> pair : internalLog) {
			List<XEventClass> trace = pair.getLeft();
			Integer cardinality = pair.getRight();
			List<XEventClass> newTrace = new LinkedList<XEventClass>();
			Boolean keep = false;
			for (XEventClass eventClass : trace) {
				switch (operator) {
					case SEQUENCE:
						if (arguments.contains(eventClass)) {
							newTrace.add(eventClass);
						}
						keep = true;
						break;
					case EXCLUSIVE_CHOICE:
						if (arguments.contains(eventClass)) {
							newTrace.add(eventClass);
							keep = true;
						}
						break;
					case PARALLEL:
						if (arguments.contains(eventClass)) {
							newTrace.add(eventClass);
						}
						keep = true;
						break;
					case LOOP:
						if (arguments.contains(eventClass)) {
							newTrace.add(eventClass);
							keep = true;
						} else {
							if (keep) {
								result.add(newTrace, cardinality);
							}
							newTrace = new LinkedList<XEventClass>();
							keep = false;
						}
						break;
					case ACTIVITY :
						break;
					default :
						break;
				}
			}
			if (keep) {
				result.add(newTrace, cardinality);
			}
		}
		
		//make a copy of the arguments
		Set<XEventClass> eventClasses = new HashSet<XEventClass>(arguments);
		
		return new Filteredlog(result, eventClasses);
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
		iteratorEvent = nextTrace.getLeft().iterator();
	}
	
	public Integer getCurrentCardinality() {
		return nextTrace.getRight();
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
}
