package org.processmining.plugins.InductiveMiner.mining;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolSingleton1;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolSingleton2;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;

import com.google.common.util.concurrent.MoreExecutors;

public abstract class MiningParameters {
	private XEventClassifier classifier;
	private float noiseThreshold;
	private float incompleteThreshold;

	private boolean debug;
	private boolean reduce;
	private Probabilities satProbabilities;
	private ExecutorService minerPool;
	private ExecutorService satPool;

	private IMLog2IMLogInfo log2logInfo;
	private List<BaseCaseFinder> baseCaseFinders;
	private List<CutFinder> cutFinders;
	private LogSplitter logSplitter;
	private List<FallThrough> fallThroughs;

	protected MiningParameters() {

		classifier = getDefaultClassifier();
		debug = false;

		setUseMultithreading(true);
	}

	public static XEventClassifier getDefaultClassifier() {
		//return new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		return new XEventNameClassifier();
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

	public ExecutorService getMinerPool() {
		return this.minerPool;
	}

	public ExecutorService getSatPool() {
		return this.satPool;
	}

	public void setUseMultithreading(boolean useMultithreading) {
		if (!useMultithreading) {
			minerPool = MoreExecutors.sameThreadExecutor();
			satPool = MoreExecutors.sameThreadExecutor();
		} else {
			minerPool = ThreadPoolSingleton2.getInstance();
			satPool = ThreadPoolSingleton1.getInstance();
		}
	}

	public IMLog2IMLogInfo getLog2LogInfo() {
		return log2logInfo;
	}

	public void setLog2LogInfo(IMLog2IMLogInfo log2logInfo) {
		this.log2logInfo = log2logInfo;
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

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		this.reduce = reduce;
	}

}