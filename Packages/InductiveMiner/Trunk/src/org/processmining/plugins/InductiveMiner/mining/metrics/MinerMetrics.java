package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.properties.processmodel.Property;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class MinerMetrics {

	public static long getShortestTrace(Node node) {
		if (node instanceof Manual) {
			return 1;
		} else if (node instanceof Automatic) {
			return 0;
		} else if (node instanceof Block) {
			Block block = (Block) node;
			if (block instanceof Xor || block instanceof Def || block instanceof Or) {
				long result = Long.MAX_VALUE;
				for (Node child: block.getChildren()) {
					result = Math.min(result, getShortestTrace(child));
				}
				return result;
			} else if (block instanceof And || block instanceof Seq) {
				int result = 0;
				for (Node child: block.getChildren()) {
					result += getShortestTrace(child);
				}
				return result;
			} else if (block instanceof AbstractBlock.DefLoop || block instanceof AbstractBlock.XorLoop) {
				return getShortestTrace(block.getChildren().get(0)) + getShortestTrace(block.getChildren().get(2));
			}
		}
		assert(false);
		return 0;
	}

	@SuppressWarnings("rawtypes")
	public static Object getProperty(Node node, Property property) {
		Object result = null;
		try {
			result = node.getIndependentProperty(property);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (result == null &&!( property instanceof PropertyEpsilonTracesSkipped)) {
			System.out.println("==== node without property ====");
			System.out.println(property.getClass());
			System.out.println(node);
			assert(false);
		}
		return result;
		//return property.getDefaultValue();
	}

	@SuppressWarnings("rawtypes")
	public static void attachProperty(Node node, Property property, Object value) {
		if (value != null) {
			try {
				node.setIndependentProperty(property, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
}
