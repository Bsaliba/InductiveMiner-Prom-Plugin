package bPrime.batch;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BatchParameters {
	private String folder;
	private String PetrinetOutputFolder;
	private Dimension PetrinetOutputDimension;
	private int numberOfConcurrentFiles;
	private Set<String> extensions;
	private boolean measurePrecision;
	
	public BatchParameters() {
		//folder = "d:\\datasets\\boek\\chapter 5";
		folder = "D:\\datasets\\selected";
		
		PetrinetOutputFolder = "D:\\output";
		//PetrinetOutputFolder = null;
		PetrinetOutputDimension = new Dimension(1000, 500);
		numberOfConcurrentFiles = 0;
		extensions = new HashSet<String>(Arrays.asList(".xes", ".xml"));
		measurePrecision = false;
	}
	
	public void setFolder(String folder2) {
		folder = folder2;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public String getPetrinetOutputFolder() {
		return PetrinetOutputFolder;
	}
	
	public Dimension getPetrinetOutputDimension() {
		return PetrinetOutputDimension;
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
}