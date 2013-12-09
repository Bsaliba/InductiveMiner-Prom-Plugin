package org.processmining.plugins.InductiveMiner.model;

import java.util.List;


public class Parallel extends Binoperator{

	public Parallel(int countChildren) {
		super(countChildren);
	}

	public Parallel(List<Node> children) {
		super(children);
	}

	public String getOperatorString() {
		return "/\\";
		//return "&Lambda;";
	}
	
}
