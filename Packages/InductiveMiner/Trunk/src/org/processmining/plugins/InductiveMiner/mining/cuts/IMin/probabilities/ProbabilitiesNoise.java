package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;

public class ProbabilitiesNoise extends Probabilities {

	public double getProbabilityXor(LogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilitySequence(LogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		if (D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityParallel(LogInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && D(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopSingle(LogInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopDouble(LogInfo logInfo, XEventClass a, XEventClass b) {
		return 0;
	}

	public double getProbabilityLoopIndirect(LogInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public String toString() {
		return "SAT noise";
	}

}
