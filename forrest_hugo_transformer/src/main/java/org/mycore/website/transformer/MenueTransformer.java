package org.mycore.website.transformer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Hello world!
 *
 */
public class MenueTransformer 
{
	private static Path P_INPUT = Transformer.BASE_DIR.resolve("mycore-documentation\\src\\documentation\\content\\xdocs\\site.xml");
	private static Path P_OUTPUT = Transformer.BASE_DIR.resolve("mycore-website-hugo\\mycore.org\\config\\_default\\menus.de.yaml");
	
    public static void main( String[] args )
    {
    	MenueTransformer a = new MenueTransformer();
    	SAXBuilder sax = new SAXBuilder();
    	
    	int level = 0;
		try (BufferedWriter writer = Files.newBufferedWriter(P_OUTPUT)) {
			writer.append("main:\n");
			Document doc = sax.build(P_INPUT.toFile());
			Element eRoot = doc.getRootElement();
	    	a.run(writer, eRoot, level, "/");
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    private void run(BufferedWriter bw, Element el, int level, String urlPre) throws JDOMException, IOException {
    	level++;
		int weight = -100;
		
		for(Element e : el.getChildren()) {
			if(Arrays.asList("imprint", "contact", "privacy").contains(e.getName())) {
				continue;
			}
			bw.append("  -");
			for(int i = 0; i<level; i++) {
				bw.append("   ");
			}
			bw.append("{identifier: ");
			bw.append(e.getName().replace(".", "_"));
			
			if(!e.getParentElement().getName().equals("site")) {
				bw.append(", parent: ");
				bw.append(el.getName());
			}
			bw.append(", name: \"");
			bw.append(e.getAttributeValue("label"));
			bw.append("\", weight: ");
			weight = weight + 1;
			bw.append(Integer.toString(weight));
			bw.append(", url: \"");
			String url = "";
			if(!e.getParentElement().getName().equals(el) && el.getAttribute("href") != null) {
				if(e.getAttribute("href")!=null) {
					if(e.getAttributeValue("href").startsWith("http")) {
						url = e.getAttributeValue("href");
					} else {
						url = urlPre + e.getAttributeValue("href").replace(".html", "");
						if (!url.startsWith("/documentation") && !url.startsWith("/site")) {
							url = "/site" + url;
						}
					}
					
				}
				
				
				if(url.endsWith("/")) {
					bw.append(url.substring(0, url.length()-1));
				} else {
					bw.append(url);
				}
				
			}
			bw.append("\"}\n");
			if(e.getChildren()!=null) {
				run(bw, e, level, url);
			}
		}
    }
}
