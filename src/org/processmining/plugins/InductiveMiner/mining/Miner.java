package org.processmining.plugins.InductiveMiner.mining;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.plugins.InductiveMiner.model.ProcessTreeModel;
import org.processmining.plugins.InductiveMiner.model.conversion.ProcessTreeModel2PetriNet;
import org.processmining.plugins.InductiveMiner.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class Miner {

	/*
	 * basic usage:
	 * 
	 * don't use this class, use MiningPluginPetrinet or MiningPluginProcessTree instead
	 */

	@Deprecated
	public Object[] mineParametersPetrinetWithoutConnections(XLog log,
			MiningParameters parameters) {
		ProcessTreeModel model = mine(log, parameters);
		WorkflowNet workflowNet = ProcessTreeModel2PetriNet.convert(model.root);

		//create mapping
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping = new TransEvClassMapping(parameters.getClassifier(), dummy);
		for (Pair<Transition, XEventClass> p : workflowNet.transition2eventClass) {
			mapping.put(p.getFirst(), p.getSecond());
			activities.add(p.getSecond());
		}

		return new Object[] { model, workflowNet, mapping, activities };
	}
	
	@Deprecated
	public ProcessTreeModel mine(XLog log, MiningParameters parameters) {
		//prepare the log
		Filteredlog filteredLog = new Filteredlog(log, parameters.getClassifier());
		return mine(filteredLog, parameters);
	}

	@Deprecated
	public ProcessTreeModel mine(Filteredlog filteredLog, MiningParameters parameters) {
		return (ProcessTreeModel) Miner2.mine(filteredLog, parameters);
	}
}
