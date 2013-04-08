package bPrime.model;

import org.deckfour.xes.classification.XEventClass;

public class EventClass extends Node {
	public XEventClass eventClass;
	
	public EventClass(XEventClass eventClass) {
		this.eventClass = eventClass;
	}

	public String toString() {
		String s = eventClass.toString();
		
		//chop off the +complete part
		if (s.contains("+complete")) {
			s = s.substring(0, s.indexOf("+complete"));
		}
		
		return s;
	}

	/*
	public boolean canProduceEpsilon() {
		return false;
	}
	*/

}
