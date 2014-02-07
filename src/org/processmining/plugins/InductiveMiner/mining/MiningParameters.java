package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
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
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallelWithMinimumSelfDistance;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.ProbabilitiesEstimatedZ;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;

public class MiningParameters {
	private XEventClassifier classifier;
	private float noiseThreshold;
	private float incompleteThreshold;
	private String outputDFGfileName;
	private File outputFlowerLogFileName;
	
	private boolean useSAT;
	private boolean useExhaustiveKSuccessor;
	private boolean debug;
	private Probabilities satProbabilities;
	private JobList minerPool;
	private JobList satPool;
	
	private List<BaseCaseFinder> baseCaseFinders;
	private List<CutFinder> cutFinders;
	private LogSplitter logSplitter;
	private List<FallThrough> fallThroughs;

	@Deprecated
	public MiningParameters() {
		
		
		//TODO: remove
		setBaseCaseFinders(new LinkedList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new LinkedList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMParallelWithMinimumSelfDistance(),
				new CutFinderIMLoop(),
				new CutFinderIMParallel()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new LinkedList<FallThrough>(Arrays.asList(
				new FallThroughFlower()
				)));
		
		classifier = getDefaultClassifier();
		noiseThreshold = (float) 0.0;
		incompleteThreshold = (float) 0.0;
		outputDFGfileName = "D:\\output";
		outputFlowerLogFileName = null;
		debug = true;
		
		useSAT = false;
		useExhaustiveKSuccessor = false;
		satProbabilities = new ProbabilitiesEstimatedZ();
		//satProbabilities = new ProbabilitiesNoise();
		
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

	@Deprecated
	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	@Deprecated
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

	@Deprecated
	public float getIncompleteThreshold() {
		return incompleteThreshold;
	}

	@Deprecated
	public void setIncompleteThreshold(float incompleteThreshold) {
		this.incompleteThreshold = incompleteThreshold;
	}

	@Deprecated
	public boolean isUseSat() {
		return useSAT;
	}

	@Deprecated
	public void setUseSAT(boolean useSAT) {
		this.useSAT = useSAT;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Deprecated
	public boolean isUseExhaustiveKSuccessor() {
		return useExhaustiveKSuccessor;
	}

	@Deprecated
	public void setUseExhaustiveKSuccessor(boolean useExhaustiveKSuccessor) {
		this.useExhaustiveKSuccessor = useExhaustiveKSuccessor;
	}

	@Deprecated
	public Probabilities getSatProbabilities() {
		return satProbabilities;
	}

	@Deprecated
	public void setSatProbabilities(Probabilities satProbabilities) {
		this.satProbabilities = satProbabilities;
	}
	
	public JobList getMinerPool() {
		return this.minerPool;
	}
	
	@Deprecated
	public JobList getSatPool() {
		return this.satPool;
	}
	
	@Deprecated
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