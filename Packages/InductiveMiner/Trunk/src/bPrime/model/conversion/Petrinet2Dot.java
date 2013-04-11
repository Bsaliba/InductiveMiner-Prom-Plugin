package bPrime.model.conversion;

import java.util.HashMap;
import java.util.UUID;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class Petrinet2Dot {
	
	public String dot;
	
	public abstract class dotNode {
		public String id;
		public String style;
		
		public dotNode(String style) {
			this.id = UUID.randomUUID().toString();
			this.style = style;
			dot += "\"" + id + "\" [ "+style+" ];\n";
		}
	}
	
	public class dotPlace extends dotNode {
		public dotPlace(String label) {
			super("label=\"\", shape=\"circle\"");
		}
		public dotPlace(String label, String style) {
			super("label=\"\", shape=\"circle\", "+ style);
		}
	}
	
	public class dotTransition extends dotNode {
		//transition
		public dotTransition(String label) {
			super("label=\""+label+"\", shape=\"box\"");
		}
		//tau transition
		public dotTransition() {
			super("label=\"\", style=filled, fillcolor=\"#EEEEEE\", width=\"0.15\", shape=\"box\"");
		}
	}
	
	public String convert(Petrinet petrinet, Marking initialMarking, Marking finalMarking, String sinkColour) {
		
		HashMap<PetrinetNode, dotNode> mapPetrinet2Dot = new HashMap<PetrinetNode, dotNode>();
		
		dot = "digraph G {\n";
		dot += "rankdir=LR;\n";
		
		//add places
		for (Place place : petrinet.getPlaces()) {
			if (initialMarking != null && initialMarking.contains(place)) {
				mapPetrinet2Dot.put(place, new dotPlace(place.getLabel(), "style=filled, fillcolor=\"green\""));
			} else if (finalMarking != null && finalMarking.contains(place)) {
				mapPetrinet2Dot.put(place, new dotPlace(place.getLabel(), "style=filled, fillcolor=\"" + sinkColour + "\""));
			} else {
				mapPetrinet2Dot.put(place, new dotPlace(place.getLabel()));
			}
		}
		
		//add transitions
		for (Transition t : petrinet.getTransitions()) {
			if (t.isInvisible()) {
				mapPetrinet2Dot.put(t, new dotTransition());
			} else {
				mapPetrinet2Dot.put(t, new dotTransition(t.getLabel()));
			}
		}
		
		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : petrinet.getEdges()) {
			addArc(mapPetrinet2Dot.get(edge.getSource()), mapPetrinet2Dot.get(edge.getTarget()));
		}
		
		dot += "}\n";
		
		return dot;
	}
	
	private void addArc(dotNode from, dotNode to) {
		 dot += "\"" + from.id + "\" -> \"" + to.id + "\";\n";
	}
}
