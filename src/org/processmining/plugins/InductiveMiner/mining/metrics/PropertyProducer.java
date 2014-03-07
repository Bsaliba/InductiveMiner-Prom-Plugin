package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyProducer extends org.processmining.plugins.properties.processmodel.abstractproperty.PropertyString {

	private static final long serialVersionUID = 3315044337356838863L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "Module of IM that produced this node";
	}

	public String getDefaultValue() {
		return "";
	}

	
}
