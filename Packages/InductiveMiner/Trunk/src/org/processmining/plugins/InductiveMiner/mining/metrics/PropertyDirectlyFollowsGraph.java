package org.processmining.plugins.InductiveMiner.mining.metrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;
import org.processmining.plugins.properties.processmodel.abstractproperty.PropertyList;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyDirectlyFollowsGraph extends PropertyList<Triple<String, String, Long>> {

	private static final long serialVersionUID = 2004649367962293093L;

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
		return new LinkedList<Triple<String, String, Long>>();
	}

	@Override
	public String marshall(Object values) {
		List<Triple<String, String, Long>> value = (List<Triple<String, String, Long>>) values;
		StringBuilder result = new StringBuilder();
		for (Triple<String, String, Long> t : value) {
			result.append("<valueEntry><A>" + t.getA() + "</A><B>" + t.getB() + "</B><C>" + t.getC() + "</C></valueEntry>\n");
		}
		return result.toString();
	}

	public Object unmarshall(String xml) {
		return null;
//		ArrayList<String> ret = new ArrayList<String>();
//		// we have a string with valueEntries
//		String[] values = xml.split("<valueEntry value=\"");
//		for(String s: values){
//			if(!s.split("\"")[0].isEmpty()){
//				ret.add(s.split("\"")[0]);
//			}
//		}
//		return ret;
	}

}
