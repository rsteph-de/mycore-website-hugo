package org.mycore.website.hugo.builder.controller;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.mycore.website.hugo.builder.HugoBuilderDataStore;
import org.mycore.website.hugo.builder.HugoBuilderService;
import org.mycore.website.hugo.builder.model.GitCommit;
import org.mycore.website.hugo.builder.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class WebhookController {

//	private static String JSON_BEISPIEL = "{\"ref\":\"refs/heads/_generated\",\"before\":\"f4b5119e4433f2b5eeb4c1e7fa537158a4a53934\",\"after\":\"ae4e3738f19145bba9e6283ce9c491c2a77ce399\",\"created\":false,\"deleted\":false,\"forced\":false,\"base_ref\":null,\"compare\":\"https://github.com/rsteph-de/mycore-website-hugo/compare/f4b5119e4433...ae4e3738f191\",\"commits\":[{\"id\":\"ae4e3738f19145bba9e6283ce9c491c2a77ce399\",\"tree_id\":\"1249177b692c12a369b5a4da21e9a70f500187b4\",\"distinct\":true,\"message\":\"Update README.md\",\"timestamp\":\"2019-04-01T14:50:01+02:00\",\"url\":\"https://github.com/rsteph-de/mycore-website-hugo/commit/ae4e3738f19145bba9e6283ce9c491c2a77ce399\",\"author\":{\"name\":\"Robert\",\"email\":\"rsteph-de@users.noreply.github.com\",\"username\":\"rsteph-de\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"README.md\"]}],\"head_commit\":{\"id\":\"ae4e3738f19145bba9e6283ce9c491c2a77ce399\",\"tree_id\":\"1249177b692c12a369b5a4da21e9a70f500187b4\",\"distinct\":true,\"message\":\"Update README.md\",\"timestamp\":\"2019-04-01T14:50:01+02:00\",\"url\":\"https://github.com/rsteph-de/mycore-website-hugo/commit/ae4e3738f19145bba9e6283ce9c491c2a77ce399\",\"author\":{\"name\":\"Robert\",\"email\":\"rsteph-de@users.noreply.github.com\",\"username\":\"rsteph-de\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"README.md\"]},\"repository\":{\"id\":169969448,\"node_id\":\"MDEwOlJlcG9zaXRvcnkxNjk5Njk0NDg=\",\"name\":\"mycore-website-hugo\",\"full_name\":\"rsteph-de/mycore-website-hugo\",\"private\":false,\"owner\":{\"name\":\"rsteph-de\",\"email\":\"rsteph-de@users.noreply.github.com\",\"login\":\"rsteph-de\",\"id\":20067327,\"node_id\":\"MDQ6VXNlcjIwMDY3MzI3\",\"avatar_url\":\"https://avatars2.githubusercontent.com/u/20067327?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/rsteph-de\",\"html_url\":\"https://github.com/rsteph-de\",\"followers_url\":\"https://api.github.com/users/rsteph-de/followers\",\"following_url\":\"https://api.github.com/users/rsteph-de/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/rsteph-de/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/rsteph-de/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/rsteph-de/subscriptions\",\"organizations_url\":\"https://api.github.com/users/rsteph-de/orgs\",\"repos_url\":\"https://api.github.com/users/rsteph-de/repos\",\"events_url\":\"https://api.github.com/users/rsteph-de/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/rsteph-de/received_events\",\"type\":\"User\",\"site_admin\":false},\"html_url\":\"https://github.com/rsteph-de/mycore-website-hugo\",\"description\":\"Playground for a new MyCoRe website\",\"fork\":false,\"url\":\"https://github.com/rsteph-de/mycore-website-hugo\",\"forks_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/forks\",\"keys_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/teams\",\"hooks_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/hooks\",\"issue_events_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/events\",\"assignees_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/tags\",\"blobs_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/languages\",\"stargazers_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/stargazers\",\"contributors_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/contributors\",\"subscribers_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/subscribers\",\"subscription_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/subscription\",\"commits_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/merges\",\"archive_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/downloads\",\"issues_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/rsteph-de/mycore-website-hugo/deployments\",\"created_at\":1549796997,\"updated_at\":\"2019-04-01T12:38:48Z\",\"pushed_at\":1554123002,\"git_url\":\"git://github.com/rsteph-de/mycore-website-hugo.git\",\"ssh_url\":\"git@github.com:rsteph-de/mycore-website-hugo.git\",\"clone_url\":\"https://github.com/rsteph-de/mycore-website-hugo.git\",\"svn_url\":\"https://github.com/rsteph-de/mycore-website-hugo\",\"homepage\":null,\"size\":8449,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"JavaScript\",\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":0,\"mirror_url\":null,\"archived\":false,\"open_issues_count\":0,\"license\":null,\"forks\":0,\"open_issues\":0,\"watchers\":0,\"default_branch\":\"master\",\"stargazers\":0,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"rsteph-de\",\"email\":\"rsteph-de@users.noreply.github.com\"},\"sender\":{\"login\":\"rsteph-de\",\"id\":20067327,\"node_id\":\"MDQ6VXNlcjIwMDY3MzI3\",\"avatar_url\":\"https://avatars2.githubusercontent.com/u/20067327?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/rsteph-de\",\"html_url\":\"https://github.com/rsteph-de\",\"followers_url\":\"https://api.github.com/users/rsteph-de/followers\",\"following_url\":\"https://api.github.com/users/rsteph-de/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/rsteph-de/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/rsteph-de/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/rsteph-de/subscriptions\",\"organizations_url\":\"https://api.github.com/users/rsteph-de/orgs\",\"repos_url\":\"https://api.github.com/users/rsteph-de/repos\",\"events_url\":\"https://api.github.com/users/rsteph-de/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/rsteph-de/received_events\",\"type\":\"User\",\"site_admin\":false}}";
//	private static String SIGNATURE = "5401cf99215131b5f92af44684ba1d15ea5f80d7";
	
	@Autowired
	HugoBuilderDataStore builderData;
	
	@Autowired
	HugoBuilderService builderService;

	@Value("${mcr.hugo_builder.git.webhook.secret}")
	private String secret;
	@Value("${mcr.hugo_builder.git.webhook.branch}")
	private String branch;
	

	
	private static Logger LOGGER = LoggerFactory.getLogger(WebhookController.class);

	@PostMapping("/git-webhook")
	public ResponseEntity<Status> rebuildWebsite(@RequestHeader(value = "X-GitHub-Event") String gitEvent,
			@RequestHeader(value = "X-GitHub-Delivery") String gitDelivery,
			@RequestHeader(value = "X-Hub-Signature") String signature,
			@RequestHeader(value = "User-Agent") String userAgent, @RequestBody String json) {
		LOGGER.info("X-GitHub-Event: " + gitEvent);
		LOGGER.info("X-GitHub-Delivery: " + gitDelivery);
		LOGGER.info("X-Hub-Signature: " + signature);
		LOGGER.info("User-Agent: " + userAgent);
		LOGGER.info("JSON: " + json);

		try {
			JsonParser parser = new JsonParser();
			JsonObject jsonData = parser.parse(json).getAsJsonObject();
			String ref = jsonData.get("ref").getAsString();

			LOGGER.debug("user Agent: " + userAgent);
			LOGGER.debug("ref: " + ref);
			LOGGER.debug("Branch: " + branch);
			LOGGER.debug("validate: " + Boolean.toString(validate(secret, json, signature)));

			if ("push".equals(gitEvent) && userAgent.startsWith("GitHub-Hookshot/") && branch.equals(ref)) {
				String sig = signature.replace("sha1=", "");
				if (validate(secret, json, sig)) {
					List<GitCommit> commits = new ArrayList<>();
					for (JsonElement j : jsonData.get("commits").getAsJsonArray()) {
						commits.add(new GitCommit(j.getAsJsonObject()));
					}
					builderData.pushCommits(commits);
					
					//TODO create new Response-Object for Github
					//Nachricht: nächstes Hugo build started um ... (wenn zwischenzeitlich keine neuen Commits eingehen)
					
					Status result = new Status();
					result.setMessage("A hugo website build was scheduled within the next 3 minutes");
					result.setCompleted(LocalDateTime.now().plusMinutes(3).toString());
					return ResponseEntity.ok().body(result);
				}
			}
		} catch (Exception e) {
			LOGGER.error("JSON parser error.", e);
		}
		Status errorStatus = new Status();
		errorStatus.setSuccessful(false);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStatus);
	}

	@GetMapping("/run")
	public Status run() {
		GitCommit commit = new GitCommit();
		builderData.pushCommits(Arrays.asList(commit));
		builderData.getLastCommitTime().set(Long.MAX_VALUE);
		
		return  builderService.run();
	}

	@GetMapping("/status")
	@ResponseBody
	public List<Status> status() {
		LOGGER.info("Status aufgerufen!");
		return builderData.getStatusHistory();
	}

	private boolean validate(String secret, String message, String signature) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA1");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
			sha256_HMAC.init(secret_key);
			byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
			String hash = String.format("%040x", new BigInteger(1, bytes));
			return signature.equals(hash);
		} catch (Exception e) {
			LOGGER.error("Error validating json.", e);
		}
		return false;
	}
}