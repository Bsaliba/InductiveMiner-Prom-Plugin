package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class XLog2Dfg {
	@Plugin(name = "Convert log to directly-follows graph", returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class }, parameterLabels = {
	"Log" }, userAccessible = true, help="Convert a log into a directly-follows graph.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel("Directly-follows graph of " + XConceptExtension.instance().extractName(log));
		return log2Dfg(log);
	}
	
	public static Dfg log2Dfg(XLog log) {
		XEventClassifier classifier = MiningParameters.getDefaultClassifier();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		
		//read log
		Dfg dfg = new Dfg();
		for (XTrace trace : log) {
			XEventClass lastActivity = null;
			for (XEvent event : trace) {
				XEventClass activity = info.getEventClasses().getClassOf(event);
				dfg.addActivity(activity);
				
				if (lastActivity == null) {
					//first activity
					dfg.getStartActivities().add(activity);
				} else {
					//add directly-follows edge
					dfg.addDirectlyFollowsEdge(lastActivity, activity, 1);
				}
				
				lastActivity = activity;
			}
			
			//add end activity
			if (lastActivity != null) {
				dfg.getEndActivities().add(lastActivity);
			}
		}
		
		return dfg;
	}
	
}
