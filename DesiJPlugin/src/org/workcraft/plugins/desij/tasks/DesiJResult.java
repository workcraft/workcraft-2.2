package org.workcraft.plugins.desij.tasks;

import java.io.File;

public class DesiJResult {
	private File specificationFile;
	private File[] componentFiles;
	private File logFile;
	private File modifiedSpecificationResult;
	private File equationsFile;
	
	public DesiJResult(File specFile, File[] compFiles, 
			File logFile, File modifiedSpecification, File equationsFile) {
		this.specificationFile = specFile;
		this.componentFiles = compFiles;
		this.logFile = logFile;
		this.modifiedSpecificationResult = modifiedSpecification;
		this.equationsFile = equationsFile;
	}
	
	public File getSpecificationFile() {
		return this.specificationFile;
	}
	
	/*
	 * Resulting component files
	 * --> could be null if no decomposition operation was performed
	 */
	public File[] getComponentFiles(){
		return this.componentFiles;
	}
	
	public File getLogFile() {
		return this.logFile;
	}
	
	/**
	 * @return specification without redundant places and/or dummies
	 * --> could be null if e.g. decomposition was performed
	 */
	public File getModifiedSpecResult() {
		return this.modifiedSpecificationResult;
	}
	
	/**
	 * @return equations for components generated by petrify or mpsat
	 * --> could be null if no synthesis is required or a required synthesis has been failed
	 */
	public File getEquationFile() {
		return this.equationsFile;
	}

}
