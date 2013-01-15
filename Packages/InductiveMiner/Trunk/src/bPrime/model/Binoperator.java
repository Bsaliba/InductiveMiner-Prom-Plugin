package bPrime.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import bPrime.Sets;


public abstract class Binoperator extends Node {
	
	public Binoperator(int countChildren) {
		super();
		//children = new ArrayList<Node>();
		for (int i = 0; i < countChildren; i++) {
			children.add(null);
		}
	}
	
	public String toString() {
		List<String> childStrings = new LinkedList<String>();
		for (Node child : children) {
			if (child != null) {
				childStrings.add(child.toString());
			} else {
				childStrings.add("..");
			}
		}
		
		return getOperatorString() + "(" + Sets.implode(childStrings, ", ") + ")";
	}
	
	public void setNumberOfChildren(int count) {
		children = new ArrayList<Node>();
		for (int i = 0; i < count; i++) {
			children.add(null);
		}
	}
	
	public void setChild(int index, Node child) {
		children.set(index, child);
	}
	
	public Node getChild(int index) {
		return children.get(0);
	}
	
	public abstract String getOperatorString();
}
