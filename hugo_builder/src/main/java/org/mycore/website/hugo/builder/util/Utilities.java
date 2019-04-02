package org.mycore.website.hugo.builder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utilities {
	public static Log LOGGER = LogFactory.getLog(Utilities.class);

	public static void deleteDirectory(Path p) throws IOException {
		if(Files.exists(p)) {
			Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
	}

	public static void copyFolder(Path src, Path dest) throws IOException {
		Files.createDirectories(dest);
		Files.walk(src).forEach(source -> {
			try {
				Files.copy(source, dest.resolve(src.relativize(source)), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}
}
