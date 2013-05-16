package org.processmining.plugins.flowerMiner;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

@Plugin(name = "Mine a Petri net using FlowerMiner", returnLabels = { "Petri net", "Initial marking", "Final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
		"Log" }, userAccessible = true)
public class FlowerMiner {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = { 0 })
	public Object[] mineDefaultPetrinet(PluginContext context, XLog log) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClass dummy = new XEventClass("", 1);
		
		Object[] result = mineParametersPetrinet(logInfo, classifier, dummy);
		Petrinet petrinet = (Petrinet) result[0];
		Marking initialMarking = (Marking) result[1];
		Marking finalMarking = (Marking) result[2];
		TransEvClassMapping mapping = (TransEvClassMapping) result[3];
		
		//create connections
		context.addConnection(new LogPetrinetConnectionImpl(log, logInfo.getEventClasses(), petrinet, mapping));
		context.addConnection(new InitialMarkingConnection(petrinet, initialMarking));
		context.addConnection(new FinalMarkingConnection(petrinet, finalMarking));
		context.addConnection(new EvClassLogPetrinetConnection("classifier-log-petrinet connection", petrinet, log, classifier, mapping));
		
		return new Object[]{petrinet, initialMarking, finalMarking};
	}
	
	/*
	 * 'mines' a flower model, returns array of Object (petrinet, initial marking, final marking, mapping)
	 */
	public Object[] mineParametersPetrinet(XLogInfo logInfo, XEventClassifier classifier, XEventClass dummy) {
		
		Petrinet net = new PetrinetImpl("flower");
		Place source = net.addPlace("source");
		Place sink = net.addPlace("sink");
		Place stigma = net.addPlace("stigma");
		TransEvClassMapping mapping = new TransEvClassMapping(classifier, dummy);
		
		Transition start = net.addTransition("start");
		start.setInvisible(true);
		net.addArc(source, start);
		net.addArc(start, stigma);
		mapping.put(start, dummy);
		
		Transition end = net.addTransition("end");
		end.setInvisible(true);
		net.addArc(stigma, end);
		net.addArc(end, sink);
		mapping.put(end, dummy);
		
		for (XEventClass activity : logInfo.getEventClasses().getClasses()) {
			Transition t = net.addTransition(activity.toString());
			net.addArc(stigma, t);
			net.addArc(t, stigma);
			mapping.put(t, activity);
		}
		
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);
		
		return new Object[]{net, initialMarking, finalMarking, mapping};
	}
}
