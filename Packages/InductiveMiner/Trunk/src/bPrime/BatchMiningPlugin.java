package bPrime;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.OpenLogFilePlugin;

import bPrime.mining.MiningPlugin;
import bPrime.model.ProcessTrees;

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
		String folder = "G:\\PROM\\";
		for (String file : getListOfFiles(folder)) {
			try {
				debug("starting log import " + file);
				XLog log = (XLog) logImporter.importFile(context, file);
				debug("import complete");
				processTrees.trees.add(plugin.mineDefault(context, log));
				debug("mining complete");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processTrees;
	}
	
	private List<String> getListOfFiles(String folder) {
		final List<String> result = new LinkedList<String>();
		try {
		    Path startPath = Paths.get(folder);
		    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
		        @Override
		        public FileVisitResult preVisitDirectory(Path dir,
		                BasicFileAttributes attrs) {
		            System.out.println("Dir: " + dir.toString());
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		        	String name = file.toString();
		        	System.out.println(name.substring(name.length()-4, name.length()) + "---");
		            if (name.substring(name.length()-4, name.length()).equalsIgnoreCase(".xes")) {
		    			System.out.println("File: " + file.toString());
		            	result.add(file.toString());
		            }
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult visitFileFailed(Path file, IOException e) {
		            return FileVisitResult.CONTINUE;
		        }
		    });
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		return result;
	}
	
	private void debug(String s) {
		System.out.println(s);
	}
}
