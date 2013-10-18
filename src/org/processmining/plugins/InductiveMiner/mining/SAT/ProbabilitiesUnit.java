package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesUnit extends Probabilities {

	public ProbabilitiesUnit(DirectlyFollowsRelation relation) {
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

	public double getProbabilityLoop(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a) && E(a, b) && E(b, a)) {
			return 1;
		} else if (D(a, b) && !D(b, a) && E(b, a)) {
			return 1;
		} else if (!D(a, b) && D(b, a) && E(a, b)) {
			return 1;
		} else if (D(a, b) && D(b, a) && w(a, b) > 0) {
			return 1;
		}
		return 0;
	}

	public double getProbabilityLoopSingle(XEventClass a, XEventClass b) {
		if (getProbabilityLoop(a, b) == 1) {
			
			if (noSEinvolvedInMsd(a, b)) {
				return 0;
			}
			
			if (D(a, b) && !D(b, a)) {
				return 1;
			} else if (!D(a, b) && D(b, a)) {
				return 1;
			}
		}
		return 0;
	}

	public double getProbabilityLoopDouble(XEventClass a, XEventClass b) {
		if (getProbabilityLoop(a, b) == 1 && D(a, b) && D(b, a)) {
			
			if (noSEinvolvedInMsd(a, b)) {
				return 0;
			}
			
			if (noSEinvolvedInMsd(b, a)) {
				return 0;
			}
			
			return 1;
		}
		return 0;
	}
	
	private boolean noSEinvolvedInMsd(XEventClass a, XEventClass b) {
		Set<XEventClass> SE = Sets.union(relation.getStartActivities().toSet(), relation.getEndActivities().toSet());
		if (w(a,b) > 0 && !SE.contains(a) && !SE.contains(b)) {
			Set<XEventClass> SEmMSD = Sets.intersection(SE, relation.getMinimumSelfDistanceBetween(a).toSet());
			if (SEmMSD.size() == 0) {
				return true;
			}
		}
		return false;
	}

}
