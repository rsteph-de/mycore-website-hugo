package org.mycore.website.hugo.builder.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

public class GitCommit {
	private String id;
	private String message;
	private String timestamp;
	private String url;
	private String author;
	private String committer;
	private List<String> added = new ArrayList<>();
	private List<String> removed = new ArrayList<>();
	private List<String> modified = new ArrayList<>();
	
	public GitCommit() {
		id = UUID.randomUUID().toString();
		message = "Build invoked by Hugo Builder /run";
		timestamp = ZonedDateTime.now().toString();
		url="";
		author = "hugo_builder";
		committer = "hugo_builder";
	}
	
	public GitCommit(JsonObject json) {
		id = json.get("id").getAsString();
		message = json.get("message").getAsString();
		timestamp = json.get("timestamp").getAsString();
		url = json.get("url").getAsString();
		author = json.get("author").getAsJsonObject().get("username").getAsString();
		committer = json.get("committer").getAsJsonObject().get("username").getAsString();
		
		
		json.get("added").getAsJsonArray().forEach(x -> {added.add(x.getAsString());});
		json.get("removed").getAsJsonArray().forEach(x -> {removed.add(x.getAsString());});
		json.get("modified").getAsJsonArray().forEach(x -> {modified.add(x.getAsString());});
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCommitter() {
		return committer;
	}
	public void setCommitter(String committer) {
		this.committer = committer;
	}
	public List<String> getAdded() {
		return added;
	}
	public List<String> getRemoved() {
		return removed;
	}
	public List<String> getModified() {
		return modified;
	}
}
