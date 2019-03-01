package org.mycore.website.transformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Transformer {
	public static Path BASE_DIR = Paths.get("C:\\\\workspaces\\\\mycore\\\\git");
	
	public static void main(String[] args) {
		Transformer t = new Transformer();
		t.run();
	}

	private void run() {
		try {
			Files.walk(Paths.get("C:\\workspaces\\mycore\\git\\mycore-website-hugo\\mycore.org\\content\\de"))
					.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			Files.walk(Paths.get("C:\\workspaces\\mycore\\git\\mycore-website-hugo\\mycore.org\\content\\en"))
					.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

			MenueTransformer.main(new String[0]);
			PageTransformer.main(new String[0]);

			Files.copy(Paths
					.get("C:\\workspaces\\mycore\\git\\mycore-website-hugo\\mycore.org\\content\\de_alt\\_index.html"),
					Paths.get(
							"C:\\workspaces\\mycore\\git\\mycore-website-hugo\\mycore.org\\content\\de\\_index.html"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
