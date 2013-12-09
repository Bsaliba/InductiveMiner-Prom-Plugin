package org.processmining.plugins.InductiveMiner.model;

import java.util.List;



public class Sequence extends Binoperator {

	public Sequence(int countChildren) {
		super(countChildren);
	}

	public Sequence(List<Node> children) {
		super(children);
	}

	public String getOperatorString() {
		return "->";
	}

}
