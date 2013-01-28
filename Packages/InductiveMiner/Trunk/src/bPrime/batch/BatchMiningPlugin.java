package bPrime.batch;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParam;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppPruneAlg;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import bPrime.ThreadPool;
import bPrime.mining.MiningPlugin;

@Plugin(name = "Batch mine Process Trees using B'", returnLabels = { "Process Trees" }, returnTypes = { ProcessTrees.class }, parameterLabels = {
		"Log", "Parameters" }, userAccessible = true)
public class BatchMiningPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, default", requiredParameterLabels = { })
	public ProcessTrees mineDefault(PluginContext context) {
		return this.mineParameters(context, new BatchParameters());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, parameterized", requiredParameterLabels = { 1 })
	public ProcessTrees mineParameters(final PluginContext context, BatchParameters parameters) {
		
		//initialise for thread splitting
		ThreadPool pool = new ThreadPool(parameters.getNumberOfConcurrentFiles());
		File folder = new File(parameters.getFolder());
		List<String> files = getListOfFiles(folder, parameters.getExtensions());
		final ProcessTrees result = new ProcessTrees();
		
		for (String file2 : files) {
		
			final String file = file2;
			final int index = result.add();
			
			pool.addJob(
					new Runnable() {
			            public void run() {
			            	runJob(result, index, context, file);
			            }
					}
				);
		}
    	
    	return result;
	}
	
	private void runJob(ProcessTrees result, 
			int index,
			PluginContext context,
			String file) {
		//perform the computations, store the result in result[index]
		
		//import the log
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		XLog log;
		try {
			log = (XLog) logImporter.importFile(context, file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		context.getProvidedObjectManager().createProvidedObject("Event log", log, context);
		
		//mine the petri net
		MiningPlugin plugin = new MiningPlugin();
    	Object[] arr = plugin.mineDefaultPetrinet(context, log);
    	Petrinet petrinet = (Petrinet) arr[0];
    	Marking initialMarking = (Marking) arr[1];
    	Marking finalMarking = (Marking) arr[2];
		context.getProvidedObjectManager().createProvidedObject("Petri net of " + file, petrinet, context);
    	context.getProvidedObjectManager().createProvidedObject("Initial marking of " + file, initialMarking, context);
    	context.getProvidedObjectManager().createProvidedObject("Final marking of " + file, finalMarking, context);
    	
    	//replay the log
    	PNLogReplayer replayer = new PNLogReplayer();
		
		TransEvClassMapping mapping = null;
		try {
			EvClassLogPetrinetConnection conn = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, petrinet, log);
			mapping = conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
		} catch (ConnectionCannotBeObtained e) {
			e.printStackTrace();
			return;
		}
		IPNReplayAlgorithm algorithm = new BehavAppPruneAlg();
		BehavAppParam replayParameters = new BehavAppParam();
		replayParameters.setInitialMarking(initialMarking);
		replayParameters.setFinalMarkings(finalMarking);
		replayParameters.setCreateConn(true);
		replayParameters.setGUIMode(false);
    	PNRepResult replayed = replayer.replayLog(context, petrinet, log, mapping, algorithm, replayParameters);
    	
    	//measure precision/generalisation
    	AlignmentPrecGen precisionMeasurer = new AlignmentPrecGen();
    	AlignmentPrecGenRes precisionGeneralisation = precisionMeasurer.measureConformanceAssumingCorrectAlignment(context, mapping, replayed, petrinet, initialMarking, true);
    	
    	String comment = "precision " + precisionGeneralisation.getPrecision() + "<br>generalisation " + precisionGeneralisation.getGeneralization();
    	result.set(index, file, comment);
	}
	
	/*
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine Process Trees, parameterized", requiredParameterLabels = { 1 })
	public PNRepResult mineParameters(final PluginContext context, BatchParameters parameters) {
		final MiningPlugin plugin = new MiningPlugin();
		final PNLogReplayer replayer = new PNLogReplayer();
		final ProcessTrees processTrees = new ProcessTrees();
		final OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		final AlignmentPrecGen precisionMeasurer = new AlignmentPrecGen();
		File folder = new File(parameters.getFolder());
		List<String> files = getListOfFiles(folder, parameters.getExtensions());
		
		//use multiple threads. It makes no sense to read in files using a thread pool, but it makes sense to mine multithreaded
		//however, we cannot load all logs in memory, so we need to do this concurrent as well
		//ThreadPool pool = new ThreadPool(parameters.getNumberOfConcurrentFiles());
		
		ThreadPool pool = new ThreadPool(0);
		String file = "D:\\datasets\\boek\\chapter 5\\L1.xes";
		
		
		for (String file : files) {
			final Integer index = processTrees.add();
			final String file2 = file;
			
			PNRepResult replayed = null;
			
			/*
			//mine the log in a thread
			pool.addJob(
				new Runnable() {
		            public void run() {
		            	//import the log
						debug("starting log import " + file2);
						try {
							//here we go
							
							//import the log
							XLog log = (XLog) logImporter.importFile(context, file2);
							debug("import complete");
							
							//mine the petri net
			            	Object[] arr = plugin.mineDefaultPetrinet(context, log);
			            	Petrinet petrinet = (Petrinet) arr[0];
			            	Marking initialMarking = (Marking) arr[1];
			            	Marking finalMarking = (Marking) arr[2];
			            	
			            	
			            	//replay the log
			            	ProcessTreeModelParameters Treeparameters = new ProcessTreeModelParameters();
			    			XEventClass dummy = new XEventClass("", 1);
			    			TransEvClassMapping mapping = new TransEvClassMapping(Treeparameters.getClassifier(), dummy);
			    			IPNReplayAlgorithm algorithm = new BehavAppPruneAlg();
			    			BehavAppParam replayParameters = new BehavAppParam();
			    			replayParameters.setInitialMarking(initialMarking);
			    			replayParameters.setFinalMarkings(finalMarking);
			    			//weight function
			    			Map<XEventClass, Integer> xEventClassWeightMap = new HashMap<XEventClass, Integer>();
			    			xEventClassWeightMap.put(dummy, 1);
			    			Collection<XEventClass> eventClasses = XLogInfoFactory.createLogInfo(log, Treeparameters.getClassifier()).getEventClasses().getClasses();
			    			for (XEventClass activity : eventClasses) {
			    				xEventClassWeightMap.put(activity, 1);
			    			}
			    			//replayParameters.setxEventClassWeightMap(xEventClassWeightMap);
			    			//replay
			            	//PNRepResult replayed = replayer.replayLog(context, petrinet, log, mapping, algorithm, replayParameters);
			    			replayed = replayer.replayLog(context, petrinet, log, mapping, algorithm, replayParameters);
			            	
			            	
			            	//measure precision/generalisation
			            	//AlignmentPrecGenRes precisionGeneralisation = precisionMeasurer.measureConformanceAssumingCorrectAlignment(context, mapping, replayed, petrinet, initialMarking, true);
			            	
			            	//debug(precisionGeneralisation.toHTMLString(false));
			            	
			            	//processTrees.set(index, tree.root, file2);
						} catch (Exception e) {
							e.printStackTrace();
						}
		            /*}
			});
		}
		
		try {
			pool.join();
		} catch (ExecutionException e) {
			debug("something failed");
			e.printStackTrace();
			return null;
		}
		
		return processTrees;
		
		return replayed;
	}*/
	
	private List<String> getListOfFiles(File file, Set<String> extensions) {
		List<String> result = new LinkedList<String>();
		if (file.isFile()) {
			String name = file.getName();
			if (extensions.contains(name.substring(name.length()-4, name.length()))) {
    			//System.out.println("File: " + file.toString());
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
