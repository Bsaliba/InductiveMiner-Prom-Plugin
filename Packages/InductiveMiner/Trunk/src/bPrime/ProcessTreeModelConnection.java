package bPrime;

import org.deckfour.xes.model.XLog;
import org.processmining.processtree.ProcessTree;

import bPrime.mining.MiningParameters;

public class ProcessTreeModelConnection extends AbstractProcessTreeModelConnection<MiningParameters> {
	public ProcessTreeModelConnection(XLog log, ProcessTree model, MiningParameters parameters) {
		super(log, model, parameters);
	}
}
