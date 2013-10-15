package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class SATResult {
	private final List<Set<XEventClass>> cut;
	private final double probability;
	private final String type;

	public SATResult(Set<XEventClass> cutA, Set<XEventClass> cutB, double probability, String type) {
		if (cutA == null || cutB == null) {
			cut = null;
		} else {
			cut = new LinkedList<Set<XEventClass>>();
			cut.add(cutA);
			cut.add(cutB);
		}
		this.probability = probability;
		this.type = type;
	}
	
	public SATResult(SATResult copyFrom) {
		this.probability = copyFrom.probability;
		this.type = copyFrom.type;
		this.cut = copyFrom.cut;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("probability " + probability);
		if (cut != null) {
			s.append(", " + type + " cut ");
			s.append(cut.toString());
		}
		return s.toString();
	}
	
	public List<Set<XEventClass>> getCut() {
		return cut;
	}
	
	public double getProbability() {
		return probability;
	}
	
	public String getType() {
		return type;
	}

}
