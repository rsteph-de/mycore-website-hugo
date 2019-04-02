package org.mycore.website.hugo.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mycore.website.hugo.builder.util.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HugoBuilderService {

	public static Log LOGGER = LogFactory.getLog(HugoBuilderService.class);

	@Value("${mcr.hugo_builder.workingdir}")
	private String nameWorkingDir;
	private Path pWorkingDir;
	
	@Value("${mcr.hugo_builder.webdir}")
	private String nameWebDir;
	private Path pWebDir;

	@Value("${mcr.hugo_builder.git.download}")
	private String gitDownloadURL;
	
	@Value("${mcr.hugo_builder.projectdir.name}")
	private String projectdirName;

	@Value("${mcr.hugo_builder.hugo_cmd}")
	private String hugoCommand;

	@PostConstruct
	private void init() {
		pWorkingDir = Paths.get(nameWorkingDir);
		pWebDir = Paths.get(nameWebDir);
	}

	public synchronized void run() {
		try {
			if (Files.exists(pWorkingDir)) {
				Utilities.deleteDirectory(pWorkingDir);
			}
			Files.createDirectories(pWorkingDir);
		} catch (IOException e) {
			LOGGER.error("Error creating ddirectory: " + pWorkingDir);
		}
		gitCheckout();
		runHugo();
		publish();
	}

	private void gitCheckout() {
		try {
			URL gitURL = new URL(gitDownloadURL);

			try (ZipInputStream zipIn = new ZipInputStream(gitURL.openStream())) {
				ZipEntry entry = zipIn.getNextEntry();
				// iterates over entries in the zip file
				while (entry != null) {
					Path pTarget = pWorkingDir.resolve(entry.getName());
					if (!entry.isDirectory()) {
						Files.copy(zipIn, pTarget);
					} else {
						Files.createDirectories(pTarget);
					}
					zipIn.closeEntry();
					entry = zipIn.getNextEntry();
				}
			}
		} catch (IOException e) {
			LOGGER.error("Eror downloading and extracting " + gitDownloadURL, e);
		}

	}

	public void runHugo() {
		StringBuilder output = new StringBuilder("\n");
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			// processBuilder.command("bash", "-c", "ls /home/mkyong/");
			processBuilder.command(hugoCommand);
			processBuilder.directory(pWorkingDir.resolve(projectdirName).resolve("mycore.org").toFile());

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
	
	public void publish() {
		try {
		Utilities.deleteDirectory(pWebDir);
		Utilities.copyFolder(pWorkingDir.resolve(projectdirName).resolve("mycore.org").resolve("public"), pWebDir);
		}
		catch(IOException e) {
			LOGGER.error("Error copying web files", e);
		}
	}

}
