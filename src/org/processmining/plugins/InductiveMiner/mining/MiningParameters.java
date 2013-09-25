package org.processmining.plugins.InductiveMiner.mining;

import java.io.File;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public class MiningParameters {
	private XEventClassifier classifier;
	private float noiseThreshold;
	private float incompleteThreshold;
	private String outputDFGfileName;
	private File outputFlowerLogFileName;
	
	private boolean useSAT;
	private boolean useExhaustiveKSuccessor;
	private boolean debug;
	private int satType;
	private double satParameter;

	public MiningParameters() {
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		noiseThreshold = (float) 0.0;
		incompleteThreshold = (float) 0.5;
		outputDFGfileName = "D:\\output";
		outputFlowerLogFileName = null;
		useSAT = false;
		debug = true;
		useExhaustiveKSuccessor = true;
		satType = 2;
		setSatParameter(0.1);
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

	public int getSatType() {
		return satType;
	}

	public void setSatType(int satType) {
		this.satType = satType;
	}

	public double getSatParameter() {
		return satParameter;
	}

	public void setSatParameter(double satParameter) {
		this.satParameter = satParameter;
	}
}