package org.processmining.plugins.InductiveMiner.mining.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.model.Node;

public class fitness {
	
	@SuppressWarnings("unchecked")
	public static void computeFitness(Node node) {
		
		//events
		MultiSet<XEventClass> events = new MultiSet<XEventClass>();
		if (node.metadata.containsKey("filteredEvents")) {
			events.addAll((MultiSet<XEventClass>) node.metadata.get("filteredEvents"));
		}
		for (Node child : node.getChildren()) {
			computeFitness(child);
			events.addAll((MultiSet<XEventClass>) child.metadata.get("subtreeFilteredEvents"));
		}
		node.metadata.put("subtreeFilteredEvents", events);
		
		//empty traces
		int emptyTraces = 0;
		if (node.metadata.containsKey("filteredEmptyTraces")) {
			emptyTraces += ((AtomicInteger) node.metadata.get("filteredEmptyTraces")).get();
		}
		for (Node child : node.getChildren()) {
			emptyTraces += (Integer) child.metadata.get("subtreeFilteredEmptyTraces");
		}
		node.metadata.put("subtreeFilteredEmptyTraces", new Integer(emptyTraces));
		
		//store fitness as metadata
		if (node.metadata.containsKey("numberOfEvents") && node.metadata.containsKey("numberOfTraces")) {
			int logEvents = (Integer) node.metadata.get("numberOfEvents");
			int logTraces = (Integer) node.metadata.get("numberOfTraces");
			
			double fitness;
			if (logEvents + logTraces > 0) {
				fitness = 1 - (events.size() + emptyTraces) / (logEvents + logTraces * 1.0);
			} else {
				fitness = 1;
			}
			node.metadata.put("subtreeFitness", new Integer((int) Math.round(fitness * 100) ));
			debug("subtree fitness " + fitness + " " + node.toString());
		}
	}
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
