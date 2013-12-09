package org.processmining.plugins.InductiveMiner.model;

import java.util.List;


public class Loop extends Binoperator {

	public Loop(int countChildren) {
		super(countChildren);
	}

	public Loop(List<Node> children) {
		super(children);
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
