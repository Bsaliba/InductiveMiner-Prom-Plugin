package bPrime;

import org.deckfour.xes.model.XLog;
import org.processmining.processtree.ProcessTree;

public class ProcessTreeModelConnection extends AbstractProcessTreeModelConnection<ProcessTreeModelParameters> {
	public ProcessTreeModelConnection(XLog log, ProcessTree model, ProcessTreeModelParameters parameters) {
		super(log, model, parameters);
	}
}
