package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyMovesOnModel extends PropertyLong {

	private static final long serialVersionUID = 8889088857669397193L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "moves on model";
	}

	public Long getDefaultValue() {
		return 0l;
	}

}
