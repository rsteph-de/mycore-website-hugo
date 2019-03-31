package org.mycore.website.hugo.builder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utitlities {
	public static Log LOGGER = LogFactory.getLog(Utitlities.class);

	public static void deleteDirectory(Path p) throws IOException{

		  Files.walk(p)
	      .sorted(Comparator.reverseOrder())
	      .map(Path::toFile)
	      .forEach(File::delete);
	}
}
