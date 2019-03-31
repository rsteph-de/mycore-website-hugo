package org.mycore.website.hugo.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.mycore.website.hugo.builder.util.Utitlities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HugoBuilderService {

	public static Log LOGGER = LogFactory.getLog(HugoBuilderService.class);

	@Value("${mcr.hugo_builder.workingdir}")
	private String nameWorkingDir;

	private Path pWorkingDir;

	@Value("${mcr.hugo_builder.git_repository}")
	private String gitURL;

	@Value("${mcr.hugo_builder.hugo_cmd}")
	private String hugoCommand;

	@PostConstruct
	private void init() {
		pWorkingDir = Paths.get(nameWorkingDir);
	}

	public synchronized void run() {
		try {
			if (Files.exists(pWorkingDir)) {
				Utitlities.deleteDirectory(pWorkingDir);
			}
			Files.createDirectories(pWorkingDir);
		} catch (IOException e) {
			LOGGER.error("Error creating ddirectory: " + pWorkingDir);
		}
		gitCheckout();
		runHugo();
	}

	private void gitCheckout() {
		try (Git git = Git.cloneRepository().setURI(gitURL).setDirectory(pWorkingDir.toFile()).call()) {
			Ref ref = git.checkout().setCreateBranch(true).setName("new_hugo")
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setStartPoint("origin/master").call();
		} catch (Exception e) {
			LOGGER.error("Error at Git-Checkout: " + gitURL + "to: " + pWorkingDir, e);
		}
	}

	public void runHugo() {
		StringBuilder output = new StringBuilder("\n");
		try {
		ProcessBuilder pbMvn = new ProcessBuilder();
		pbMvn.command("\"C:\\Program Files\\apache-maven-3.3.9\\bin\\mvn.cmd\"", "clean install");
		pbMvn.directory(pWorkingDir.toFile());
		Process pMvn = pbMvn.start();
		int mvnExit = pMvn.waitFor();
		LOGGER.info("Maven exit code: "+mvnExit);
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		// processBuilder.command("bash", "-c", "ls /home/mkyong/");
		processBuilder.command(hugoCommand);
		processBuilder.directory(pWorkingDir.resolve("mycore.org").toFile());


			Process process = processBuilder.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line + "\n");
				}

				int exitVal = process.waitFor();
				LOGGER.info(exitVal);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOGGER.info(output);

	}

}
