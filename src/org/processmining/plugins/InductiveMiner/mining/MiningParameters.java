package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.jobList.JobListBlocking;
import org.processmining.plugins.InductiveMiner.jobList.JobListConcurrent;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolSingleton1;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolSingleton2;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;

public abstract class MiningParameters {
	private XEventClassifier classifier;
	private float noiseThreshold;
	private float incompleteThreshold;
	private String outputDFGfileName;
	private File outputFlowerLogFileName;
	
	private boolean debug;
	private Probabilities satProbabilities;
	private JobList minerPool;
	private JobList satPool;
	
	private List<BaseCaseFinder> baseCaseFinders;
	private List<CutFinder> cutFinders;
	private LogSplitter logSplitter;
	private List<FallThrough> fallThroughs;

	protected MiningParameters() {
		
		classifier = getDefaultClassifier();
		debug = true;
		
		outputDFGfileName = "D:\\output";
		outputFlowerLogFileName = null;
		
		setUseMultithreading(true);
	}
	
	public static XEventClassifier getDefaultClassifier() {
		return new XEventAndClassifier(new XEventNameClassifier(), new XEventNameClassifier());
	}

	public void setClassifier(XEventClassifier classifier) {
		if (classifier != null) {
			this.classifier = classifier;
		}
	}

	public XEventClassifier getClassifier() {
		return this.classifier;
	}

	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	public void setNoiseThreshold(float noiseThreshold) {
		this.noiseThreshold = noiseThreshold;
	}

	public String getOutputDFGfileName() {
		return outputDFGfileName;
	}

	public void setOutputDFGfileName(String outputFileName) {
		this.outputDFGfileName = outputFileName;
	}

	public boolean equals(Object object) {
		if (object instanceof MiningParameters) {
			MiningParameters parameters = (MiningParameters) object;
			if (classifier.equals(parameters.classifier)) {
				if (noiseThreshold == parameters.getNoiseThreshold()) {
					if (incompleteThreshold == parameters.getIncompleteThreshold()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int hashCode() {
		return classifier.hashCode();
	}

	public File getOutputFlowerLogFileName() {
		return outputFlowerLogFileName;
	}

	public void setOutputFlowerLogFileName(File outputFlowerLogFileName) {
		this.outputFlowerLogFileName = outputFlowerLogFileName;
	}

	public float getIncompleteThreshold() {
		return incompleteThreshold;
	}

	public void setIncompleteThreshold(float incompleteThreshold) {
		this.incompleteThreshold = incompleteThreshold;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public Probabilities getSatProbabilities() {
		return satProbabilities;
	}

	public void setSatProbabilities(Probabilities satProbabilities) {
		this.satProbabilities = satProbabilities;
	}
	
	public JobList getMinerPool() {
		return this.minerPool;
	}
	
	public JobList getSatPool() {
		return this.satPool;
	}
	
	public void setUseMultithreading(boolean useMultithreading) {
		setUseMultithreadingGlobal(useMultithreading);
		
		if (useMultithreading) {
			satPool = new JobListBlocking();
		} else {
			satPool = new JobListConcurrent(ThreadPoolSingleton1.getInstance());
		}
	}
	
	protected void setUseMultithreadingGlobal(boolean useMultithreading) {
		if (useMultithreading) {
			minerPool = new JobListBlocking();
		} else {
			minerPool = new JobListConcurrent(ThreadPoolSingleton2.getInstance());
		}
	}

	public List<BaseCaseFinder> getBaseCaseFinders() {
		return baseCaseFinders;
	}

	public void setBaseCaseFinders(List<BaseCaseFinder> baseCaseFinders) {
		this.baseCaseFinders = baseCaseFinders;
	}

	public List<CutFinder> getCutFinders() {
		return cutFinders;
	}

	public void setCutFinder(List<CutFinder> cutFinders) {
		this.cutFinders = cutFinders;
	}

	public LogSplitter getLogSplitter() {
		return logSplitter;
	}

	public void setLogSplitter(LogSplitter logSplitter) {
		this.logSplitter = logSplitter;
	}

	public List<FallThrough> getFallThroughs() {
		return fallThroughs;
	}

	public void setFallThroughs(List<FallThrough> fallThroughs) {
		this.fallThroughs = fallThroughs;
	}


}