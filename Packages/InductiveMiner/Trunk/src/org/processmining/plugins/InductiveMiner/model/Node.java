package org.processmining.plugins.InductiveMiner.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Node {
	
	protected List<Node> children = new ArrayList<Node>();
	public abstract String toString();
	public Map<String, Object> metadata = new HashMap<String, Object>();
	
	public List<Node> getAllChildren() {
		List<Node> result = new LinkedList<Node>();
		result.add(this);
		for (Node child : children) {
			result.addAll(child.getAllChildren());
		}
		return result;
	}
	
	public List<Node> getChildren() {
		return children;
	}

}
