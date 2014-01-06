package org.processmining.plugins.InductiveMiner.mining.SAT.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesNoise extends Probabilities {

	public double getProbabilityXor(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		if (!D(relation, a, b) && !D(relation, b, a) && !E(relation, a, b) && !E(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(relation, a, b) + 1);
	}

	public double getProbabilitySequence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		if (!D(relation, a, b) && !D(relation, b, a) && E(relation, a, b) && !E(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		if (D(relation, a, b) && !D(relation, b, a) && !E(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(relation, a, b) + 1);
	}

	public double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		if (D(relation, a, b) && D(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(relation, a, b) + 1);
	}

	public double getProbabilityLoopSingle(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		if (D(relation, a, b) && !D(relation, b, a) && E(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(relation, a, b) + 1);
	}

	public double getProbabilityLoopDouble(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return 0;
	}

	public double getProbabilityLoopIndirect(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		if (!D(relation, a, b) && !D(relation, b, a) && E(relation, a, b) && E(relation, b, a)) {
			return 1 - 1/(z(relation, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(relation, a, b) + 1);
	}

	public String toString() {
		return "SAT noise";
	}

}
