package org.processmining.plugins.InductiveMiner.mining.filteredLog;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class FilterResults {
	public Collection<Filteredlog> sublogs;
	public MultiSet<XEventClass> filteredEvents;
	public int filteredEmptyTraces;
	
	public FilterResults(Collection<Filteredlog> sublogs, MultiSet<XEventClass> filteredEvents, int filteredEmptyTraces) {
		this.sublogs = sublogs;
		this.filteredEvents = filteredEvents;
		this.filteredEmptyTraces = filteredEmptyTraces;
	}
}
