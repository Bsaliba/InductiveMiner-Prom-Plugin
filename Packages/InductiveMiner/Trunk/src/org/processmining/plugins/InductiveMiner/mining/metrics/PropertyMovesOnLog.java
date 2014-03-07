package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;
import org.processmining.plugins.properties.processmodel.abstractproperty.PropertyInteger;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyMovesOnLog extends PropertyInteger {

	private static final long serialVersionUID = 3208126370849190661L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "moves on log";
	}

	public Integer getDefaultValue() {
		return 0;
	}

}
