package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.processtree.Node;

public class MinerMetrics {

	public static Node attachStatistics(Node node, Integer numberOfTracesRepresented) {
		if (numberOfTracesRepresented == null) {
			return node;
		}
		
		if (numberOfTracesRepresented == 0) {
			new Exception("no traces represented").printStackTrace();
			System.out.println(node.toString());
		}
		PropertyNumberOfTracesRepresented property1 = new PropertyNumberOfTracesRepresented();
		try {
			node.setIndependentProperty(property1, new Integer(numberOfTracesRepresented));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return node;
	}

	public static Node attachStatistics(Node node, IMLogInfo logInfo) {
		return attachStatistics(node, (int) logInfo.getNumberOfTraces());
	}
	
	public static void copyStatistics(Node node, Node newNode) {
		if (getNumberOfTracesRepresented(node) != null) {
			attachStatistics(newNode, getNumberOfTracesRepresented(node));
		}
	}
	
	public static Integer getNumberOfTracesRepresented(Node node) {
		PropertyNumberOfTracesRepresented property1 = new PropertyNumberOfTracesRepresented();
		try {
			return (Integer) node.getIndependentProperty(property1);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
