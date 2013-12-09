package org.processmining.plugins.InductiveMiner.mining.SAT.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesEstimated extends Probabilities {

	public double getProbabilityXor(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a) && !E(r, a, b) && !E(r, b, a)) {
			return 1 - (1 / (z(r, a, b) + 1));
		}
		return 0;
	}

	public double getProbabilitySequence(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a)) {
			if (!E(r, b, a)) {
				if (!E(r, a, b)) {
					return (1 / 6.0) * 1 / (z(r, a, b) + 1);
				} else {
					return 1 - 1 / (z(r, a, b) + 1);
				}
			}
		} else if (D(r, a, b) && !D(r, b, a) && !E(r, b, a)) {
			return 1 - 1 / (x(r, a, b) + 1);
		}
		return 0;
	}

	public double getProbabilityLoopIndirect(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a)) {
			if (!E(r, a, b) && !E(r, b, a)) {
				return (1 / 6.0) * 1 / (z(r, a, b) + 1);
			} else if (E(r, a, b) && E(r, b, a)) {
				return 1 - 1 / (z(r, a, b) + 1);
			} else {
				return (1 / 4.0) * 1 / (z(r, a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityLoopSingle(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a)) {
			if (!E(r, a, b) && !E(r, b, a)) {
				return (1 / 6.0) * 1 / (z(r, a, b) + 1);
			} else if (E(r, a, b) || E(r, b, a)) {
				return (1 / 4.0) * 1 / (z(r, a, b) + 1);
			} else {
				return (1 / 3.0) * 1 / (z(r, a, b) + 1);
			}
		} else if (D(r, a, b) && !D(r, b, a)) {
			if (!E(r, b, a)) {
				return (1 / 2.0) * 1 / (x(r, a, b) + 1);
			} else {
				return 1 - 1 / (x(r, a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityParallel(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a)) {
			if (!E(r, a, b) && !E(r, b, a)) {
				return (1 / 6.0) * 1 / (z(r, a, b) + 1);
			} else if (E(r, a, b) || E(r, b, a)) {
				return (1 / 4.0) * 1 / (z(r, a, b) + 1);
			} else {
				return (1 / 3.0) * 1 / (z(r, a, b) + 1);
			}
		} else if (D(r, a, b) && D(r, b, a)) {
			return 1;
		} else if (D(r, a,b) ) {
			if (E(r, b,a)) {
				return 1 / (x(r, a, b) + 1);
			}else {
				return (1 / 2.0) / (x(r, a, b) + 1);
			}
		} else {
			if (E(r, a, b)) {
				return 1 / (x(r, b, a) + 1);
			}else {
				return (1 / 2.0) / (x(r, b, a) + 1);
			}
		}
	}

	public double getProbabilityLoopDouble(DirectlyFollowsRelation r, XEventClass a, XEventClass b) {
		return 0;
	}

	public String toString() {
		return "SAT estimated (without short loops)";
	}

}
