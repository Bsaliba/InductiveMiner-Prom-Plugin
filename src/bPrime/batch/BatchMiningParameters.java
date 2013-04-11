package bPrime.batch;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BatchMiningParameters {
	private File folder;
	private String PetrinetOutputFolder;
	private int numberOfConcurrentFiles;
	private Set<String> extensions;
	private boolean measurePrecision;
	private String SplitOutputFolder;
	private float noiseThreshold;
	
	public BatchMiningParameters() {
		folder = new File("d:\\datasets");
		
		PetrinetOutputFolder = "D:\\output";
		//PetrinetOutputFolder = null;
		
		//SplitOutputFolder = "D:\\output";
		SplitOutputFolder = null;
		
		setNoiseThreshold((float) 0.2);

		numberOfConcurrentFiles = 4;
		extensions = new HashSet<String>(Arrays.asList(".xes", ".xml", ".mxml"));
		measurePrecision = false;
	}
	
	public void setFolder(File folder) {
		this.folder = folder;
	}
	
	public File getFolder() {
		return folder;
	}
	
	public String getPetrinetOutputFolder() {
		return PetrinetOutputFolder;
	}
	
	public String getSplitOutputFolder() {
		return SplitOutputFolder;
	}
	
	public int getNumberOfConcurrentFiles() {
		return numberOfConcurrentFiles;
	}
	
	public Set<String> getExtensions() {
		return extensions;
	}
	
	public boolean getMeasurePrecision() {
		return measurePrecision;
	}
	
	public void setMeasurePrecision(boolean measurePrecision) {
		this.measurePrecision = measurePrecision;
	}

	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	public void setNoiseThreshold(float noiseThreshold) {
		this.noiseThreshold = noiseThreshold;
	}
}