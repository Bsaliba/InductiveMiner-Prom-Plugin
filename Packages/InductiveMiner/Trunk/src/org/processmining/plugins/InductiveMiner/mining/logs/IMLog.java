package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.BitSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles.Transition;

public interface IMLog extends Iterable<IMTrace> {

	/*
	 * Memory-lightweight implementation of a filtering system.
	 */

	@Deprecated
	public final static XEventClassifier lifeCycleClassifier = new LifeCycleClassifier();

	
	/**
	 * Clone this IMLog. The new one might be based on the same XLog as the old one.
	 * @return
	 */
	public IMLog clone();
	
	/**
	 * Classify an event
	 * 
	 * @return
	 */
	public XEventClass classify(IMTrace IMTrace, XEvent event);

	public XEventClassifier getClassifier();

	public Transition getLifeCycle(XEvent event);

	public XTrace getTraceWithIndex(int traceIndex);

	/**
	 * Return the number of traces in the log
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Copy a trace and return the copy.
	 * 
	 * @param index
	 * @return
	 */
	public IMTrace copyTrace(int index, BitSet traceOutEvents);

	public String toString();

	public XLog toXLog();

	/**
	 * Turns the IMLog into an XLog, and makes a new IMLog out of it. Use this
	 * method to reduce memory usage if the log becomes sparse.
	 * 
	 * @return the newly created IMLog, which has no connection anymore to the
	 *         original XLog.
	 */
	public IMLog decoupleFromXLog();
}
