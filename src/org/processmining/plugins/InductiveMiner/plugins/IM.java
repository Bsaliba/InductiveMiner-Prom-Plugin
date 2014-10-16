package org.processmining.plugins.InductiveMiner.plugins;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IM {

	@Plugin(name = "Mine process tree with Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMProcessTree.mineProcessTree(log, dialog.getMiningParameters());
	}

	@Plugin(name = "Mine Petri net with Inductive Miner", returnLabels = { "Petri net", "Initial marking",
			"final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return new Object[] { null, null, null };
		}
		return IMPetriNet.minePetriNet(context, log, dialog.getMiningParameters());
	}

	@Plugin(name = "Mine Process tree with Inductive Miner, with parameters", returnLabels = { "Process tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
			"Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static ProcessTree mineProcessTree(PluginContext context, XLog log, MiningParameters parameters) {
		return IMProcessTree.mineProcessTree(log, parameters);
	}

	@Plugin(name = "Mine Petri net with Inductive Miner, with parameters", returnLabels = { "Petri net",
			"Initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class }, parameterLabels = {
			"Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		return IMPetriNet.minePetriNet(context, log, parameters);
	}

	//make xloginfo to obtain a list of classifiers
	//		public List<XEventClassifier> getClassifiers(XLog xLog) {
	//			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(xLog);
	//			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>(xLogInfo.getEventClassifiers());
	//			classifiers.addAll(xLog.getClassifiers());
	//			classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
	//			Collections.sort(classifiers, new Comparator<XEventClassifier>() {
	//				public int compare(XEventClassifier o1, XEventClassifier o2) {
	//					return o1.name().compareTo(o2.name());
	//				}
	//			});
	//			return classifiers;
	//		}

	public static class ClassifierWrapper implements Comparable<ClassifierWrapper> {
		public final XEventClassifier classifier;
		public final String name;

		public ClassifierWrapper(String prefix, XEventClassifier classifier) {
			this.classifier = classifier;
			this.name = prefix + classifier.toString();
		}

		public String toString() {
			return name;
		}
		
		public int compareTo(ClassifierWrapper o) {
			return name.compareTo(o.name);
		}
	}

	public static ClassifierWrapper[] getClassifiers(XLog log) {
		Set<ClassifierWrapper> classifiers = new TreeSet<>();

		for (XEventClassifier c : log.getClassifiers()) {
			classifiers.add(new ClassifierWrapper("(log) ", c));
		}

		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log);
		for (XEventClassifier c : xLogInfo.getEventClassifiers()) {
			classifiers.add(new ClassifierWrapper("(log info) ", c));
		}

		ClassifierWrapper[] result = new ClassifierWrapper[classifiers.size() + 2];
		result[0] = new ClassifierWrapper("", new XEventNameClassifier());
		result[1] = new ClassifierWrapper("", new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		Iterator<ClassifierWrapper> it = classifiers.iterator();
		for (int i = 0; i < classifiers.size(); i++) {
			result[i + 2] = it.next();
		}
		return result;
	}
}
