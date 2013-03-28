package bPrime.mining;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import bPrime.MultiSet;

public class DirectlyFollowsRelation {
	private DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph;
	private DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph;
	private MultiSet<XEventClass> startActivities;
	private MultiSet<XEventClass> endActivities;
	private HashMap<XEventClass, Set<XEventClass>> minimumSelfDistancesBetween;
	private HashMap<XEventClass, Integer> minimumSelfDistances;
	private int numberOfEpsilonTraces;
	private int longestTrace;
	private int lengthStrongestTrace;
	private int strongestDirectEdge;
	private int strongestEventualEdge;
	private int strongestStartActivity;
	private int strongestEndActivity;
	
	public DirectlyFollowsRelation(Filteredlog log, MiningParameters parameters) {
		
		//initialise, read the log
		directlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		eventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		startActivities = new MultiSet<XEventClass>();
		endActivities = new MultiSet<XEventClass>();
		minimumSelfDistances = new HashMap<XEventClass, Integer>();
		minimumSelfDistancesBetween = new HashMap<XEventClass, Set<XEventClass>>();
		numberOfEpsilonTraces = 0;
		longestTrace = 0;
		lengthStrongestTrace = 0;
		
		XEventClass fromEventClass;
		XEventClass toEventClass;
		
		//add the nodes to the graph
		for (XEventClass a : log.getEventClasses()) {
			directlyFollowsGraph.addVertex(a);
			eventuallyFollowsGraph.addVertex(a);
		}
		
		//walk trough the log
		log.initIterator();
		HashMap<XEventClass, Integer> eventSeenAt;
		List<XEventClass> readTrace;
		
		while (log.hasNextTrace()) {
			log.nextTrace();
			Integer cardinality = log.getCurrentCardinality();
		
			toEventClass = null;
			fromEventClass = null;
			
			int traceSize = 0;
			eventSeenAt = new HashMap<XEventClass, Integer>();
			readTrace = new LinkedList<XEventClass>();
			
			while(log.hasNextEvent()) {

				fromEventClass = toEventClass;
				toEventClass = log.nextEvent();
				
				//add connections to the eventually-follows graph
				DefaultWeightedEdge edge;
				Integer newEventuallyCardinality;
				for (XEventClass eventuallySeen : readTrace) {
					edge = eventuallyFollowsGraph.addEdge(eventuallySeen, toEventClass);
					newEventuallyCardinality = cardinality;
					if (edge == null) {
						edge = eventuallyFollowsGraph.getEdge(eventuallySeen, toEventClass);
						newEventuallyCardinality += (int) eventuallyFollowsGraph.getEdgeWeight(edge);
					}
					eventuallyFollowsGraph.setEdgeWeight(edge, newEventuallyCardinality);
				}
				
				readTrace.add(toEventClass);
				
				if (eventSeenAt.containsKey(toEventClass)) {
					//we have detected an activity for the second time
					//check whether this is shorter than what we had already seen
					if (!minimumSelfDistances.containsKey(toEventClass) || traceSize - eventSeenAt.get(toEventClass) < minimumSelfDistances.get(toEventClass)) {
						//keep the new minimum self distance
						minimumSelfDistances.put(toEventClass, traceSize - eventSeenAt.get(toEventClass));
						
						//store the new activities in between
						minimumSelfDistancesBetween.put(toEventClass, new HashSet<XEventClass>(readTrace.subList(eventSeenAt.get(toEventClass)+1, traceSize)));
					}
				}
				eventSeenAt.put(toEventClass, traceSize);
				
				if (fromEventClass != null) {
					
					//add edge to directly-follows graph
					edge = directlyFollowsGraph.addEdge(fromEventClass, toEventClass);
					Integer newCardinality = cardinality;
					if (edge == null) {
						edge = directlyFollowsGraph.getEdge(fromEventClass, toEventClass);
						newCardinality = newCardinality + (int) (directlyFollowsGraph.getEdgeWeight(edge));
					}
					directlyFollowsGraph.setEdgeWeight(edge, newCardinality);
					
				} else {
					startActivities.add(toEventClass, cardinality);
				}
				
				traceSize += 1;
			}
			
			//update the longest-trace-counter
			if (traceSize > longestTrace) {
				longestTrace = traceSize;
			}
			
			lengthStrongestTrace = Math.max(lengthStrongestTrace, cardinality);
			
			if (toEventClass != null) {
				endActivities.add(toEventClass, cardinality);
			}
			
			if (traceSize == 0) {
				numberOfEpsilonTraces = numberOfEpsilonTraces + cardinality;
			}
		}
		//debug(minimumSelfDistancesBetween.toString());
		
		//find the edge with the greatest weight
		strongestDirectEdge = 0;
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			strongestDirectEdge = Math.max(strongestDirectEdge, (int) directlyFollowsGraph.getEdgeWeight(edge));
		}
		
