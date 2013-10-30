package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.plugins.InductiveMiner.mining.SAT.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.SAT.ProbabilitiesEstimated;

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

	public MiningParameters() {
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		noiseThreshold = (float) 0.0;
		incompleteThreshold = (float) 0.0;
		outputDFGfileName = "D:\\output";
		outputFlowerLogFileName = null;
		useSAT = true;
		debug = true;
		useExhaustiveKSuccessor = false;
		satProbabilities = new ProbabilitiesEstimated(null);
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

	public boolean useSAT() {
		return useSAT;
	}

	public void setUseSAT(boolean useSAT) {
		this.useSAT = useSAT;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isUseExhaustiveKSuccessor() {
		return useExhaustiveKSuccessor;
	}

	public void setUseExhaustiveKSuccessor(boolean useExhaustiveKSuccessor) {
		this.useExhaustiveKSuccessor = useExhaustiveKSuccessor;
	}

	public Probabilities getSatProbabilities() {
		return satProbabilities;
	}

	public void setSatProbabilities(Probabilities satProbabilities) {
		this.satProbabilities = satProbabilities;
	}


}