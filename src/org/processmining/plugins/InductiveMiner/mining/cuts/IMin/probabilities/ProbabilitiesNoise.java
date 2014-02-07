package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;

public class ProbabilitiesNoise extends Probabilities {

	public double getProbabilityXor(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilitySequence(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		if (D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityParallel(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && D(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopSingle(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopDouble(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		return 0;
	}

	public double getProbabilityLoopIndirect(IMLogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public String toString() {
		return "SAT noise";
	}

}
