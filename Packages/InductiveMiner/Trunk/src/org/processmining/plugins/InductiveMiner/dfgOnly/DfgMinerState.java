package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class DfgMinerState {
	private final DfgMiningParameters parameters;
	private final MultiSet<XEventClass> discardedEvents;
	
	public DfgMinerState(DfgMiningParameters parameters) {
		this.parameters = parameters;
		discardedEvents = new MultiSet<>();
	}

	public DfgMiningParameters getParameters() {
		return parameters;
	}

	public MultiSet<XEventClass> getDiscardedEvents() {
		return discardedEvents;
	}
}
