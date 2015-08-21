package org.processmining.plugins.InductiveMiner.mining.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.plugins.InductiveMiner.MaybeString;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;
import org.processmining.plugins.properties.processmodel.abstractproperty.PropertyList;
import org.processmining.processtree.Node;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyDirectlyFollowsGraph extends PropertyList<Triple<MaybeString, MaybeString, Long>> {

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
	public static List<Triple<MaybeString, MaybeString, Long>> get(Node node) {
		PropertyDirectlyFollowsGraph property = new PropertyDirectlyFollowsGraph();
		try {
			return (List<Triple<MaybeString, MaybeString, Long>>) node.getIndependentProperty(property);
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

	public List<Triple<MaybeString, MaybeString, Long>> clone(Object element) {
		List<Triple<MaybeString, MaybeString, Long>> ret = new ArrayList<Triple<MaybeString, MaybeString, Long>>();
		if (element instanceof List<?>) {
			List<?> el2 = (List<?>) element;
			for (Object elem : el2) {
				if (elem instanceof Triple<?, ?, ?>) {
					Triple<?, ?, ?> p = (Triple<?, ?, ?>) elem;
					if (p.getA() instanceof MaybeString && p.getB() instanceof MaybeString && p.getC() instanceof Long) {
						ret.add(new Triple<MaybeString, MaybeString, Long>(MaybeString.load((String) p.getA()),
								MaybeString.load((String) p.getB()), new Long((Long) p.getC())));
					}
				}
			}
		}
		return ret;
	}

	public List<Triple<MaybeString, MaybeString, Long>> getDefaultValue() {
		return new ArrayList<Triple<MaybeString, MaybeString, Long>>();
	}

	@Override
	public String marshall(Object values) {
		@SuppressWarnings("unchecked")
		List<Triple<MaybeString, MaybeString, Long>> value = (List<Triple<MaybeString, MaybeString, Long>>) values;
		StringBuilder result = new StringBuilder();
		for (Triple<MaybeString, MaybeString, Long> t : value) {
			result.append("<valueEntry><A>" + t.getA() + "</A><B>" + t.getB() + "</B><C>" + t.getC()
					+ "</C></valueEntry>\n");
		}
		return result.toString();
	}

	private static Pattern pattern = Pattern
			.compile("<valueEntry><A>([^<]*)</A><B>([^<]*)</B><C>(\\d*)</C></valueEntry>");

	public Object unmarshall(String xml) {
		System.out.println("unmarshall " + xml);
		List<Triple<MaybeString, MaybeString, Long>> result = new ArrayList<Triple<MaybeString, MaybeString, Long>>();
		Matcher m = pattern.matcher(xml);
		while (m.find()) {
			result.add(new Triple<MaybeString, MaybeString, Long>(new MaybeString(m.group(1)),
					new MaybeString (m.group(2)), Long.valueOf(m.group(3))));
		}
		return result;
	}
}
