package org.processmining.plugins.InductiveMiner.model.conversion;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessor;
import org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations.UpToKSuccessorMatrix;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.Dot;

@Plugin(name = "Convert to k-successor relation", returnLabels = { "K-successor relation" }, returnTypes = { Dot.class }, parameterLabels = { "Process Tree", "Log" }, userAccessible = true)
public class ProcessTree2KSuccessorRelation {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert process tree to k-successor relation, default", requiredParameterLabels = { 0 })
	public Dot processTree2KSuccessorRelation(PluginContext context, ProcessTree tree) throws Exception {
		
		Dot result = new Dot();
		
		UpToKSuccessorMatrix r = UpToKSuccessor.fromNode(tree.getRoot());
		
		result.append(r.toString(true));
		debug(r.toString(false));
		
		return result;
		
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert log to k-successor relation, default", requiredParameterLabels = { 1 })
	public Dot processTree2KSuccessorRelation(PluginContext context, XLog log) throws Exception {
		MiningParameters parameters = new MiningParameters();
		Filteredlog flog = new Filteredlog(log, parameters);
		UpToKSuccessorMatrix r = UpToKSuccessor.fromLog(flog, parameters);
		
		Dot result = new Dot();
		result.append(r.toString(true));
		debug(r.toString(false));
		
		return result;
	}
	
	private void debug(String x) {
		System.out.println(x);
	}
	

}