		//find the edge with the greatest weight
		strongestEventualEdge = 0;
		for (DefaultWeightedEdge edge : eventuallyFollowsGraph.edgeSet()) {
			strongestEventualEdge = Math.max(strongestEventualEdge, (int) eventuallyFollowsGraph.getEdgeWeight(edge));
		}
		
		//find the strongest start activity
		strongestStartActivity = 0;
		for (XEventClass activity : startActivities) {
			strongestStartActivity = Math.max(strongestStartActivity, startActivities.getCardinalityOf(activity));
		}
		
		//find the strongest end activity
		strongestEndActivity = 0;
		for (XEventClass activity : endActivities) {
			strongestEndActivity = Math.max(strongestEndActivity, endActivities.getCardinalityOf(activity));
		}
	}
	
	private DirectlyFollowsRelation(
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph,
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph,
			MultiSet<XEventClass> startActivities,
			MultiSet<XEventClass> endActivities,
			HashMap<XEventClass, Set<XEventClass>> minimumSelfDistancesBetween,
			HashMap<XEventClass, Integer> minimumSelfDistances,
			int numberOfEpsilonTraces,
			int longestTrace,
			int lengthStrongestTrace,
			int strongestDirectEdge,
			int strongestEventualEdge,
			int strongestStartActivity,
			int strongestEndActivity) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.eventuallyFollowsGraph = eventuallyFollowsGraph;
		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
		this.longestTrace = longestTrace;
		this.lengthStrongestTrace = lengthStrongestTrace;
		this.strongestDirectEdge = strongestDirectEdge;
		this.strongestEventualEdge = strongestEventualEdge;
		this.strongestStartActivity = strongestStartActivity;
		this.strongestEndActivity= strongestEndActivity;
	}
	
	public DirectlyFollowsRelation filterNoise(float threshold) {
		//filter start activities
		MultiSet<XEventClass> filteredStartActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : startActivities) {
			if (startActivities.getCardinalityOf(activity) >= strongestStartActivity * threshold) {
				filteredStartActivities.add(activity, startActivities.getCardinalityOf(activity));
			}
		}
		
		//filter end activities
		MultiSet<XEventClass> filteredEndActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : endActivities) {
			if (endActivities.getCardinalityOf(activity) >= strongestEndActivity * threshold) {
				filteredEndActivities.add(activity, endActivities.getCardinalityOf(activity));
			}
		}
		
		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {
			filteredDirectlyFollowsGraph.addVertex(activity);
		}
		//add edges
		
		/*
		//method 1: global threshold
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			if (directlyFollowsGraph.getEdgeWeight(edge) >= strongestDirectEdge * threshold) {
				XEventClass from = directlyFollowsGraph.getEdgeSource(edge);
				XEventClass to = directlyFollowsGraph.getEdgeTarget(edge);
				DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
				filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, directlyFollowsGraph.getEdgeWeight(edge));
			}
		}
		*/
		
		//method 2: local threshold
		for (XEventClass activity : directlyFollowsGraph.vertexSet()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = endActivities.getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : directlyFollowsGraph.outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) directlyFollowsGraph.getEdgeWeight(edge));
			}
			
			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : directlyFollowsGraph.outgoingEdgesOf(activity)) {
				if (directlyFollowsGraph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = directlyFollowsGraph.getEdgeSource(edge);
					XEventClass to = directlyFollowsGraph.getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
					filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, directlyFollowsGraph.getEdgeWeight(edge));
				}
			}
		}
		
		//filter eventually-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : eventuallyFollowsGraph.vertexSet()) {
			filteredEventuallyFollowsGraph.addVertex(activity);
		}
		//add edges
		/*
		//method 1: global threshold
		for (DefaultWeightedEdge edge : eventuallyFollowsGraph.edgeSet()) {
			if (eventuallyFollowsGraph.getEdgeWeight(edge) >= strongestEventualEdge * threshold) {
				XEventClass from = eventuallyFollowsGraph.getEdgeSource(edge);
				XEventClass to = eventuallyFollowsGraph.getEdgeTarget(edge);
				DefaultWeightedEdge filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
				filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge, eventuallyFollowsGraph.getEdgeWeight(edge));
			}
		}
		*/
		
		//method 2: local threshold
		for (XEventClass activity : eventuallyFollowsGraph.vertexSet()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = endActivities.getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : eventuallyFollowsGraph.outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) eventuallyFollowsGraph.getEdgeWeight(edge));
			}
			
			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : eventuallyFollowsGraph.outgoingEdgesOf(activity)) {
				if (eventuallyFollowsGraph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = eventuallyFollowsGraph.getEdgeSource(edge);
					XEventClass to = eventuallyFollowsGraph.getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
					filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge, eventuallyFollowsGraph.getEdgeWeight(edge));
				}
			}
		}
		
		return new DirectlyFollowsRelation(
				filteredDirectlyFollowsGraph, 
				filteredEventuallyFollowsGraph, 
				filteredStartActivities, 
				filteredEndActivities,
				minimumSelfDistancesBetween,
				minimumSelfDistances,
				numberOfEpsilonTraces,
				longestTrace,
				lengthStrongestTrace,
				strongestDirectEdge,
				strongestEventualEdge,
				strongestStartActivity,
				strongestEndActivity);
	}
	
	public String debugGraph() {
		String result = "nodes: " + directlyFollowsGraph.vertexSet().toString();
		
		result += "\nstart nodes: ";
		for (XEventClass event : startActivities) {
			result += event + " (" + startActivities.getCardinalityOf(event) + ") ";
		}
		
		result += "\nend nodes: ";
		for (XEventClass event : endActivities) {
			result += event + " (" + endActivities.getCardinalityOf(event) + ") ";
		}
		
		result += "\nedges: ";
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			result += "\t(" + directlyFollowsGraph.getEdgeSource(edge);
			result += " => " + directlyFollowsGraph.getEdgeTarget(edge) + " " + ((int) directlyFollowsGraph.getEdgeWeight(edge)) + ") ";
		}
		
		return result;
	}
	
	public String toDot(Collection<Set<XEventClass>> cut, boolean useEventuallyFollows) {
		if (cut == null) {
			return toDot(useEventuallyFollows);
		}
		
		final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph;
		if (!useEventuallyFollows) {
			graph = directlyFollowsGraph;
		} else {
			graph = eventuallyFollowsGraph;
		}
		
		String dot = "digraph G {\n";
		dot += "rankdir=LR;\n";
		
		//prepare the nodes
		HashMap<XEventClass, String> activityToNode = new HashMap<XEventClass, String>();
		for (Set<XEventClass> branch : cut) {
			dot += "subgraph \"cluster_"+ UUID.randomUUID().toString() + "\" {\n";
			for (XEventClass activity : branch) {
				String id = UUID.randomUUID().toString();
				activityToNode.put(activity, id);
				dot += "\"" + id + "\" [ label=\""+ activity.toString() +"\", shape=\"box\"";
				
				//determine node colour using start and end activities
				if (startActivities.contains(activity) && endActivities.contains(activity)) {
					dot += ", style=\"filled\""
							+ ", fillcolor=\""
							+ colourMapGreen(startActivities.getCardinalityOf(activity), strongestStartActivity)
							+ ":" + colourMapRed(endActivities.getCardinalityOf(activity), strongestEndActivity)
							+ "\"";
				} else if (startActivities.contains(activity)) {
					dot += ", style=\"filled\""
							+ ", fillcolor=\"" 
							+ colourMapGreen(startActivities.getCardinalityOf(activity), strongestStartActivity)
							+ ":white\"";
				} else if (endActivities.contains(activity)) {
					dot += ", style=\"filled\""
							+ ", fillcolor=\"white:" 
							+ colourMapRed(endActivities.getCardinalityOf(activity), strongestEndActivity)
							+ "\"";
				}
				
				dot += "];\n";
			}
			dot += "color=\"blue\";}\n";
		}
		
		//add the edges
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			XEventClass from = graph.getEdgeSource(edge);
			XEventClass to = graph.getEdgeTarget(edge);
			int weight = (int) graph.getEdgeWeight(edge);
			dot += "\"" + activityToNode.get(from) + "\" -> \"" + activityToNode.get(to) + "\" ["
					+ "label=\"" + String.valueOf(weight) + "\", "
					+ "color=\"" + colourMapBlackBody(weight, strongestDirectEdge) + "\""
					+ "];\n";
		}
		
		dot += "}\n";
		return dot;
		/*
		DOTExporter<XEventClass, DefaultWeightedEdge> dotExporter = 
	        new DOTExporter<XEventClass, DefaultWeightedEdge>(
        		new VertexNameProvider<XEventClass>() {
					public String getVertexName(XEventClass activity) {
						return "\"" + activity.toString() + "\"";
					}
        		},
        		new VertexNameProvider<XEventClass>() {
					public String getVertexName(XEventClass activity) {
						String result = activity.toString();
						result += "\" shape=\"box";
						if (startActivities.contains(activity) && endActivities.contains(activity)) {
							result += "\" style=\"filled";
							result += "\" fillcolor=\""
									+ colourMapGreen(startActivities.getCardinalityOf(activity), strongestStartActivity)
									+ ":" + colourMapRed(endActivities.getCardinalityOf(activity), strongestEndActivity);
						} else if (startActivities.contains(activity)) {
							result += "\" style=\"filled";
							result += "\" fillcolor=\"" 
									+ colourMapGreen(startActivities.getCardinalityOf(activity), strongestStartActivity)
									+ ":white";
						} else if (endActivities.contains(activity)) {
							result += "\" style=\"filled";
							result += "\" fillcolor=\"white:" 
									+ colourMapRed(endActivities.getCardinalityOf(activity), strongestEndActivity);
						}
						return result;
					}
        		},
        		new EdgeNameProvider<DefaultWeightedEdge>() { 
					public String getEdgeName(DefaultWeightedEdge edge) {
						//hack the color into the label of the edge using kind-of sql-injection
						int weight = (int) graph.getEdgeWeight(edge);
						String result = String.valueOf(weight);
						result += "\" color=\"" + colourMapBlackBody(weight, strongestDirectEdge);
						return result;
					} 
				}
        	);
		
		StringWriter out = new StringWriter();
		dotExporter.export(out, graph);
		return out.toString();
		*/
	}
	
	public String toDot(boolean useEventuallyFollows) {
		Set<Set<XEventClass>> cut = new HashSet<Set<XEventClass>>();
		cut.add(directlyFollowsGraph.vertexSet());
		return toDot(cut, useEventuallyFollows);
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}
	
	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getEventuallyFollowsGraph() {
		return eventuallyFollowsGraph;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}
	
	public int getNumberOfEpsilonTraces() {
		return numberOfEpsilonTraces;
	}
	
	public int getLengthStrongestTrace() {
		return lengthStrongestTrace;
	}
	
	public int getLongestTrace() {
		return longestTrace;
	}
	
	public Set<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new HashSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}
	
	private String colourMapBlackBody(int weight, int maxWeight) {
		float x = weight/(float) maxWeight;
		
		/*
		//blue-yellow
		x = (x * (float) 0.5) + (float) 0.5;
		return new Color(x, x, 1-x);
		*/
		x = (x * (float) 0.75) + (float) 0.25;
		
		//black-body
		Color colour = new Color(
				Math.min(Math.max((1-x) * 3, 0), 1),
				Math.min(Math.max((((1-x) - 1/(float) 3) * 3), 0), 1),
				Math.min(Math.max((((1-x) - 2/(float) 3) * 3), 0), 1));
		
		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
	
	private String colourMapRed(int weight, int maxWeight) {
		float x = weight/(float) maxWeight;
		
		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1, 1-x, 1-x);
		
		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
	
	private String colourMapGreen(int weight, int maxWeight) {
		float x = weight/(float) maxWeight;
		
		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1-x, 1, 1-x);
		
		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
	
	private void debug(String x) {
		System.out.println(x);
	}
}
