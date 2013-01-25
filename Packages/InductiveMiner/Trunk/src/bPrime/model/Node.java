package bPrime.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class Node {
	
	protected List<Node> children = new ArrayList<Node>();
	public abstract String toString();
	
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
