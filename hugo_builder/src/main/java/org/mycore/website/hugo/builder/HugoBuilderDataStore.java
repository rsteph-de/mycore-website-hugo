package org.mycore.website.hugo.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.mycore.website.hugo.builder.model.GitCommit;
import org.mycore.website.hugo.builder.model.Status;
import org.springframework.stereotype.Repository;

@Repository
public class HugoBuilderDataStore {
	private Vector<GitCommit> gitCommits = new Vector<>();
	
	private AtomicLong lastCommitTime = new AtomicLong(Long.MAX_VALUE);
	
	private List<Status> statusHistory = new ArrayList<>();

	public void pushCommits(List<GitCommit> newCommits) {
		gitCommits.addAll(newCommits);
	}

	public List<GitCommit> pullAvailableCommits() {
		Vector<GitCommit> v = new Vector<>();
		v.addAll(gitCommits);
		gitCommits.removeAll(v);
		return v;
	}
	
	public void pushStatus(Status status){
		statusHistory.add(0, status);
		while (statusHistory.size() > 10) {
			statusHistory.remove(statusHistory.size() - 1);
		}
	}

	public boolean isEmpty() {
		return gitCommits.size() == 0;
	}

	public AtomicLong getLastCommitTime() {
		return lastCommitTime;
	}

	public List<Status> getStatusHistory() {
		return statusHistory;
	}
}
