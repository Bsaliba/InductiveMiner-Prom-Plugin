package bPrime;

import org.deckfour.xes.model.XLog;

import bPrime.model.ProcessTreeModel;

public class ProcessTreeModelConnection extends AbstractProcessTreeModelConnection<ProcessTreeModelParameters> {
	public ProcessTreeModelConnection(XLog log, ProcessTreeModel model, ProcessTreeModelParameters parameters) {
		super(log, model, parameters);
	}
}
