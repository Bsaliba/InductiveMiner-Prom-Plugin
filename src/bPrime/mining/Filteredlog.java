package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import bPrime.model.ProcessTreeModel.Operator;

public class Filteredlog {
	
	//protected XLog log;
	private MultiSet<List<XEventClass>> internalLog;
	
	private Set<XEventClass> eventClasses;
	private final int eventSize;
	
	private Iterator<List<XEventClass>> iteratorTrace;
	private List<XEventClass> nextTrace;
	private Iterator<XEventClass> iteratorEvent;
	private XEventClass nextEvent;
	
	public Filteredlog(XLog log, MiningParameters parameters) {
		
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		eventClasses = new LinkedHashSet<XEventClass>(info.getEventClasses().getClasses());
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
	
	public Filteredlog applyFilter(Operator operator, Set<XEventClass> arguments) {
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
								result.add(newTrace, internalLog.getCardinalityOf(trace));
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
				result.add(newTrace, internalLog.getCardinalityOf(trace));
				newEventSize += newTrace.size() * internalLog.getCardinalityOf(trace);
			}
		}
		
		//make a copy of the arguments
		Set<XEventClass> eventClasses = new LinkedHashSet<XEventClass>(arguments);
		
		return new Filteredlog(result, eventClasses, newEventSize);
	}
	
	public Set<Filteredlog> applyFilterExclusiveChoice2(Set<Set<XEventClass>> sigmas) {
		FilteredlogExclusiveChoiceNoiseFilter noiseFilter = new FilteredlogExclusiveChoiceNoiseFilter(sigmas);
		
		//walk through the traces and add them to the result
		for (List<XEventClass> trace : internalLog.toSet()) {
			noiseFilter.filterTrace(trace, internalLog.getCardinalityOf(trace));
		}
		
		MultiSet<XEventClass> noise = noiseFilter.getNoise();
		if (noise.size() > 0) {
			debug(" Filtered noise: (" + ((float) noise.size()/eventSize*100) + "%) " + noise.toString());
		}
		
		return noiseFilter.getSublogs();
	}
	
	 public Set<Filteredlog> applyFilterExclusiveChoice(Set<Set<XEventClass>> sigmas) {
		
		//initialise the sublogs, make a hashmap of activities, initialise event counters
		HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>> mapSigma2sublog = new HashMap<Set<XEventClass>, MultiSet<List<XEventClass>>>();
		HashMap<XEventClass, Set<XEventClass>> mapActivity2sigma = new HashMap<XEventClass, Set<XEventClass>>();
		HashMap<Set<XEventClass>, Integer> mapSigma2eventSize = new HashMap<Set<XEventClass>, Integer>();
		for (Set<XEventClass> sigma : sigmas) {
			mapSigma2sublog.put(sigma, new MultiSet<List<XEventClass>>());
			mapSigma2eventSize.put(sigma, 0);
			for (XEventClass activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}
		
		//debug noise
		MultiSet<XEventClass> noise = new MultiSet<XEventClass>();
		
		//walk through the traces and add them to the result
		for (List<XEventClass> trace : internalLog.toSet()) {
			
			//walk through the events and count how many go in each sigma
			HashMap<Set<XEventClass>, Integer> eventCounter = new HashMap<Set<XEventClass>, Integer>();
			for (Set<XEventClass> sigma : sigmas) {
				eventCounter.put(sigma, 0);
			}
			for (XEventClass event : trace) {
				Set<XEventClass> sigma = mapActivity2sigma.get(event);
				eventCounter.put(sigma, eventCounter.get(sigma) + 1);
			}
			
			//put the trace in the sublog of the sigma that accounts for more than half of the events
			for (Set<XEventClass> sigma : sigmas) {
				if (eventCounter.get(sigma) * 2 > trace.size()) {
					//make a copy of the trace, leaving out the noise
					List<XEventClass> newTrace = new ArrayList<XEventClass>();
					for (XEventClass event : trace) {
						if (sigma.contains(event)) {
							newTrace.add(event);
						} else {
							//debug
							noise.add(event, internalLog.getCardinalityOf(trace));							
						}
					}
					
					MultiSet<List<XEventClass>> sublog = mapSigma2sublog.get(sigma);
					sublog.add(newTrace, internalLog.getCardinalityOf(trace));
					mapSigma2sublog.put(sigma, sublog);
					mapSigma2eventSize.put(sigma, mapSigma2eventSize.get(sigma) + (trace.size() * internalLog.getCardinalityOf(trace)));
				}
			}
		}
		
		if (noise.size() > 0) {
			debug(" Filtered noise: (" + ((float) noise.size()/eventSize*100) + "%) " + noise.toString());
		}
		
		//make a copy of the arguments and the new filtered sublogs
		Set<Filteredlog> result2 = new LinkedHashSet<Filteredlog>();
		for (Set<XEventClass> sigma : sigmas) {
			result2.add(new Filteredlog(mapSigma2sublog.get(sigma), new LinkedHashSet<XEventClass>(sigma), mapSigma2eventSize.get(sigma)));
		}
		return result2;
	} 
	 
	public List<Filteredlog> applyFilterSequence(List<Set<XEventClass>> sigmas) {
		//temporary test for noise filtering
		FilteredlogSequenceNoiseFilter noiseFilter = new FilteredlogSequenceNoiseFilter(sigmas);
		
		//walk through the traces and add them to the result
		for (List<XEventClass> trace : internalLog.toSet()) {
			noiseFilter.filterTrace(trace, internalLog.getCardinalityOf(trace));
		}
		
		MultiSet<XEventClass> noise = noiseFilter.getNoise();
		if (noise.size() > 0) {
			debug(" Filtered noise: (" + ((float) noise.size()/eventSize*100) + "%) " + noise.toString());
		}
		
		return noiseFilter.getSublogs();
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
