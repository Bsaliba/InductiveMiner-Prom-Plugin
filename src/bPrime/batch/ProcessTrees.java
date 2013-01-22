package bPrime.batch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.util.HTMLToString;

import bPrime.model.Node;

public class ProcessTrees implements HTMLToString {
	private List<Node> roots = new LinkedList<Node>();
	private List<String> comments = new LinkedList<String>();
	
	public Integer add() {
		roots.add(null);
		comments.add(null);
		return roots.size()-1;
	}
	
	public void set(Integer index, Node root, String comment) {
		roots.set(index, root);
		comments.set(index, comment);
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuilder result = new StringBuilder();
		Iterator<Node> itR = roots.iterator();
		Iterator<String> itC = comments.iterator(); 
		while (itR.hasNext()) {
			result.append(itC.next());
			result.append("<br>");
			result.append(itR.next().toString());
			result.append("<br><br>");
		}
		return result.toString();
	}
}
