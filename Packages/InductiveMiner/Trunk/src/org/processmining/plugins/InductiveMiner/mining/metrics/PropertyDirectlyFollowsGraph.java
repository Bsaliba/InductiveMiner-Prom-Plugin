package org.processmining.plugins.InductiveMiner.mining.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;
import org.processmining.plugins.properties.processmodel.abstractproperty.PropertyList;
import org.processmining.processtree.Node;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyDirectlyFollowsGraph extends PropertyList<Triple<String, String, Long>> {

	private static final long serialVersionUID = 2004649367962293093L;

	public static boolean isSet(Node node) {
		//see if node has a directly-follows graph attached
		PropertyDirectlyFollowsGraph property = new PropertyDirectlyFollowsGraph();
		try {
			return node.getIndependentProperty(property) != null;
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static List<Triple<String, String, Long>> get(Node node) {
		PropertyDirectlyFollowsGraph property = new PropertyDirectlyFollowsGraph();
		try {
			return (List<Triple<String, String, Long>>) node.getIndependentProperty(property);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "directly-follows graph";
	}

	public List<Triple<String, String, Long>> clone(Object element) {
		List<Triple<String, String, Long>> ret = new ArrayList<Triple<String, String, Long>>();
		if (element instanceof List<?>) {
			List<?> el2 = (List<?>) element;
			for (Object elem : el2) {
				if (elem instanceof Triple<?, ?, ?>) {
					Triple<?, ?, ?> p = (Triple<?, ?, ?>) elem;
					if (p.getA() instanceof String && p.getB() instanceof String && p.getC() instanceof Long) {
						ret.add(new Triple<String, String, Long>(new String((String) p.getA()), new String((String) p
								.getB()), new Long((Long) p.getC())));
					}
				}
			}
		}
		return ret;
	}

	public List<Triple<String, String, Long>> getDefaultValue() {
		return new ArrayList<Triple<String, String, Long>>();
	}

	@Override
	public String marshall(Object values) {
		List<Triple<String, String, Long>> value = (List<Triple<String, String, Long>>) values;
		StringBuilder result = new StringBuilder();
		for (Triple<String, String, Long> t : value) {
			result.append("<valueEntry><A>" + t.getA() + "</A><B>" + t.getB() + "</B><C>" + t.getC()
					+ "</C></valueEntry>\n");
		}
		return result.toString();
	}

	private static Pattern pattern = Pattern
			.compile("<valueEntry><A>([^<]*)</A><B>([^<]*)</B><C>(\\d*)</C></valueEntry>");

	public Object unmarshall(String xml) {
		List<Triple<String, String, Long>> result = new ArrayList<Triple<String, String, Long>>();
		Matcher m = pattern.matcher(xml);
		while (m.find()) {
			result.add(new Triple<String, String, Long>(m.group(1), m.group(2), Long.valueOf(m.group(3))));
		}
		return result;
	}
}
