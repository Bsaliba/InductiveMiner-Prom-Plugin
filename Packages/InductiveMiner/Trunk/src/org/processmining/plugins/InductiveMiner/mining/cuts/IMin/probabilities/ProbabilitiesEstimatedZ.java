package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;

public class ProbabilitiesEstimatedZ extends Probabilities {

	public double getProbabilityXor(LogInfo r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a) && !E(r, a, b) && !E(r, b, a)) {
			return 1 - (1 / (z(r, a, b) + 1));
		}
		return 0;
	}

	public double getProbabilitySequence(LogInfo r, XEventClass a, XEventClass b) {
		if (!D(r, a, b) && !D(r, b, a)) {
			if (!E(r, b, a)) {
				if (!E(r, a, b)) {
					return (1 / 6.0) * 1 / (z(r, a, b) + 1);
				} else {
					return 1 - 1 / (z(r, a, b) + 1);
				}
			}
		} else if (D(r, a, b) && !D(r, b, a) && !E(r, b, a)) {
			return 1 - 1 / (z(r, a, b) + 1);
		}
		return 0;
	}

	public double getProbabilityLoopIndirect(LogInfo r, XEventClass a, XEventClass b) {
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

	public double getProbabilityLoopSingle(LogInfo r, XEventClass a, XEventClass b) {
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
				return (1 / 2.0) * 1 / (z(r, a, b) + 1);
			} else {
				return 1 - 1 / (z(r, a, b) + 1);
			}
		}
		return 0;
	}

	public double getProbabilityParallel(LogInfo r, XEventClass a, XEventClass b) {
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
				return 1 / (z(r, a, b) + 1);
			}else {
				return (1 / 2.0) / (z(r, a, b) + 1);
			}
		} else {
			if (E(r, a, b)) {
				return 1 / (z(r, b, a) + 1);
			}else {
				return (1 / 2.0) / (z(r, b, a) + 1);
			}
		}
	}

	public double getProbabilityLoopDouble(LogInfo r, XEventClass a, XEventClass b) {
		return 0;
	}

	public String toString() {
		return "SAT estimated Z-only (without short loops)";
	}

}
