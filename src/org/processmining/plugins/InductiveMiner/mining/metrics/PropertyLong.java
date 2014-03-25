package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.processmodel.abstractproperty.HighLevelProperty;

public abstract class PropertyLong extends HighLevelProperty<Long> {
	
	private static final long serialVersionUID = -7723070273909264121L;

	public double getValueDouble(Object value) {
		return (Long) value;
	}

	public String marshall(Object value) {
		return "<valueEntry value=\"" + value + "\"/>\n";
	}

	public Object unmarshall(String xml) {
		return new Long(xml.split("<valueEntry value=\"")[1].split("\"")[0]);
	}
	
	public Long clone(Object element){
		return new Long((Long) element);
	}
	
}
