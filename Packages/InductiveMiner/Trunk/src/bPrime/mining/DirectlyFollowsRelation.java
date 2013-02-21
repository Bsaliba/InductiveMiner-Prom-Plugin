package bPrime.mining;

import java.awt.Color;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
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
	
	public void filterNoise(float threshold) {
		//filter start activities
		Iterator<XEventClass> it = startActivities.iterator();
		while (it.hasNext()) {
			if (startActivities.getCardinalityOf(it.next()) < strongestStartActivity * threshold) {
				it.remove();
			}
		}
		
		//filter end activities
		Iterator<XEventClass> it2 = endActivities.iterator();
		while (it2.hasNext()) {
			if (endActivities.getCardinalityOf(it2.next()) < strongestEndActivity * threshold) {
				it2.remove();
			}
		}
		
		//filter edges in directly-follows graph
		Set<DefaultWeightedEdge> removeSet = new HashSet<DefaultWeightedEdge>();
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			if (directlyFollowsGraph.getEdgeWeight(edge) < strongestDirectEdge * threshold) {
				removeSet.add(edge);
			}
		}
		directlyFollowsGraph.removeAllEdges(removeSet);
		
		//filter edges in eventually-follows graph
		removeSet = new HashSet<DefaultWeightedEdge>();
		for (DefaultWeightedEdge edge : eventuallyFollowsGraph.edgeSet()) {
			if (eventuallyFollowsGraph.getEdgeWeight(edge) < strongestEventualEdge * threshold) {
				removeSet.add(edge);
			}
		}
		eventuallyFollowsGraph.removeAllEdges(removeSet);
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
	
	public String toDot() {
		final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = directlyFollowsGraph;
		
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
