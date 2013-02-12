package bPrime.mining;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
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
import bPrime.Pair;
import bPrime.ProcessTreeModelParameters;

public class DirectlyFollowsRelation {
	private DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph;
	private MultiSet<XEventClass> startActivities;
	private MultiSet<XEventClass> endActivities;
	private HashMap<XEventClass, Set<XEventClass>> minimumSelfDistancesBetween;
	private HashMap<XEventClass, Integer> minimumSelfDistances;
	private boolean tauPresent;
	private int longestTrace;
	private int strongestEdge;
	private int strongestStartActivity;
	private int strongestEndActivity;
	
	public DirectlyFollowsRelation(Filteredlog log, ProcessTreeModelParameters parameters) {
		//initialise
		graph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		startActivities = new MultiSet<XEventClass>();
		endActivities = new MultiSet<XEventClass>();
		minimumSelfDistances = new HashMap<XEventClass, Integer>();
		minimumSelfDistancesBetween = new HashMap<XEventClass, Set<XEventClass>>();
		tauPresent = false;
		
		XEventClass fromEventClass;
		XEventClass toEventClass;
		longestTrace = 0;
		
		//add the nodes to the graph
		for (XEventClass a : log.getEventClasses()) {
			graph.addVertex(a);
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
					DefaultWeightedEdge edge = graph.addEdge(fromEventClass, toEventClass);
					Integer newCardinality = cardinality;
					if (edge == null) {
						edge = graph.getEdge(fromEventClass, toEventClass);
						newCardinality = newCardinality + (int) (graph.getEdgeWeight(edge));
					}
					graph.setEdgeWeight(edge, newCardinality);
					
				} else {
					startActivities.add(toEventClass, cardinality);
				}
				
				traceSize += 1;
			}
			
			//update the longest-trace-counter
			if (traceSize > longestTrace) {
				longestTrace = traceSize;
			}
			
			if (toEventClass != null) {
				endActivities.add(toEventClass, cardinality);
			}
			
			if (traceSize == 0) {
				tauPresent = true;
			}
		}
		//debug(minimumSelfDistancesBetween.toString());
		
		//find the edge with the greatest weight
		strongestEdge = 0;
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			strongestEdge = Math.max(strongestEdge, (int) graph.getEdgeWeight(edge));
		}
		
		//find the strongest start activity
		strongestStartActivity = 0;
		for (Pair<XEventClass, Integer> pair : startActivities) {
			strongestStartActivity = Math.max(strongestStartActivity, pair.getRight());
		}
		
		//find the strongest end activity
		strongestEndActivity = 0;
		for (Pair<XEventClass, Integer> pair : endActivities) {
			strongestEndActivity = Math.max(strongestEndActivity, pair.getRight());
		}
	}
	
	public void filterNoise(Integer threshold) {
		//filter start activities
		Iterator<Pair<XEventClass, Integer>> it = startActivities.iterator();
		while (it.hasNext()) {
			Pair<XEventClass, Integer> pair = it.next();
			if (pair.getRight() < threshold) {
				it.remove();
			}
		}
		
		//filter end activities
		Iterator<Pair<XEventClass, Integer>> it2 = endActivities.iterator();
		while (it2.hasNext()) {
			Pair<XEventClass, Integer> pair = it2.next();
			if (pair.getRight() < threshold) {
				it2.remove();
			}
		}
		
		//filter edges
		Set<DefaultWeightedEdge> removeSet = new HashSet<DefaultWeightedEdge>();
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			if (graph.getEdgeWeight(edge) < threshold) {
				removeSet.add(edge);
			}
		}
		graph.removeAllEdges(removeSet);
	}
	
	public String debugGraph() {
		String result = "nodes: " + graph.vertexSet().toString();
		
		result += "\nstart nodes: ";
		for (Pair<XEventClass, Integer> pair : startActivities) {
			result += pair.getLeft() + " (" + pair.getRight() + ") ";
		}
		
		result += "\nend nodes: ";
		for (Pair<XEventClass, Integer> pair : endActivities) {
			result += pair.getLeft() + " (" + pair.getRight() + ") ";
		}
		
		result += "\nedges: ";
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			result += "\t(" + graph.getEdgeSource(edge);
			result += " => " + graph.getEdgeTarget(edge) + " " + ((int) graph.getEdgeWeight(edge)) + ") ";
		}
		
		return result;
	}
	
	public void toDot(String fileName) {
		//Writer out = new BufferedWriter(new OutputStreamWriter(System.out));
		FileWriter out;
		try {
			out = new FileWriter(fileName + ".dot");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
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
						result += "\" color=\"" + colourMapBlackBody(weight, strongestEdge);
						return result;
					} 
				}
        	);
		dotExporter.export(out, graph);
		try {
			Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\" -Tpng -o\""+fileName+".png\" \""+fileName+".dot\"");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}
	
	public boolean getTauPresent() {
		return tauPresent;
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
}
