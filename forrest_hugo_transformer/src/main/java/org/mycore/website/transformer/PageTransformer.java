package org.mycore.website.transformer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.yaml.snakeyaml.Yaml;

public class PageTransformer {
	
	private static Path P_INPUT = Transformer.BASE_DIR.resolve("mycore-documentation\\src\\documentation\\content\\xdocs");
	private static Path P_OUTPUT_DE = Transformer.BASE_DIR.resolve("mycore-website-hugo\\mycore.org\\content\\de");
	private static Path P_OUTPUT_EN = Transformer.BASE_DIR.resolve("mycore-website-hugo\\mycore.org\\content\\en");
	private static Path MENUE = Transformer.BASE_DIR.resolve("mycore-website-hugo\\mycore.org\\config\\_default\\menus.de.yaml");
	private static List<Path> IGNORE = Arrays.asList(Paths.get("index.de.xml"), Paths.get("index.en.xml"));
	private static List<String> IGNORE_REF = Arrays.asList("appdev_2_1", "howtoget", "docportal", "sessions_2_1", "version_2_2", "version_2_1", "news");
	
	private Map<String, Object> menueData;

	public static void main(String[] args) {
		PageTransformer pT = new PageTransformer();
		pT.run();
	}
	
	public PageTransformer() {
		try {
			Yaml yaml = new Yaml();
			menueData = yaml.load(Files.newBufferedReader(MENUE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		try {

			Files.walk(P_INPUT).filter(Files::isRegularFile).forEach(p -> execute(p));
			Files.walk(P_OUTPUT_DE).filter(Files::isDirectory).forEach(p -> createIndexMD(p));
			Files.walk(P_OUTPUT_EN).filter(Files::isDirectory).forEach(p -> createIndexMD(p));

			Files.walk(P_OUTPUT_DE).filter(Files::isRegularFile).forEach(p -> editHTML(p));
			Files.walk(P_OUTPUT_EN).filter(Files::isRegularFile).forEach(p -> editHTML(p));

			editMenueAndFilenames();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getYamlUrl(String id) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> entries = (List<Map<String, Object>>) menueData.get("main");
		for (Map<String, Object> map : entries) {
			if(id.equals(map.get("identifier").toString())) {
				return map.get("url").toString();
			}
		}
		return "";
	}

	private void editMenueAndFilenames() throws IOException {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> entries = (List<Map<String, Object>>) menueData.get("main");
		for (Map<String, Object> map : entries) {
			String id = map.get("identifier").toString();
			String url = map.get("url").toString();
			if (!url.startsWith("http://")) {
				Path pDe = P_OUTPUT_DE.resolve(url.substring(1) + ".html");
				if(pDe.getFileName().toString().contains("#")) {
					continue;
				}
				if (Files.exists(pDe)) {
					Files.move(pDe, pDe.getParent().resolve(id + ".html"));
				} else {
					System.out.println(pDe.getFileName());
				}
				Path pEn = P_OUTPUT_EN.resolve(url.substring(1) + ".html");
				if (Files.exists(pEn)) {
					Files.move(pEn, pEn.getParent().resolve(id + ".html"));
				}
				map.put("url", url.substring(0, url.lastIndexOf("/") + 1) + id);
			}
		}
		Yaml yaml = new Yaml();
		yaml.dump(menueData, Files.newBufferedWriter(MENUE));
	}

	private Object editHTML(Path p) {
		if (p.getFileName().toString().endsWith(".html")) {
			try {
				List<String> lines = Files.readAllLines(p);
				try (BufferedWriter writer = Files.newBufferedWriter(p)) {
					for (String s : lines) {
						s = s.replace("{{&lt;", "{{<").replace("&gt;}}", ">}}");
						writer.newLine();
						writer.write(s);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	private Object createIndexMD(Path p) {
		try {
			if (Files.exists(p.resolve("index.html"))) {
				Files.move(p.resolve("index.html"), p.resolve("_index.html"));
			} else {
				Files.write(p.resolve("_index.md"), "".getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object execute(Path p) {
		if (IGNORE.contains(P_INPUT.relativize(p))) {
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxx" + p.getFileName());
			return null;
		}
		if (p.getFileName().toString().endsWith(".xml")) {
			try {
				SAXBuilder sax = new SAXBuilder();
				sax.setValidation(false);
				sax.setFeature("http://xml.org/sax/features/validation", false);
				sax.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				Document doc = sax.build(p.toFile());

				Path pTarget = P_OUTPUT_DE.resolve(P_INPUT.relativize(p.getParent()))
						.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".de.", "."));
				if (p.getFileName().toString().contains(".en.")) {
					pTarget = P_OUTPUT_EN.resolve(P_INPUT.relativize(p.getParent()))
							.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".en.", "."));
				}

				Files.createDirectories(pTarget.getParent());
				try (BufferedWriter writer = Files.newBufferedWriter(pTarget)) {
					if (doc.getRootElement().getName().equals("document") == true) {
						createHeader(writer, doc.getRootElement().getChild("header"));
						Element outputE = doc.getRootElement().getChild("body");
						createBody(outputE, 1);
						XMLOutputter out = new XMLOutputter();
						out.output(outputE.getContent(), writer);
					}
				}
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void createBody(Element eSource, int level) {
		int newLevel = level;
		for (Content c : eSource.getContent()) {
			if (c.getCType() == Content.CType.Element) {
				Element e = (Element) c;
				e.setNamespace(Namespace.NO_NAMESPACE);
				if (e.getName().equals("section")) {
					e.setName("div");
					newLevel++;
				}
				if (e.getName().equals("title")) {
					e.setName("h" + Integer.toString(Math.min(newLevel, 6)));
				}
				if (e.getName().equals("a") && e.getAttribute("href") != null
						&& e.getAttributeValue("href").startsWith("site:")) {
					replaceSiteByRef(e);
				}
				createBody(e, newLevel);
			}
		}
	}

	private void replaceSiteByRef(Element e) {
		String ref = e.getAttributeValue("href").substring(5).replace(".", "_");
		if (ref.contains("/")) {
			ref = ref.substring(ref.lastIndexOf("/") + 1);
		}
		
		String url = getYamlUrl(ref);
		if(IGNORE_REF.contains(ref)) {
			e.setAttribute("href", ref + "#NOT_FOUND");
		} else if(url.startsWith("http:")) {
			e.setAttribute("href", url);
		} else if(url.contains("#")) {
			e.setAttribute("href", ref + "#TODO");
		} else {
			e.setAttribute("href", "{{< ref " + ref + " >}}");
		}
	}

	private void createHeader(BufferedWriter writer, Element e) {
		try {
			writer.append("---\n\n");
			if (e.getChild("title") != null) {
				writer.append("title: \"" + e.getChildText("title") + "\"");
			}
			if (e.getChild("release") != null) {
				String release = "";
				writer.append("\nmcr_versions: [");
				for (Element dateE : e.getChildren("release")) {
					release = release + "'" + dateE.getText() + "',";
				}
				release = release.substring(0, release.length() - 1) + "]";
				writer.append(release);
			}
			if (e.getChild("authors") != null) {
				String persons = "";
				writer.append("\nauthors: ");
				for (Element personE : e.getChildren("authors").get(0).getChildren("person")) {
					persons = persons + personE.getAttributeValue("name") + ", ";
				}
				persons = persons.substring(0, persons.length() - 2);
				writer.append(persons);
			}
			if (e.getChild("abstract") != null) {
				writer.append("\ndescription: \"" + e.getChildText("abstract").replace("\"", "'") + "\"");
			}
			if (e.getChild("version") != null) {
				writer.append("\ndate: \"" + e.getChildText("version") + "\"");
			}
			writer.append("\n\n---");

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
