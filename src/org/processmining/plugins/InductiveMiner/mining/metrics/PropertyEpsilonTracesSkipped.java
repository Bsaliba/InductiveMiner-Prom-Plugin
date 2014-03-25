package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyEpsilonTracesSkipped extends PropertyLong {

	private static final long serialVersionUID = 8889088857669397193L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "epsilon traces skipped";
	}

	public Long getDefaultValue() {
		return -1l;
	}

}
