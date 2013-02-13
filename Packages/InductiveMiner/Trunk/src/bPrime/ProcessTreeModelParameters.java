package bPrime;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public class ProcessTreeModelParameters {
	private XEventClassifier classifier;
	private boolean filterNoise;
	private int noiseThreshold;
	
	private String outputDFGfileName;
	
	public ProcessTreeModelParameters() {
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		filterNoise = true;
		noiseThreshold = 3;
		outputDFGfileName = null;
	}
	
	public void setClassifier(XEventClassifier classifier) {
		if (classifier != null) {
			this.classifier = classifier;
		}
	}
	
	public XEventClassifier getClassifier() {
		return this.classifier;
	}
	
	public boolean getFilterNoise() {
		return this.filterNoise;
	}
	
	public int getNoiseThreshold() {
		return noiseThreshold;
	}
	
	public String getOutputDFGfileName() {
		return outputDFGfileName; 
	}

	public void setOutputDFGfileName(String outputFileName) {
		this.outputDFGfileName = outputFileName;
	}
	
	public boolean equals(Object object) {
		if (object instanceof ProcessTreeModelParameters) {
			ProcessTreeModelParameters parameters = (ProcessTreeModelParameters) object;
			if (classifier.equals(parameters.classifier)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return classifier.hashCode();
	}
}