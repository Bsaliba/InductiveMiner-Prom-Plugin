package org.processmining.plugins.InductiveMiner.mining.SAT.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesUnitWithShortLoops extends Probabilities {

	public ProbabilitiesUnitWithShortLoops(DirectlyFollowsRelation relation) {
		super(relation);
	}

	public double getProbabilityXor(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a) && !E(a, b) && !E(b, a)) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getProbabilitySequence(XEventClass a, XEventClass b) {
		if (D(a, b) && !D(b, a) && !E(b, a)) {
			return 1;
		} else if (!D(a, b) && !D(b, a) && E(a, b) && !E(b, a)) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getProbabilityParallel(XEventClass a, XEventClass b) {
		if (D(a, b) && D(b, a) && w(a, b) == 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getProbabilityLoopSingle(XEventClass a, XEventClass b) {

		if (noSEinvolvedInMsd(a, b)) {
			return 0;
		}

		if (D(a, b) && !D(b, a) && E(b, a)) {
			return 1;
		}
		return 0;
	}

	public double getProbabilityLoopDouble(XEventClass a, XEventClass b) {
		if (noSEinvolvedInMsd(a, b)) {
			return 0;
		}

		if (noSEinvolvedInMsd(b, a)) {
			return 0;
		}

		if (D(a, b) && D(b, a) && w(a,b) > 0) {
			return 1;
		}
		
		return 0;
	}
	
	public double getProbabilityLoopIndirect(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a) && E(a, b) && E(b, a)) {
			return 1;
		}
		return 0;
	}
	
	public String toString() {
		return "SAT unit with short loops";
	}

}
