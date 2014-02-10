package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyNumberOfTracesRepresented extends org.processmining.plugins.properties.processmodel.abstractproperty.PropertyInteger {

	private static final long serialVersionUID = -3260227079823636872L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "number of traces represented";
	}

	public Integer getDefaultValue() {
		return 0;
	}

}
