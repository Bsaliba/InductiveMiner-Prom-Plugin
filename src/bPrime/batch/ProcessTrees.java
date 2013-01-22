package bPrime.batch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.util.HTMLToString;
import org.processmining.processtree.ProcessTree;

import bPrime.model.Node;

public class ProcessTrees implements HTMLToString {
	private List<ProcessTree> trees = new LinkedList<ProcessTree>();
	private List<Node> roots = new LinkedList<Node>();
	private List<String> comments = new LinkedList<String>();
	
	public void add(ProcessTree tree, Node root, String comment) {
		trees.add(tree);
		roots.add(root);
		comments.add(comment);
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
