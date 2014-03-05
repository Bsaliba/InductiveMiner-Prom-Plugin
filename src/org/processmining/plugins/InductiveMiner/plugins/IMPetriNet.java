package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;


public class IMPetriNet {

	@Plugin(name = "Mine Petri net with Inductive Miner", returnLabels = { "Petri net", "Initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
			"Log", "Inductive Miner parameters" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public Object[] minePetriNet(PluginContext context, XLog log) {
		return minePetriNet(context, log, new MiningParametersIM());
	}
	
	@Plugin(name = "Mine Petri net with Inductive Miner", returnLabels = { "Petri net", "Initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
			"Log", "Inductive Miner parameters" }, userAccessible = false)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public Object[] minePetriNetParameters(PluginContext context, XLog log, MiningParameters parameters) {
		return minePetriNet(context, log, parameters);
	}
	
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		
		Object[] result = minePetriNet(log, parameters);
		
		//create Petri net connections
		context.addConnection(new InitialMarkingConnection((Petrinet) result[0], (Marking) result[1]));
		context.addConnection(new FinalMarkingConnection((Petrinet) result[0], (Marking) result[1]));
		
		return result;
	}
	
	public static Object[] minePetriNet(XLog log, MiningParameters parameters) {
		ProcessTree tree = IMProcessTree.mineProcessTree(log, parameters);
		PetrinetWithMarkings pn = null;
		try {
			pn = ProcessTree2Petrinet.convert(tree);
		} catch (NotYetImplementedException e) {
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			e.printStackTrace();
		}
		return new Object[]{pn.petrinet, pn.initialMarking, pn.finalMarking};
	}
	
}
