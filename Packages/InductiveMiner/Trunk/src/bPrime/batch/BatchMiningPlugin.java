package bPrime.batch;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.processtree.ProcessTree;

import bPrime.ProcessTreeModelParameters;
import bPrime.mining.MiningPlugin;
import bPrime.model.ConversionToProcessTrees;
import bPrime.model.ProcessTreeModel;

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
	public ProcessTrees mineParameters(PluginContext context, BatchParameters parameters) {
		MiningPlugin plugin = new MiningPlugin();
		ProcessTrees processTrees = new ProcessTrees();
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File folder = new File(parameters.getFolder());
		for (String file : getListOfFiles(folder)) {
			try {
				debug("starting log import " + file);
				XLog log = (XLog) logImporter.importFile(context, file);
				debug("import complete");
				ProcessTreeModel tree = plugin.mine(context, log, new ProcessTreeModelParameters());
				ProcessTree convertedTree = ConversionToProcessTrees.convert(tree.root);
				processTrees.add(convertedTree, tree.root, file);
				debug("mining complete");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processTrees;
	}
	
	private List<String> getListOfFiles(File file) {
		List<String> result = new LinkedList<String>();
		if (file.isFile()) {
			String name = file.getName();
			if (name.substring(name.length()-4, name.length()).equalsIgnoreCase(".xes")) {
    			//System.out.println("File: " + file.toString());
            	result.add(file.toString());
            }
		} else if (file.isDirectory()) {
			File[] listOfFiles = file.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++) {
					result.addAll(getListOfFiles(listOfFiles[i]));
				}
			}
		}
		return result;
    }
	
	private void debug(String s) {
		System.out.println(s);
	}
}
