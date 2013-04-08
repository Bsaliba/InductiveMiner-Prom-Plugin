package bPrime.mining;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import bPrime.model.conversion.ProcessTreeModel2PetriNet;
import bPrime.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;

@Plugin(name = "Mine a Petri net using B'", returnLabels = { "Petri net", "Initial marking", "Final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)

public class MiningPluginPetrinet {
	//@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	//@PluginVariant(variantLabel = "Mine a Process Tree Petri net, default", requiredParameterLabels = { 0 })
	public Object[] mineDefaultPetrinet(PluginContext context, XLog log) {
		return this.mineParametersPetrinet(context, log, new MiningParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Petri net, parameterized", requiredParameterLabels = { 0, 1 })
	public Object[] mineParametersPetrinet(PluginContext context, XLog log, MiningParameters parameters) {
		/* there is no connection yet linking log, petri net, initial marking and final marking (or todo?)
		Collection<ProcessTreeModelConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(ProcessTreeModelConnection.class, context, log);
			for (ProcessTreeModelConnection connection : connections) {
				if (connection.getObjectWithRole(ProcessTreeModelConnection.LOG).equals(log)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ProcessTreeModelConnection.MODEL);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		*/
		
		//call the connectionless function
		Miner miner = new Miner();
		Object[] arr = miner.mineParametersPetrinetWithoutConnections(context, log, parameters);
		ProcessTreeModel2PetriNet.WorkflowNet workflowNet = (WorkflowNet) arr[1];
		TransEvClassMapping mapping = (TransEvClassMapping) arr[2];
		
		ProcessTreeModel2PetriNet.addMarkingsToProm(context, workflowNet);
		
		//create connections
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.addConnection(new LogPetrinetConnectionImpl(log, info.getEventClasses(), workflowNet.petrinet, workflowNet.transition2eventClass));
		
		context.addConnection(new EvClassLogPetrinetConnection("classifier-log-petrinet connection", workflowNet.petrinet, log, parameters.getClassifier(), mapping));
		
		return new Object[] { workflowNet.petrinet, workflowNet.initialMarking, workflowNet.finalMarking };
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Petri net, dialog", requiredParameterLabels = { 0 })
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParameters();
		MiningDialog dialog = new MiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine a Petri net using B'", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return mineParametersPetrinet(context, log, parameters);
	}
}
