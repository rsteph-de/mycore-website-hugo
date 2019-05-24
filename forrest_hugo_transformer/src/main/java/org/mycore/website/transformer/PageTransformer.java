package org.mycore.website.transformer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.XMLOutputter;
import org.yaml.snakeyaml.Yaml;

public class PageTransformer {

	private Map<String, Object> menueData;

	public static void main(String[] args) {
		PageTransformer pT = new PageTransformer();
		pT.run();
	}

	public PageTransformer() {
		try {
			Yaml yaml = new Yaml();
			menueData = yaml.load(Files.newBufferedReader(Transformer.P_MENUE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		try {

			Files.walk(Transformer.P_INPUT).filter(Files::isRegularFile).forEach(p -> execute(p));
			Files.walk(Transformer.P_OUTPUT_DE).filter(Files::isDirectory).forEach(p -> createIndexMD(p));
			Files.walk(Transformer.P_OUTPUT_EN).filter(Files::isDirectory).forEach(p -> createIndexMD(p));

			Files.walk(Transformer.P_OUTPUT_DE).filter(Files::isRegularFile).forEach(p -> editHTML(p));
			Files.walk(Transformer.P_OUTPUT_EN).filter(Files::isRegularFile).forEach(p -> editHTML(p));

			editMenueAndFilenames();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getYamlUrl(String id) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> entries = (List<Map<String, Object>>) menueData.get("main");
		for (Map<String, Object> map : entries) {
			if (id.equals(map.get("identifier").toString())) {
				return map.get("url").toString();
			}
		}
		return "";
	}

	private void editMenueAndFilenames() throws IOException {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> entries = (List<Map<String, Object>>) menueData.get("main");
		Iterator<Map<String, Object>> it = entries.iterator();
		while (it.hasNext()) {
			Map<String, Object> map = it.next();
			String id = map.get("identifier").toString();
			String url = map.get("url").toString();
			if (!url.startsWith("http://") && url.length() > 1) {
				Path pDe = Transformer.P_OUTPUT_DE.resolve(url.substring(1) + ".html");
				if (pDe.getFileName().toString().contains("#")) {
					continue;
				}
				if (Files.exists(pDe)) {
					Files.move(pDe, pDe.getParent().resolve(id + ".html"));
				} else {
					System.out.println(pDe.getFileName());
				}
				Path pEn = Transformer.P_OUTPUT_EN.resolve(url.substring(1) + ".html");
				if (Files.exists(pEn)) {
					Files.move(pEn, pEn.getParent().resolve(id + ".html"));
				}
				map.put("url", url.substring(0, url.lastIndexOf("/") + 1) + id);
				if (map.get("url").equals("/site/welcome")) {
					map.put("url", "/");
				}

			}
			if (Arrays.asList("imprint", "contact", "privacy").contains(map.get("identifier"))) {
				it.remove();
			}
		}
		Yaml yaml = new Yaml();
		yaml.dump(menueData, Files.newBufferedWriter(Transformer.P_MENUE));
	}

	private Object editHTML(Path p) {
		if (p.getFileName().toString().endsWith(".html")) {
			try {
				List<String> lines = Files.readAllLines(p);
				try (BufferedWriter writer = Files.newBufferedWriter(p)) {
					for (String s : lines) {
						s = s.replace("{{&lt;", "{{<").replace("&gt;}}", ">}}");
						s = s.replaceAll("<img src=\"(/images/_generated/.*?)\\s*\"",
								"<img src='{{< relURL \"$1\" >}}'");

						// replaces refs, because files do not yet exist in english pages
						if (p.toString().replace("//", "\"").contains("\\en\\")) {
							s = s.replace("{{< ref mir >}}", "{{***TODO*** ref mir ***}}");
							s = s.replace("{{< ref map >}}", "{{***TODO*** ref map ***}}");
						}
						if(s.contains("Ω")) {
							s = s.replace("Ω",  "").replace("&gt;", ">").replace("&lt;", "<").replace("&nbsp;", " ").replaceAll("&amp;", "&");
						}

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
				if (Arrays.asList("overview", "contact").contains(p.getFileName().toString())) {
					Files.move(p.resolve("index.html"), p.resolve(p.getFileName().toString() + ".html"));
				} else {
					Files.move(p.resolve("index.html"), p.resolve("_index.html"));
				}

			} else {
				if (!Arrays.asList("de", "en").contains(p.getFileName().toString())) {
					Files.write(p.resolve("_index.md"), "".getBytes());
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object execute(Path p) {
		if (Transformer.IGNORE.contains(Transformer.P_INPUT.relativize(p))) {
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxx" + p.getFileName());
			return null;
		}
		if (p.getFileName().toString().endsWith(".xml")) {
			try {
				SAXBuilder sax = new SAXBuilder();
				sax.setXMLReaderFactory(XMLReaders.NONVALIDATING);
				sax.setFeature("http://xml.org/sax/features/validation", false);
				sax.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				Document doc = sax.build(p.toFile());
				Path targetSubPath = Transformer.P_INPUT.relativize(p.getParent());
				Path pTarget = Transformer.P_OUTPUT_DE.resolve(targetSubPath)
						.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".de.", "."));
				if (p.getFileName().toString().contains(".en.")) {
					pTarget = Transformer.P_OUTPUT_EN.resolve(targetSubPath)
							.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".en.", "."));
				}

				if (!targetSubPath.toString().startsWith("documentation")) {
					pTarget = Transformer.P_OUTPUT_DE.resolve("site").resolve(targetSubPath)
							.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".de.", "."));
					if (p.getFileName().toString().contains(".en.")) {
						pTarget = Transformer.P_OUTPUT_EN.resolve("site").resolve(targetSubPath)
								.resolve(p.getFileName().toString().replace(".xml", ".html").replace(".en.", "."));
					}
				}

				Files.createDirectories(pTarget.getParent());
				try (BufferedWriter writer = Files.newBufferedWriter(pTarget)) {
					if (doc.getRootElement().getName().equals("document") == true) {
						createHeader(writer, doc.getRootElement().getChild("header"));
						Element outputE = doc.getRootElement().getChild("body");
						createBody(outputE, new AtomicInteger(1), targetSubPath);
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

	private void createBody(Element eSource, AtomicInteger level, Path targetSubPath) {
		for (int i = 0; i < eSource.getContent().size(); i++) {
			Content c = eSource.getContent(i);
			if (c.getCType() == Content.CType.Element) {
				Element e = (Element) c;
				e.setNamespace(Namespace.NO_NAMESPACE);
				String oldName = e.getName();
				if (oldName.equals("section")) {
					e.setName("div");
					level.incrementAndGet();
				}
				if (e.getName().equals("title")) {
					e.setName("h" + Integer.toString(Math.min(level.get(), 6)));
				}
				if (e.getName().equals("a") && e.getAttribute("href") != null
						&& e.getAttributeValue("href").startsWith("site:")) {
					replaceSiteByRef(e);
				}
				
				if (e.getName().equals("pre") || e.getName().equals("source")) {
					Element preParentE = e.getParentElement();
					String lang = "text";
					if (e.getAttribute("class") != null) {
						lang = e.getAttributeValue("class").replace("brush:", "").replaceAll("\\s*gutter:\\s*false", "")
								.replace(";", "").trim();
						if(e.getAttributeValue("class").equals("code")) {
							lang = "xml";
						}
					}
					preParentE.addContent(preParentE.getContent().indexOf(e),
							new Text("{{< highlight " + lang + " \"linenos=table\">}}"));
					while (e.getContent().size() > 0) {
						Content c2 = e.getContent(0);
						if (c2 instanceof CDATA) {
							CDATA cdata = (CDATA) c2.detach();
							String text = "Ω" + cdata.getText().replace("\n", "\nΩ");
							preParentE.addContent(preParentE.getContent().indexOf(e), new Text(text));
						} else {
							preParentE.addContent("Ω").addContent(preParentE.getContent().indexOf(e), c2.detach());
						}

					}
					preParentE.addContent(preParentE.getContent().indexOf(e), new Text("{{< /highlight >}}"));
					preParentE.removeContent(e);
				}

				if (e.getName().equals("div") && (e.getChild("img") != null || (e.getChild("center") != null && e.getChild("center") != null))) {
					createFigure(e, targetSubPath);
					continue;
				}

				if (e.getName().equals("img")) {
					String src = copyImage(e.getAttributeValue("src"), targetSubPath);
					if(src != null) {
						e.setAttribute("src", src);
					}
				}
				createBody(e, level, targetSubPath);
				if (e.getName().equals("span") && e.getAttribute("class") != null
						&& e.getAttributeValue("class").equals("klein")) {
					e.setAttribute("class", "small");
				}
				if (oldName.equals("section")) {
					level.decrementAndGet();
				}

			}
		}
	}

	/**
	 * {{< mcr-figure src="" caption="" count"" alt="" width="" >}}
	 * 
	 * @param e
	 * @param targetSubPath
	 */
	private void createFigure(Element e, Path targetSubPath) {
		String src="";
		Element eImg = e.getChild("img");
		if(eImg == null) {
			eImg = e.getChild("center").getChild("img");
		}
		src = copyImage(eImg.getAttributeValue("src"), targetSubPath);
		
		String caption = "";
		String label = "";
		String alt = "";
		String width = "";
		String height= "";
		e.removeAttribute("style");
		if (e.getChildText("span") != null) {
			caption = e.getChildTextNormalize("span");
			if (e.getChild("span").getChildText("strong") != null) {
				label = e.getChild("span").getChildTextNormalize("strong");
				if(label.endsWith(":")) {
					label = label.substring(0, label.length()-1).trim();
				}
			}
		}
		
		if (eImg.getAttributeValue("alt") != null) {
			alt = eImg.getAttributeValue("alt").trim();
		}
		if (eImg.getAttributeValue("width") != null) {
			width = eImg.getAttributeValue("width").trim();
		}
		if (eImg.getAttributeValue("height") != null) {
			height = eImg.getAttributeValue("height").trim();
		}
		String figure = "{{< mcr-figure src=\"" + src + "\" class=\"border border-secondary" + "\" label=\"" + label + "\" caption=\"" + caption  +"\" alt=\"" + alt + "\" width=\"" + width + "\" height=\"" + height + "\" />}}";
		e.setContent(new Text(figure));
	}

	private String copyImage(String src, Path targetSubPath) {
		Path pImageOld = Transformer.BASE_DIR_SOURCE.resolve("src\\documentation\\content\\xdocs");
		Path pImageOld2 = Transformer.BASE_DIR_SOURCE.resolve("src\\documentation\\resources");

		String imageFolder = "";
		if (!targetSubPath.toString().isEmpty()) {
			String[] targetSubPathSplit = targetSubPath.toString().split("\\\\");
			if (targetSubPathSplit.length > 1) {
				imageFolder = targetSubPathSplit[0] + "\\" + targetSubPathSplit[1] + "\\";
			} else if (targetSubPathSplit.length == 0) {
				imageFolder = targetSubPathSplit[0] + "\\";
			}
			if (!targetSubPath.toString().startsWith("documentation")) {
				imageFolder = "site\\" + targetSubPathSplit[0] + "\\";
			}
		}
		imageFolder = imageFolder.replace("\\", "/");

		try {
			Files.createDirectories(Transformer.P_OUTPUT_IMAGES.resolve(imageFolder));
			
			if (src.startsWith("/")) {
				src = src.substring(1);
			}
			String filename = src;
			if (filename.contains("/")) {
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			}

			if (Files.exists(pImageOld.resolve(src))) {
				Files.copy(pImageOld.resolve(src),
						Transformer.P_OUTPUT_IMAGES.resolve(imageFolder + filename),
						StandardCopyOption.REPLACE_EXISTING);
				return "/images/" + imageFolder + filename;

			}
			if (Files.exists(pImageOld2.resolve(src))) {
				Files.copy(pImageOld2.resolve(src),
						Transformer.P_OUTPUT_IMAGES.resolve(imageFolder + filename),
						StandardCopyOption.REPLACE_EXISTING);
				return "/images/" + imageFolder + filename;
			}
			
			System.err.println("Bild nicht gefunden! " + src);
			

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private void replaceSiteByRef(Element e) {
		String ref = e.getAttributeValue("href").substring(5).replace(".", "_");
		if (ref.contains("/")) {
			ref = ref.substring(ref.lastIndexOf("/") + 1);
		}

		String url = getYamlUrl(ref);
		if (Transformer.IGNORE_REF.contains(ref)) {
			e.setAttribute("href", ref + "#NOT_FOUND");
		} else if (url.startsWith("http:")) {
			e.setAttribute("href", url);
		} else if (url.contains("#")) {
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
				writer.append("\nmcr_version: [");
				for (Element dateE : e.getChildren("release")) {
					release = release + "'" + dateE.getText() + "',";
				}
				release = release.substring(0, release.length() - 1) + "]";
				writer.append(release);
			}
			if (e.getChild("authors") != null) {
				String persons = "";
				writer.append("\nauthor: ['");
				for (Element personE : e.getChildren("authors").get(0).getChildren("person")) {
					persons = persons + personE.getAttributeValue("name") + "', '";
				}
				persons = persons.substring(0, persons.length() - 3) + "]";
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
