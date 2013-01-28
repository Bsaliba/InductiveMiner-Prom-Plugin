package bPrime.batch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.util.HTMLToString;

public class ProcessTrees implements HTMLToString {
	private List<String> names;
	private List<String> comments;
	private long start;
	
	public ProcessTrees() {
		names = Collections.synchronizedList(new ArrayList<String>());
		comments = Collections.synchronizedList(new ArrayList<String>());

		start = System.currentTimeMillis();
	}
	
	public Integer add() {
		names.add(null);
		comments.add(null);
		return names.size()-1;
	}
	
	public void set(Integer index, String name, String comment) {
		names.set(index, name);
		comments.set(index, comment);
	}

	public String toHTMLString(boolean includeHTMLTags) {
		long time = System.currentTimeMillis() - start;
		StringBuilder result = new StringBuilder();
		
		//add time
		NumberFormat nf = NumberFormat.getInstance();
		result.append("Computation time " + nf.format(time) + "ms.<br><br>");
		
		Iterator<String> itN = names.iterator();
		Iterator<String> itC = comments.iterator(); 
		while (itN.hasNext()) {
			result.append("<br>\n");
			String name = itN.next();
			if (name != null) {
				result.append(name.toString());
			} else {
				result.append("Null name.");
			}
			result.append("<br>\n");
			result.append(itC.next());
			result.append("<br>\n<br>\n");
		}
		return result.toString();
	}
}
