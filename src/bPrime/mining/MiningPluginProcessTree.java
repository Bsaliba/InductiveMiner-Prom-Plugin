package bPrime.mining;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.processtree.ProcessTree;

import bPrime.ProcessTreeModelConnection;
import bPrime.model.ProcessTreeModel;
import bPrime.model.conversion.ProcessTreeModel2ProcessTree;

@Plugin(name = "Mine a Process Tree using B'", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class MiningPluginProcessTree {
	//@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	//@PluginVariant(variantLabel = "Mine a Process Tree, default", requiredParameterLabels = { 0 })
	public ProcessTree mineDefault(PluginContext context, XLog log) {
		return this.mineParameters(context, log, new MiningParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, parameterized", requiredParameterLabels = { 0, 1 })
	public ProcessTree mineParameters(PluginContext context, XLog log, MiningParameters parameters) {
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
		Miner miner = new Miner();
		ProcessTreeModel model = miner.mine(context, log, parameters);
		ProcessTree tree = ProcessTreeModel2ProcessTree.convert(model.root);
		context.addConnection(new ProcessTreeModelConnection(log, tree, parameters));
		return tree;
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGui(UIPluginContext context, XLog log) {
		MiningParameters parameters = new MiningParameters();
		MiningDialog dialog = new MiningDialog(log, parameters);
		InteractionResult result = context.showWizard("Mine a Petri net using B'", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return mineParameters(context, log, parameters);
	}
}
