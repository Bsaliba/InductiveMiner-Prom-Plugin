package bPrime.batch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.util.HTMLToString;

import bPrime.model.Node;

public class ProcessTrees implements HTMLToString {
	private List<Node> roots;
	private List<String> comments;
	private long start;
	
	public ProcessTrees() {
		roots = Collections.synchronizedList(new ArrayList<Node>());
		comments = Collections.synchronizedList(new ArrayList<String>());

		start = System.currentTimeMillis();
	}
	
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
		long time = System.currentTimeMillis() - start;
		StringBuilder result = new StringBuilder();
		
		//add time
		NumberFormat nf = NumberFormat.getInstance();
		result.append("Computation time " + nf.format(time) + "ms.<br><br>");
		
		Iterator<Node> itR = roots.iterator();
		Iterator<String> itC = comments.iterator(); 
		while (itR.hasNext()) {
			result.append(itC.next());
			result.append("<br>\n");
			Node root = itR.next();
			if (root != null) {
				result.append(root.toString());
			} else {
				result.append("Null root.");
			}
			result.append("<br>\n<br>\n");
		}
		return result.toString();
	}
}
