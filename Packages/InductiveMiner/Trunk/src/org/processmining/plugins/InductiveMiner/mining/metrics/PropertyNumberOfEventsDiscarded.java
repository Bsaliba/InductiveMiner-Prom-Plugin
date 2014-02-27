package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.annotations.ControlFlowPerspective;
import org.processmining.plugins.properties.annotations.PropertableElementProperty;

@ControlFlowPerspective
@PropertableElementProperty
public class PropertyNumberOfEventsDiscarded extends org.processmining.plugins.properties.processmodel.abstractproperty.PropertyInteger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3618758818233488504L;

	public Long getID() {
		return serialVersionUID;
	}

	public String getName() {
		return "number of events discarded";
	}

	public Integer getDefaultValue() {
		return 0;
	}
	
}
