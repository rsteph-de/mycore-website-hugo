package org.mycore.website.hugo.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mycore.website.hugo.builder.model.Status;
import org.mycore.website.hugo.builder.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HugoBuilderService {

	public static Log LOGGER = LogFactory.getLog(HugoBuilderService.class);
	
	@Autowired
	HugoBuilderDataStore builderData;

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
	
	@Value("${mcr.hugo_builder.hugo.baseurl}")
	private String hugoBaseURL;

	@PostConstruct
	private void init() {
		pWorkingDir = Paths.get(nameWorkingDir);
		pWebDir = Paths.get(nameWebDir);
	}

	public synchronized Status run() {
		Status status = new Status();
		try {
			if (Files.exists(pWorkingDir)) {
				Utilities.deleteDirectory(pWorkingDir);
			}
			Files.createDirectories(pWorkingDir);
		} catch (IOException e) {
			LOGGER.error("Error creating directory: " + pWorkingDir, e);
			status.getErrors().add("Error creating directory: " + pWorkingDir + "\n" + e.getMessage());
			status.setSuccessful(false);
		}
		gitUnzip(status);
		runHugo(status);
		publish(status);
		status.setCompleted(ZonedDateTime.now().toString());
		builderData.pushStatus(status);
		
		return status;
	}

	private void gitUnzip(Status status) {
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
			status.getErrors().add("Eror downloading and extracting " + gitDownloadURL + "\n" + e.getMessage());
			status.setSuccessful(false);
		}
	}

	private void runHugo(Status status) {
		StringBuilder output = new StringBuilder("\n");
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(hugoCommand, "-b", hugoBaseURL);
			processBuilder.directory(pWorkingDir.resolve(projectdirName).resolve("mycore.org").toFile());
			Process process = processBuilder.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line + "\n");
				}
				int exitVal = process.waitFor();
				status.setExitCode(exitVal);
				LOGGER.info(exitVal);
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error running Hugo " + e);
			status.getErrors().add("Error running Hugo \n" + e.getMessage());
			status.setSuccessful(false);
		}
		LOGGER.info(output);
		status.getOutputs().addAll(Arrays.asList((output.toString().split("\n"))));
	}
	
	private void publish(Status status) {
		try {
		Utilities.deleteDirectory(pWebDir);
		Utilities.copyFolder(pWorkingDir.resolve(projectdirName).resolve("mycore.org").resolve("public"), pWebDir);
		}
		catch(IOException e) {
			LOGGER.error("Error copying web files", e);
			status.getErrors().add("Error copying web files \n" + e.getMessage());
			status.setSuccessful(false);
		}
	}
}
