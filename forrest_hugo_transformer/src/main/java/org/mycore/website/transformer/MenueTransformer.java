package org.mycore.website.transformer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.yaml.snakeyaml.Yaml;

public class MenueTransformer {
	private static Path P_INPUT = Transformer.BASE_DIR
			.resolve("mycore-documentation\\src\\documentation\\content\\xdocs\\site.xml");
	private static Path P_OUTPUT = Transformer.BASE_DIR
			.resolve("mycore-website-hugo\\mycore.org\\config\\_default\\menus.de.yaml");

	public static void main(String[] args) {
		MenueTransformer a = new MenueTransformer();
		a.run();
	}

	private void run() {
		SAXBuilder sax = new SAXBuilder();
		Map<String, List<Map<String, Object>>> menue = new HashMap<String, List<Map<String, Object>>>();
		List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
		menue.put("main", entries);
		try (BufferedWriter writer = Files.newBufferedWriter(P_OUTPUT)) {
			Document doc = sax.build(P_INPUT.toFile());
			Element eRoot = doc.getRootElement();
			runEntries(entries, eRoot, "/");
			Yaml yaml = new Yaml();
			yaml.dump(menue, writer);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runEntries(List<Map<String, Object>> entries, Element el, String urlPre)
			throws JDOMException, IOException {
		int weight = -100;
		for (Element e : el.getChildren()) {
			Map<String, Object> entry = new HashMap<String, Object>();
			entry.put("identifier", e.getName().replace(".", "_"));
			if (!e.getParentElement().getName().equals("site")) {
				entry.put("parent", el.getName());
			}
			entry.put("name", e.getAttributeValue("label"));
			entry.put("weight", ++weight);

			String url = "";
			String href = e.getAttributeValue("href");
			if (href == null || href.equals("/../")) {
				e.setAttribute("href", "");
			}

			if (e.getAttributeValue("href").startsWith("http")) {
				url = e.getAttributeValue("href");
			} else {
				url = urlPre + e.getAttributeValue("href").replace(".html", "");
				if (!url.startsWith("/documentation") && !url.startsWith("/site")) {
					url = "/site" + url;
				}
			}

			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}

			entry.put("url", url);
			entries.add(entry);
			if (e.getChildren() != null) {
				runEntries(entries, e, url + "/");
			}
		}
	}
}
