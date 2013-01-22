package bPrime;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public class BatchParameters {
	private XEventClassifier classifier;
	private String folder;
	
	public BatchParameters() {
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		folder = "G:\\PROM";
	}
	
	public void setClassifier(XEventClassifier classifier) {
		if (classifier != null) {
			this.classifier = classifier;
		}
	}
	
	public XEventClassifier getClassifier() {
		return this.classifier;
	}
	
	public boolean equals(Object object) {
		if (object instanceof BatchParameters) {
			BatchParameters parameters = (BatchParameters) object;
			if (classifier.equals(parameters.classifier)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return classifier.hashCode();
	}
	
	public String getFolder() {
		return folder;
	}
}