---

title: 	'Webseiten-Migration: Offene Entscheidungen'
author: ['Robert Stephan']
date: 	'2019-03-10'

---
## Blog
### Hugo vs. Ghost
 - Wollen wir die jetzige Blog-Plattform behalten, oder Hugo auch für das Blog verwenden?
 - **Vorteil Hugo**:  
 	- alles aus "einem 'Guss" / nur ein System
 	- Kombination von News / Blog möglich
 - **Vorteil Ghost**: 
    - Online-Editor

## Hosting
### MyCoRe-Server *oder* Github-Pages
 - siehe [Hugo: Host on Github](https://gohugo.io/hosting-and-deployment/hosting-on-github/)
 - aber: Auch für das Github-Hosting müssen die Seiten erst gebaut und die generierten Seiten hochgeladen werden.
 - deshalb: Kann auch wie bisher die Seite auf dem MyCoRe-Server mit *Bamboo* gebaut und lokal (per Apache) bereitgestellt werden <br />   (Auschecken / `hugo`-Kommando ausführen / in `www`-Ordner kopieren)

## News
### Data *oder* Blog
   - News-Informationen zur Zeit als **JSON** in `/data/news.json`<br /> 
     (entspricht aktuellem Stand (News als XML))
   {{<highlight json>}}
{ "title":    "Neue Anwendung",
  "message": "Die Publikationsplattform der Max Weber Stiftung „perspectivia.net“ basiert nun auf MyCoRe.",
  "url":     "https://www.perspectivia.net/",
  "date":    "06. Dez 2018",
  "author":  "Wiebke Oeltjen",
   "image":  "sf" }
   {{</highlight>}}   
    
   - **Alternative:** Blogeinträge, getaggt mit "frontpage"
    - dadurch automatisch mehr Blogeinträge
    - alte Daten bleiben erhalten (News-Verlauf)
    - allerdings "Mehrarbeit" - zusätzlicher Text für kurzen Blogeintrag
    
### statisch (Hugo) oder dynamisch (Javascript)
   - zur Zeit beim Compilieren mit Hugo erzeugt
   - **Alternative:** Javascript ((Blog-Einträge oder Daten stellt Hugo per REST-API zur Verfügung)
      - Vorteil: Einträge können per Datumsparameter automatisch ein- und ausgeblendet werden <br />
        Solche "Spielereien" gingen auch statisch (Filter, ...). Dann müsste wir aber wengisten **nachts** die Seiten einmal neubauen
       