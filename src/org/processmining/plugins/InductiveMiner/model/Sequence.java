package org.processmining.plugins.InductiveMiner.model;



public class Sequence extends Binoperator {

	public Sequence(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		return "->";
	}

}
