package org.mycore.website.transformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Transformer {
	public static Path BASE_DIR = Paths.get("C:\\workspaces\\mycore-hugo\\git");
	public static Path BASE_DIR_SOURCE = BASE_DIR.resolve("documentation-forrest");
	public static Path BASE_DIR_TARGET = BASE_DIR.resolve("rsteph-mycore-website");

	public static Path P_INPUT, P_OUTPUT_DE,P_OUTPUT_EN,P_OUTPUT_IMAGES,P_MENUE;
	public static List<Path> IGNORE = Arrays.asList(Paths.get("index.de.xml"), Paths.get("index.en.xml"));
	public static List<String> IGNORE_REF = Arrays.asList("appdev_2_1", "howtoget", "docportal", "sessions_2_1",
			"version_2_2", "version_2_1", "news");

	public static void main(String[] args) {
		if (args.length == 2) {
			BASE_DIR_SOURCE = Paths.get(args[0]);
			BASE_DIR_TARGET = Paths.get(args[1]);
		} else {
			System.out.println(
					"You can run the transformer with 2 parameters: java -jar mycore-forrest2hugo.jar <forrest-base-dir> <hugo-base-dir>");
		}
		
		P_INPUT = BASE_DIR_SOURCE.resolve("src\\documentation\\content\\xdocs");
		P_OUTPUT_DE = BASE_DIR_TARGET.resolve("mycore.org\\content\\de");
		P_OUTPUT_EN = BASE_DIR_TARGET.resolve("mycore.org\\content\\en");
		P_OUTPUT_IMAGES = BASE_DIR_TARGET.resolve("mycore.org\\static\\images");
		P_MENUE = BASE_DIR_TARGET.resolve("mycore.org\\config\\_default\\menus.de.yaml");

		Transformer t = new Transformer();
		t.run();
	}

	private void run() {
		try {
			if (Files.exists(P_OUTPUT_DE)) {
				Files.walk(P_OUTPUT_DE).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			if (Files.exists(P_OUTPUT_EN)) {
				Files.walk(P_OUTPUT_EN).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			if (Files.exists(P_OUTPUT_IMAGES)) {
				Files.walk(P_OUTPUT_IMAGES).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}

			MenueTransformer.main(new String[0]);
			PageTransformer.main(new String[0]);

			Files.copy(BASE_DIR_TARGET.resolve("mycore.org\\content\\io\\_source\\de._index.html"),
					BASE_DIR_TARGET.resolve("mycore.org\\content\\de\\_index.html"));

			Files.copy(BASE_DIR_TARGET.resolve("mycore.org\\content\\io\\_source\\en._index.html"),
					BASE_DIR_TARGET.resolve("mycore.org\\content\\en\\_index.html"));
			
			Path blogDir = BASE_DIR_TARGET.resolve("mycore.org\\content\\io\\blog");
			Files.copy(blogDir,	BASE_DIR_TARGET.resolve("mycore.org\\content\\de\\blog"));
			 try (DirectoryStream<Path> stream = Files.newDirectoryStream(blogDir)) {
			       for (Path entry: stream) {
			    	   Files.copy(entry, BASE_DIR_TARGET.resolve("mycore.org\\content\\de\\blog").resolve(entry.getFileName()));
			       }
			   }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
