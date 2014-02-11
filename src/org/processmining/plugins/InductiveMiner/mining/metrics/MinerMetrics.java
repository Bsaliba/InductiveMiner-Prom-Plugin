package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.processtree.Node;

public class MinerMetrics {

	public static Node attachStatistics(Node node, long numberOfTracesRepresented) {
		PropertyNumberOfTracesRepresented property1 = new PropertyNumberOfTracesRepresented();
		try {
			node.setIndependentProperty(property1, new Integer((int) numberOfTracesRepresented));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return node;
	}

	public static Node attachStatistics(Node node, IMLogInfo logInfo) {
		return attachStatistics(node, logInfo.getNumberOfTraces());
	}

}
