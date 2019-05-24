package org.mycore.website.hugo.builder.model;

import java.util.ArrayList;
import java.util.List;

public class Status {

	private String completed;
	private String message;
	private boolean successful = true;
	private List<String> outputs = new ArrayList<String>();
	private int exitCode;
	private List<String> errors = new ArrayList<>();
	private List<GitCommit> commits = new ArrayList<>();
	
	public boolean isSuccessful() {
		return successful;
	}
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	public List<String> getErrors() {
		return errors;
	}
	public List<GitCommit> getCommits() {
		return commits;
	}
	public List<String> getOutputs() {
		return outputs;
	}
	public String getCompleted() {
		return completed;
	}
	public void setCompleted(String completed) {
		this.completed = completed;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
