package bPrime.model.conversion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;

import bPrime.model.EventClass;
import bPrime.model.ExclusiveChoice;
import bPrime.model.Loop;
import bPrime.model.Node;
import bPrime.model.Parallel;
import bPrime.model.Sequence;
import bPrime.model.Tau;

public class ProcessTreeModel2PetriNet {
	
	public static class WorkflowNet {
		public Petrinet petrinet;
		public Place source;
		public Place sink;
		public List<Pair<Transition, XEventClass>> transition2eventClass;
		public Marking initialMarking;
		public Marking finalMarking;
	}
	
	public static void addMarkingsToProm(PluginContext context, WorkflowNet workflowNet) {
		context.addConnection(new InitialMarkingConnection(workflowNet.petrinet, workflowNet.initialMarking));
		context.addConnection(new FinalMarkingConnection(workflowNet.petrinet, workflowNet.finalMarking));
	}
	
	public static WorkflowNet convert(Node root) {
		WorkflowNet workflowNet = new WorkflowNet();
		workflowNet.petrinet = new PetrinetImpl(root.toString());
		workflowNet.source = workflowNet.petrinet.addPlace("source");
		workflowNet.sink = workflowNet.petrinet.addPlace("sink");
		workflowNet.transition2eventClass = new ArrayList<Pair<Transition, XEventClass>>();
		workflowNet.initialMarking = new Marking();
		workflowNet.initialMarking.add(workflowNet.source);
		workflowNet.finalMarking = new Marking();
		workflowNet.finalMarking.add(workflowNet.sink);
		
		convertNode(workflowNet, workflowNet.source, workflowNet.sink, root, false, true);
		
		return workflowNet;
	}
	
	private static void convertNode(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			Node node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		if (node instanceof Tau) {
			convertTau(workflowNet, source, sink, (Tau) node, assureNoInputPlacementment, assureNoOutputRemoval);
		} else if (node instanceof EventClass) {
			convertActivity(workflowNet, source, sink, (EventClass) node, assureNoInputPlacementment, assureNoOutputRemoval);
		} else if (node instanceof ExclusiveChoice) {
			convertExclusiveChoice(workflowNet, source, sink, (ExclusiveChoice) node, assureNoInputPlacementment, assureNoOutputRemoval);
		} else if (node instanceof Sequence) {
			convertSequence(workflowNet, source, sink, (Sequence) node, assureNoInputPlacementment, assureNoOutputRemoval);
		} else if (node instanceof Parallel) {
			convertParallel(workflowNet, source, sink, (Parallel) node, assureNoInputPlacementment, assureNoOutputRemoval);
		} else if (node instanceof Loop) {
			convertLoop(workflowNet, source, sink, (Loop) node, assureNoInputPlacementment, assureNoOutputRemoval);
		}
	}

	private static void convertTau(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			Tau node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		Transition t = workflowNet.petrinet.addTransition("");
		t.setInvisible(true);
		workflowNet.petrinet.addArc(source, t);
		workflowNet.petrinet.addArc(t, sink);
	}
	
	private static void convertActivity(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			EventClass node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		Transition t = workflowNet.petrinet.addTransition(node.toString());
		workflowNet.petrinet.addArc(source, t);
		workflowNet.petrinet.addArc(t, sink);
		workflowNet.transition2eventClass.add(new Pair<Transition,XEventClass>(t, node.eventClass));
	}
	
	private static void convertExclusiveChoice(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			ExclusiveChoice node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {	
		for (Node child : node.getChildren()) {
			convertNode(workflowNet, source, sink, child, true, true);
		}
	}
	
	private static void convertSequence(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			Sequence node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		int last = node.getChildren().size();
		int i = 0;
		Place lastSink = source;
		for (Node child : node.getChildren()) {
			Place childSink;
			if (i == last - 1) {
				childSink = sink;
			} else {
				childSink = workflowNet.petrinet.addPlace(i + " sink " + node.toString());
			}
			
			boolean assureBefore = (i == 0 && assureNoInputPlacementment);
			boolean assureAfter = (i == last - 1 && assureNoOutputRemoval) || (i < last - 1);
			
			convertNode(workflowNet, lastSink, childSink, child, assureBefore, assureAfter);
			lastSink = childSink;
			i++;
		}
	}
	
	private static void convertParallel(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			Parallel node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		//add split tau
		Transition t1 = workflowNet.petrinet.addTransition("");
		t1.setInvisible(true);
		workflowNet.petrinet.addArc(source, t1);
		
		//add join tau
		Transition t2 = workflowNet.petrinet.addTransition("");
		t2.setInvisible(true);
		workflowNet.petrinet.addArc(t2, sink);
		
		//add for each child a source and sink place
		int i = 0;
		for (Node child : node.getChildren()) {
			Place childSource = workflowNet.petrinet.addPlace("source " + i + " " + node.toString());
			workflowNet.petrinet.addArc(t1, childSource);
			
			Place childSink = workflowNet.petrinet.addPlace("sink " + i + " " + node.toString());
			workflowNet.petrinet.addArc(childSink, t2);
			
			convertNode(workflowNet, childSource, childSink, child, false, false);
			i++;
		}
	}
	
	private static void convertLoop(WorkflowNet workflowNet, 
			Place source, 
			Place sink, 
			Loop node,
			boolean assureNoInputPlacementment,
			boolean assureNoOutputRemoval) {
		
		Place child1Source;
		if (assureNoInputPlacementment) {
			//create child 1 source place
			child1Source = workflowNet.petrinet.addPlace("source 1 " + node.toString());
			
			//create transition from child 1 sink place to sink
			Transition t1 = workflowNet.petrinet.addTransition("");
			t1.setInvisible(true);
			workflowNet.petrinet.addArc(source, t1);
			workflowNet.petrinet.addArc(t1, child1Source);
		} else {
			child1Source = source;
		}
		
		Place child1Sink;
		if (assureNoOutputRemoval) {
			//create child 1 sink place
			child1Sink = workflowNet.petrinet.addPlace("sink 1 " + node.toString());
			
			//create transition from child 1 sink place to sink
			Transition t2 = workflowNet.petrinet.addTransition("");
			t2.setInvisible(true);
			workflowNet.petrinet.addArc(child1Sink, t2);
			workflowNet.petrinet.addArc(t2, sink);
		} else {
			child1Sink = sink;
		}
		
		//convert the first child
		Iterator<Node> i = node.getChildren().iterator();
		convertNode(workflowNet, child1Source, child1Sink, i.next(), assureNoInputPlacementment, true);
		
		//convert the other children
		while (i.hasNext()) {
			/*
			//create child sink and source
			Place childNSource = workflowNet.petrinet.addPlace("source " + i + " " + node.toString());
			
			//create transitions connecting them
			Transition t3 = workflowNet.petrinet.addTransition("");
			t3.setInvisible(true);
			workflowNet.petrinet.addArc(child1Sink, t3);
			workflowNet.petrinet.addArc(t3, childNSource);
			
			convertNode(workflowNet, childNSource, child1Source, i.next(), true, assureNoOutputRemoval);
			*/
			
			convertNode(workflowNet, child1Sink, child1Source, i.next(), true, true);			
		}
	}	
}