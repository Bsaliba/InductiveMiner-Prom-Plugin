package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
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

	public static void attachProducer(Node node, String producer) {
		attachProperty(node, new PropertyProducer(), new String(producer));
	}

	public static void attachMovesOnModelWithoutEpsilonTracesFiltered(Node node, Long movesOnModel) {
		attachProperty(node, new PropertyMovesOnModel(), new Long(movesOnModel));
	}

	public static void attachMovesOnLog(Node node, Long movesOnLog) {
		attachProperty(node, new PropertyMovesOnLog(), new Long(movesOnLog));
	}

	public static void attachEpsilonTracesSkipped(Node node, Long epsilonTracesSkipped) {
		attachProperty(node, new PropertyEpsilonTracesSkipped(), new Long(epsilonTracesSkipped));
	}

	public static void attachNumberOfTracesRepresented(Node node, Long numberOfTracesRepresented) {
		attachProperty(node, new PropertyNumberOfTracesRepresented(), new Long(numberOfTracesRepresented));
	}

	public static void attachNumberOfTracesRepresented(Node node, IMLogInfo logInfo) {
		attachNumberOfTracesRepresented(node, logInfo.getNumberOfTraces());
	}

	public static Long getMovesOnLog(Node node) {
		return (Long) getProperty(node, new PropertyMovesOnLog());
	}

	public static Long getMovesOnModelWithoutEpsilonTracesFiltered(Node node) {
		return (Long) getProperty(node, new PropertyMovesOnModel());
	}

	public static Long getEpsilonTracesSkipped(Node node) {
		return (Long) getProperty(node, new PropertyEpsilonTracesSkipped());
	}

	public static Long getNumberOfTracesRepresented(Node node) {
		return (Long) getProperty(node, new PropertyNumberOfTracesRepresented());
	}

	public static String getProducer(Node node) {
		return (String) getProperty(node, new PropertyProducer());
	}
	
	public static void saveMovesSumInto(Node into, Node add) {
		attachEpsilonTracesSkipped(into, getEpsilonTracesSkipped(into) + getEpsilonTracesSkipped(add));
		attachMovesOnLog(into, getMovesOnLog(into) + getMovesOnLog(into));
		long epsilon = 0;
		if (getEpsilonTracesSkipped(into) != null) {
			epsilon += getEpsilonTracesSkipped(into);
		}
		if (getEpsilonTracesSkipped(add) != null) {
			epsilon += getEpsilonTracesSkipped(add);
		}
		if (epsilon != 0) {
			attachMovesOnModelWithoutEpsilonTracesFiltered(into, epsilon);
		}
		
		attachProducer(into, getProducer(into) + ", " + getProducer(add));
	}

//	public static void copyStatistics(Node node, Node newNode) {
//		attachNumberOfTracesRepresented(newNode, getNumberOfTracesRepresented(node));
//		attachEpsilonTracesSkipped(newNode, getEpsilonTracesSkipped(node));
//		attachMovesOnLog(newNode, getMovesOnLog(node));
//		attachMovesOnModelWithoutEpsilonTracesFiltered(newNode, getMovesOnModelWithoutEpsilonTracesFiltered(node));
//		attachProducer(newNode, getProducer(node));
//	}
	
	public static String statisticsToString(Node node) {
		StringBuilder result = new StringBuilder();
		result.append(" subtraces represented " + getNumberOfTracesRepresented(node) + "\n");
		result.append(" moves on log " + getMovesOnLog(node) + "\n");
		result.append(" moves on model " + getMovesOnModel(node) + "\n");
		result.append(" synchronous moves " + getSynchronousMoves(node) + "\n");
		result.append(" moves on log recursive " + getMovesOnLogRecursive(node) + "\n");
		result.append(" moves on model recursive " + getMovesOnModelRecursive(node) + "\n");
		result.append(" synchronous moves recursive " + getSynchronousMovesRecursive(node) + "\n");
		result.append(" fitness recursive " + getFitnessRecursive(node) + "\n");
		result.append(" produced by " + getProducer(node) + "\n");
		result.append(" epsilon traces filtered " + getEpsilonTracesSkipped(node) + "\n");
		result.append(" shortest trace " + getShortestTrace(node) + "\n");
		result.append(" moves on model without epsilon traces filtered " + getMovesOnModelWithoutEpsilonTracesFiltered(node) + "\n");
		result.append(" moves on model from epsilon traces filtered " + getMovesOnModelFromEmptyTraces(node) + "\n");
		return result.toString();
	}
	
	public static long getMovesOnModelFromEmptyTraces(Node node) {
		return getEpsilonTracesSkipped(node) * getShortestTrace(node);
	}
	
	public static long getMovesOnModel(Node node) {
		return getMovesOnModelWithoutEpsilonTracesFiltered(node) + getMovesOnModelFromEmptyTraces(node);
	}
	
	public static long getSynchronousMoves(Node node) {
		if (node instanceof Manual) {
			return getNumberOfTracesRepresented(node);
		}
		return 0;
	}
	
	public static long getMovesOnModelRecursive(Node node) {
		long result = getMovesOnModel(node);
		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				result += getMovesOnModelRecursive(child);
			}
		}
		return result;
	}
	
	public static long getMovesOnLogRecursive(Node node) {
		long result = getMovesOnLog(node);
		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				result += getMovesOnLogRecursive(child);
			}
		}
		return result;
	}
	
	public static long getSynchronousMovesRecursive(Node node) {
		long result = getSynchronousMoves(node);
		if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				result += getSynchronousMovesRecursive(child);
			}
		}
		return result;
	}
	
	public static double getFitnessRecursive(Node node) {
		long logMoves = getMovesOnLogRecursive(node);
		long modelMoves = getMovesOnModelRecursive(node);
		long synchronousMoves = getSynchronousMovesRecursive(node);
		return synchronousMoves / (logMoves + modelMoves + synchronousMoves * 1.0);
	}
	
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
