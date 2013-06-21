package org.processmining.plugins.InductiveMiner.model;


public class Loop extends Binoperator {

	public Loop(int countChildren) {
		super(countChildren);
	}

	public String getOperatorString() {
		return "loop";
	}

	/*
	public boolean canProduceEpsilon() {
		return children.get(0).canProduceEpsilon();
	}
	*/

}
