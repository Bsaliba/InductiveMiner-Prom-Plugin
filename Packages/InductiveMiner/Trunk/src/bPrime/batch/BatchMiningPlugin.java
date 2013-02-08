package bPrime.batch;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import bPrime.ProcessTreeModelParameters;
import bPrime.ThreadPool;
import bPrime.mining.MiningPlugin;
import bPrime.model.ProcessTreeModel;
import bPrime.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;

@Plugin(name = "Batch mine Process Trees using B'", returnLabels = { "Process Trees" }, returnTypes = { ProcessTrees.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class BatchMiningPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, default", requiredParameterLabels = { })
	public ProcessTrees mineDefault(PluginContext context) {
		BatchParameters parameters = new BatchParameters();
		
		//ask the user for the folder to be batch processed
		boolean repeat = true;
		while (repeat) {
			String folder = (String) JOptionPane.showInputDialog(null,
					"What is the folder to batch process?",
					"Provide Folder",
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					parameters.getFolder());
			if (folder == null) {
				return null;
			} else {
				File x = new File(folder);
				if (x.exists() && x.isDirectory()) {
					repeat = false;
					parameters.setFolder(folder);
				}
			}
		}
		
		return this.mineParameters(context, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, parameterized", requiredParameterLabels = { 1 })
	public ProcessTrees mineParameters(final PluginContext context, BatchParameters parameters) {
		
		//initialise for thread splitting
		ThreadPool pool = new ThreadPool(parameters.getNumberOfConcurrentFiles());
		File folder = new File(parameters.getFolder());
		List<String> files = getListOfFiles(folder, parameters.getExtensions());
		final ProcessTrees result = new ProcessTrees();
		final MiningPlugin miningPlugin = new MiningPlugin();
		final PNLogReplayer replayer = new PNLogReplayer();
		final boolean measurePrecision = parameters.getMeasurePrecision();
		
		for (String file2 : files) {
		
			final String file = file2;
			final int index = result.add();
			
			pool.addJob(
					new Runnable() {
			            public void run() {
			            	runJob(result, 
			            			index, 
			            			context, 
			            			file, 
			            			miningPlugin, 
			            			replayer, 
			            			measurePrecision);
			            }
					}
				);
		}
		
		try {
			pool.join();
		} catch (ExecutionException e) {
			//debug("something failed (thread join)");
			e.printStackTrace();
			return null;
		}
    	
		//debug("finished batch");
    	return result;
	}
	
	private void runJob(ProcessTrees result, 
			int index,
			PluginContext context,
			String fileName,
			MiningPlugin miningPlugin,
			PNLogReplayer replayer,
			boolean measurePrecision) {
		//perform the computations, store the result in result[index]
		
		//import the log
		//debug(fileName);
		XLog log;
		try {
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			log = (XLog) logImporter.importFile(context, fileName);
		} catch (Exception e) {
			//debug("error encountered (log import)");
			e.printStackTrace();
			return;
		}
		
		//mine the petri net
		ProcessTreeModelParameters parameters = new ProcessTreeModelParameters();
		Object[] arr = miningPlugin.mineParametersPetrinetWithoutConnections(context, log, parameters);
		ProcessTreeModel model = (ProcessTreeModel) arr[0];
		WorkflowNet workflowNet = (WorkflowNet) arr[1];
		Petrinet petrinet = workflowNet.petrinet;
		Marking initialMarking = workflowNet.initialMarking;
		Marking finalMarking = workflowNet.finalMarking;
		TransEvClassMapping mapping = (TransEvClassMapping) arr[2];
		XEventClass dummy = mapping.getDummyEventClass();
    	
		String comment;
		if (measurePrecision) {
		
	    	//replay the log
			XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			Collection<XEventClass> activities = info.getEventClasses().getClasses();
			
			PetrinetReplayerWithILP algorithm = new PetrinetReplayerWithILP();
			CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(activities, dummy, petrinet.getTransitions(), 1, 1);
			replayParameters.setInitialMarking(initialMarking);
			replayParameters.setFinalMarkings(new Marking[] {finalMarking});
			replayParameters.setCreateConn(false);
			replayParameters.setGUIMode(false);
			//replayParameters.setUseLogWeight(false);
			//Map<XEventClass, Integer> weightMap = new HashMap<XEventClass, Integer>();
			//weightMap.put(dummy, 0);
			//for (XEventClass activity : activities) {
			//	weightMap.put(activity, 1);
			//}
			//replayParameters.setxEventClassWeightMap(weightMap);
			PNRepResult replayed = null;
			try {
				replayed = replayer.replayLog(context, petrinet, log, mapping, algorithm, replayParameters);
			} catch (Exception e) {
				//debug("error encountered (replay algorithm)");
				e.printStackTrace();
				return;
			}
	    	
	    	//measure precision/generalisation
	    	AlignmentPrecGen precisionMeasurer = new AlignmentPrecGen();
	    	AlignmentPrecGenRes precisionGeneralisation = precisionMeasurer.measureConformanceAssumingCorrectAlignment(context, mapping, replayed, petrinet, initialMarking, true);
	    	
	    	comment = model.toHTMLString(false) + 
	    			"<br>precision " + precisionGeneralisation.getPrecision() +
	    			"<br>generalisation " + precisionGeneralisation.getGeneralization();
		} else {
			comment = model.toHTMLString(false);
		}
		
    	
    	result.set(index, fileName, comment);
	}
	
	private List<String> getListOfFiles(File file, Set<String> extensions) {
		List<String> result = new LinkedList<String>();
		if (file.isFile()) {
			String name = file.getName();
			if (extensions.contains(name.substring(name.length()-4, name.length()))) {
            	result.add(file.toString());
            }
		} else if (file.isDirectory()) {
			File[] listOfFiles = file.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++) {
					result.addAll(getListOfFiles(listOfFiles[i], extensions));
				}
			}
		}
		return result;
    }
	
	//private void debug(String s) {
	//	System.out.println(s);
	//}
}
