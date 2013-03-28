package bPrime.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import bPrime.ThreadPool;
import bPrime.mining.MiningParameters;
import bPrime.mining.MiningPlugin;
import bPrime.model.ProcessTreeModel;
import bPrime.model.conversion.Dot2Image;
import bPrime.model.conversion.ProcessTreeModel2Dot;
import bPrime.model.conversion.ProcessTreeModel2PetriNet.WorkflowNet;

@Plugin(name = "Batch mine Process Trees using B'", returnLabels = { "Batch Process Trees" }, returnTypes = { BatchProcessTrees.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class BatchMiningPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Batch Process Trees, default", requiredParameterLabels = { })
	public BatchProcessTrees mineDefault(PluginContext context) {
		BatchMiningParameters parameters = new BatchMiningParameters();
		
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
		
		String noiseThresholdString = (String) JOptionPane.showInputDialog(null,
				"What is the noise threshold?",
				"Noise threshold",
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				parameters.getNoiseThreshold());
		if (noiseThresholdString == null) {
			return null;
		}
		parameters.setNoiseThreshold(Float.parseFloat(noiseThresholdString));
		
		return this.mineParameters(context, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, parameterized", requiredParameterLabels = { 1 })
	public BatchProcessTrees mineParameters(final PluginContext context, final BatchMiningParameters parameters) {
		
		//initialise for thread splitting
		ThreadPool pool = new ThreadPool(parameters.getNumberOfConcurrentFiles());
		File folder = new File(parameters.getFolder());
		List<String> files = getListOfFiles(folder, parameters.getExtensions());
		final BatchProcessTrees result = new BatchProcessTrees();
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
			            			measurePrecision,
			            			parameters);
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
		
		//write the result to an HTML file
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter( "D:\\output\\index.html"));
			writer.write(result.toHTMLString(true));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
		//debug("finished batch");
    	return result;
	}
	
	private void runJob(BatchProcessTrees result, 
			int index,
			PluginContext context,
			String fileName,
			MiningPlugin miningPlugin,
			PNLogReplayer replayer,
			boolean measurePrecision,
			BatchMiningParameters batchParameters) {
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
		
		//set up files for output
		File outputFilePDF = null;
		File outputFilePNG = null;
		String outputFileDFG = null;
		if (batchParameters.getPetrinetOutputFolder() != null) {
			String x = new File(fileName).getName();
			if (x.indexOf(".") > 0) {
			    x = x.substring(0, x.lastIndexOf("."));
			}
			outputFilePDF = new File(batchParameters.getPetrinetOutputFolder(), x + "- petrinet.pdf");
			outputFilePNG = new File(batchParameters.getPetrinetOutputFolder(), x + "- petrinet.png");
			if (batchParameters.getSplitOutputFolder() != null) {
				outputFileDFG = new File(batchParameters.getSplitOutputFolder(), x + "--").getPath();
			}
		}
		
		//enable output of directly-follows images if wanted
		MiningParameters mineParameters = new MiningParameters();
		if (batchParameters.getSplitOutputFolder() != null) {
			mineParameters.setOutputDFGfileName(outputFileDFG);
		}
		mineParameters.setNoiseThreshold(batchParameters.getNoiseThreshold());
		
		//mine the Petri net
		Object[] arr = miningPlugin.mineParametersPetrinetWithoutConnections(context, log, mineParameters);
		ProcessTreeModel model = (ProcessTreeModel) arr[0];
		WorkflowNet workflowNet = (WorkflowNet) arr[1];
		Petrinet petrinet = workflowNet.petrinet;
		Marking initialMarking = workflowNet.initialMarking;
		Marking finalMarking = workflowNet.finalMarking;
		TransEvClassMapping mapping = (TransEvClassMapping) arr[2];
		XEventClass dummy = mapping.getDummyEventClass();
		
		//Visualise the Petri net
		ProcessTreeModel2Dot converter2dot = new ProcessTreeModel2Dot();
		Dot2Image.dot2image(converter2dot.convert2PetriNet(model.root), outputFilePNG, outputFilePDF);
		
		//measure precision
		String comment = "";
		if (measurePrecision) {
		
	    	//replay the log
			XLogInfo info = XLogInfoFactory.createLogInfo(log, mineParameters.getClassifier());
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
	    	
	    	comment = "precision " + precisionGeneralisation.getPrecision() + "<br>" +
	    			"generalisation " + precisionGeneralisation.getGeneralization() + "<br>";
		}
		
		comment += "fitness " + model.fitness + "<br>";
		comment += model.toHTMLString(false) + "<br>";
		comment += "<img src='"+outputFilePNG.getName()+"' style='max-width: 1900px; max-height: 900px;'>";
    	
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
	
	private void debug(String s) {
		System.out.println(s);
	}
}
