package org.mycore.website.transformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Transformer {
	public static Path BASE_DIR = Paths.get("C:\\workspaces\\mycore\\git");
	public static Path BASE_DIR_SOURCE =  BASE_DIR.resolve("mycore-documentation");
	public static Path BASE_DIR_TARGET =  BASE_DIR.resolve("mycore-website-hugo");
	
	public static Path P_INPUT = BASE_DIR_SOURCE.resolve("src\\documentation\\content\\xdocs");
	public static Path P_OUTPUT_DE = BASE_DIR_TARGET.resolve("mycore.org\\content\\de");
	public static Path P_OUTPUT_EN = BASE_DIR_TARGET.resolve("mycore.org\\content\\en");
	public static Path P_OUTPUT_IMAGES = BASE_DIR_TARGET.resolve("mycore.org\\static\\images\\_generated");
	public static Path P_MENUE = BASE_DIR_TARGET.resolve("mycore.org\\config\\_default\\menus.de.yaml");
	public static List<Path> IGNORE = Arrays.asList(Paths.get("index.de.xml"), Paths.get("index.en.xml"));
	public static List<String> IGNORE_REF = Arrays.asList("appdev_2_1", "howtoget", "docportal", "sessions_2_1", "version_2_2", "version_2_1", "news");

	
	public static void main(String[] args) {
		Transformer t = new Transformer();
		t.run();
	}

	private void run() {
		try {
			if(Files.exists(P_OUTPUT_DE)) {
				Files.walk(P_OUTPUT_DE)
				.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			if(Files.exists(P_OUTPUT_EN)) {
			Files.walk(P_OUTPUT_EN)
					.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			if(Files.exists(P_OUTPUT_IMAGES)) {
			Files.walk(P_OUTPUT_IMAGES)
					.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			
			
			MenueTransformer.main(new String[0]);
			PageTransformer.main(new String[0]);

			Files.copy(BASE_DIR_TARGET.resolve("mycore.org\\content\\io\\_source\\de._index.html"),
					BASE_DIR_TARGET.resolve("mycore.org\\content\\de\\_index.html"));
			
			Files.copy(BASE_DIR_TARGET.resolve("mycore.org\\content\\io\\_source\\en._index.html"),
					BASE_DIR_TARGET.resolve("mycore.org\\content\\en\\_index.html"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
