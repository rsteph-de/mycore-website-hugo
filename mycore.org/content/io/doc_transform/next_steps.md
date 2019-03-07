
---

title: "Webseiten-Migration: Nächste Schritte"
authors: 
date: "2019-03-07"

---
## Migrations(-Zeit-)plan
 - Was ist **vor**, **während** und **nach** der Migration zu tun?

 
## Technisches
- Umzug des Codes in Git von <code>rsteph-de</code> zu "MyCoRe-Org" 
	- Branch unter <code>documentation</code> oder eigenes Projekt?
- Automatisches Deployment nach mycore.de
	- Einrichtung einer Subdomain zum Testen: <code>wwwtest.mycore.de</code>
	- Bamboo-Konfiguration zum automatischen Deployment

## Was ist nach der Migration noch zu tun?
Zu einem Zeitpunkt <code>X</code> kann das Migrations-Skript letztmalig gestartet werden.
Dann beginnt die *Handarbeit* und es gibt kein zurück mehr.

- URLs mit "#" (TODO) reparieren
- Überschriften-Hierarchie h1, h2, h3, h4 prüfen und ggf. korrigieren
- Webseiten bereinigen / umstrukturieren
  - z.B. veraltete Informationen im 'Archiv' entfernen

## Was ist zu beachten / Wo sind noch Probleme?

### Probleme mit doppelten Anführungszeichen in HTML-Attributen und Hugo ShortCodes.

{{< highlight text "linenos=table">}}
<img src="{{</* urlRef "/images/_generated/ebers-pap.png" */>}}" title="Papyrus" />
{{< /highlight>}}
 
 - Problem: Hugo kann in Shortcodes nur doppelte Anführungszeichen verwenden. Das beißt sich mit den XML-Attributen.
 - Zur Dokumentation von Shortcodes siehe ([Hugo-Discourse](https://discourse.gohugo.io/t/how-is-the-hugo-doc-site-showing-shortcodes-in-code-blocks/9074/3))
 - Man könnte JDOM erweitern, dass es für die Attribute einfache Anführungszeichen verwendet.
([StackOverflow](https://stackoverflow.com/questions/18742412/save-xml-file-with-single-quotes-with-jdom))
Ob das sinnvoll ist, und ob wir das machen, muss ich mir erst nochmal gründlich überlegen. 
- Oder man verwendet den **[Hugo-Figure-Shortcode](https://gohugo.io/content-management/shortcodes/#figure)** bzw. schreibt etwas ähnliches selbst.


## Anzeige von Source-Code im XML-Format mit <code>&amp;lt;</code> und <code>&amp;gt;</code>
- Durch die JDOM-Ausgabe werden die XML-Tags encoded.
- Keine Idee, ob das lösbar ist.
- Man könnte versuchen, geparstes XML auszugeben.

## Javascript für TOC-Generierung ist buggy
- Alternative suchen oder selber neu schreiben
- mit JQuery lässt sich da einiges an Code bereinigen
- verschiedene Sonderfälle könnten ignoriert werden.


## CSS Fine-tuning
 - SCSS / Bootstrap Integration
 - Anpassen des Templates (Überschriften, Abstände, ...)
 
## Dokumentation der Dokumentation
 - Richtlinien zur Arbeit mit Hugo festlegen
 	- Sourcecode in HTML und/oder Markdown
 	- **Yaml** (kein Toml) für Konfigurationsdateien
 	- feste Page-Header-Variablen (Titel, Autor, Datum)
- verwendete und eigene **Shortcodes** für Images, Links, Codeblöcke beschreiben
- Inhaltsverzeichnisgenerierung ("no-TOC"-Klasse, ...)



