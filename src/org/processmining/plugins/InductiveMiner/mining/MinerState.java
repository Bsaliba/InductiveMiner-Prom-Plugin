package org.processmining.plugins.InductiveMiner.mining;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class MinerState {
	public final MultiSet<XEventClass> discardedEvents;
	public final MiningParameters parameters;

	public MinerState(MiningParameters parameters) {
		this.parameters = parameters;
		this.discardedEvents = new MultiSet<XEventClass>();
	}
}
