package org.processmining.plugins.InductiveMiner.mining.SAT.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesEstimatedZ extends Probabilities {

	public ProbabilitiesEstimatedZ(DirectlyFollowsRelation relation) {
		super(relation);
	}

	public double getProbabilityXor(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a) && !E(a, b) && !E(b, a)) {
			return 1 - (1 / (z(a, b) + 1));
		}
		return 0;
	}

	public double getProbabilitySequence(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(b, a)) {
				if (!E(a, b)) {
					return (1 / 6.0) * 1 / (z(a, b) + 1);
				} else {
					return 1 - 1 / (z(a, b) + 1);
				}
			}
		} else if (D(a, b) && !D(b, a) && !E(b, a)) {
			return 1 - 1 / (z(a, b) + 1);
		}
		return 0;
	}

	public double getProbabilityLoopIndirect(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(a, b) && !E(b, a)) {
				return (1 / 6.0) * 1 / (z(a, b) + 1);
			} else if (E(a, b) && E(b, a)) {
				return 1 - 1 / (z(a, b) + 1);
			} else {
				return (1 / 4.0) * 1 / (z(a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityLoopSingle(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(a, b) && !E(b, a)) {
				return (1 / 6.0) * 1 / (z(a, b) + 1);
			} else if (E(a, b) || E(b, a)) {
				return (1 / 4.0) * 1 / (z(a, b) + 1);
			} else {
				return (1 / 3.0) * 1 / (z(a, b) + 1);
			}
		} else if (D(a, b) && !D(b, a)) {
			if (!E(b, a)) {
				return (1 / 2.0) * 1 / (z(a, b) + 1);
			} else {
				return 1 - 1 / (z(a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityParallel(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(a, b) && !E(b, a)) {
				return (1 / 6.0) * 1 / (z(a, b) + 1);
			} else if (E(a, b) || E(b, a)) {
				return (1 / 4.0) * 1 / (z(a, b) + 1);
			} else {
				return (1 / 3.0) * 1 / (z(a, b) + 1);
			}
		} else if (D(a, b) && D(b, a)) {
			return 1;
		} else if (D(a,b) ) {
			if (E(b,a)) {
				return 1 / (z(a, b) + 1);
			}else {
				return (1 / 2.0) / (z(a, b) + 1);
			}
		} else {
			if (E(a, b)) {
				return 1 / (z(b, a) + 1);
			}else {
				return (1 / 2.0) / (z(b, a) + 1);
			}
		}
	}

	public double getProbabilityLoopDouble(XEventClass a, XEventClass b) {
		return 0;
	}

	public String toString() {
		return "SAT estimated Z-only (without short loops)";
	}

}
