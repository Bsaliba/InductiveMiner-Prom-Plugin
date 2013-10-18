package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesEventuallyFollows extends Probabilities {

	public ProbabilitiesEventuallyFollows(DirectlyFollowsRelation relation) {
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
			} else {
				return 0;
			}
		} else if (D(a, b) && !D(b, a) && !E(b, a)) {
			return 1 - 1 / (x(a, b) + 1);
		}
		return 0;
	}

	public double getProbabilityParallel(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(a, b) && !E(b, a)) {
				return (1 / 6.0) * 1 / (z(a, b) + 1);
			} else if (E(a, b) && E(b, a)) {
				return 1 / 4.0;
			} else {
				return (1 / 4.0) * 1 / (z(a, b) + 1);
			}
		} else if (D(a, b) && D(b, a)) {
			if (w(a, b) == 0) {
				return 1 - 1 / (z(a, b) + 1);
			} else {
				return 1 / (w(a, b) + 1);
			}
		}
		//D(a,b) xor D(b,a)
		if (E(a, b) && E(b, a)) {
			return (1 / 2.0) * 1 / (x(a, b) + 1);
		} else {
			return (1 / 3.0) * 1 / (x(a, b) + 1);
		}
	}

	public double getProbabilityLoopSingle(XEventClass a, XEventClass b) {
		if (!D(a, b) && !D(b, a)) {
			if (!E(a, b) && !E(b, a)) {
				return (1 / 6.0 * 1 / (z(a, b) + 1));
			} else if (E(a, b) && E(b, a)) {
				return 1 / 4.0;
			} else if (E(a, b) || E(b, a)) {
				return (1 / 4.0) * 1 / (z(a, b) + 1);
			}
		} else if (D(a, b) && !D(b, a)) {
			if (!E(b, a)) {
				return (1 / 3.0) * 1 / (x(a, b) + 1);
			} else {
				return 1 - 1 / (x(a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityLoopDouble(XEventClass a, XEventClass b) {
		if (D(a, b) && D(b, a)) {
			if (w(a, b) == 0) {
				return 1 / (z(a, b) + 1);
			} else {
				return 1 - 1 / (w(a, b) + 1);
			}
		}
		return getProbabilityParallel(a, b);
	}

	public double getProbabilityLoop(XEventClass a, XEventClass b) {
		// TODO Auto-generated method stub
		return 0;
	}
}
