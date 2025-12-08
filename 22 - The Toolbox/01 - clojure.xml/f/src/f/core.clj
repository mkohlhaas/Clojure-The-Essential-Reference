(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.xml     :as xml])
  (:import
   [javax.xml.parsers SAXParserFactory]))

;; NOTE: Use [Clojure Data XML](https://github.com/clojure/data.xml)!

;; clojure.xml has been proposed for deprecation.
;; It was never refactored or re-designed beyond the first implementation.

(def document (xml/parse "https://www.welt.de/feeds/latest.rss"))

(count document) ; 3
(keys  document) ; (:tag :attrs :content)

;; skeleton data structure:
;; {:tag …
;;  :attrs …
;;  :content
;;  [{:tag … :attrs … :content […]}
;;   …
;;   {:tag …
;;    :attrs …
;;    :content
;;    [{:tag … :attrs … :content […]}
;;     …
;;     {:tag … :attrs … :content […]}]}]}

(comment
  (defn third [coll]
    (nth coll 2))

  (def contents1 (map :content (filter #(= :item (:tag %)) (:content (first (:content document))))))
  (count contents1) ; 30

  (def contents2 (map (juxt first third) (map #(filter :content %) contents1)))

  (def res (map (partial map :content) contents2))
  (count res) ; 30

  res)
  ; ((["„Ich hoffe, das Leben wird irgendwann plötzlich enden“"]
  ;   ["Im Kino ist Stellan Skarsgård eine Macht. Ohne ihn wären „Dune“, „Andor“ oder „Sentimental Value“ nur halb so gut. Jede Szene mit ihm ist eine Masterclass in Intensität und Bedrohlichkeit. Im Gespräch verrät er das Einzige, wovor er sich  fürchtet."])
  ;  (["Vom Hürdenlauf einer Branche"]
  ;   ["Die Offshore-Windkraft ist in vieler Hinsicht die erfolgreichste der erneuerbaren Energien – doch wieder einmal stockt der Ausbau in Deutschland erheblich. Und es bleibt offen, wie die Ziele der kommenden Jahre erreicht werden sollen."])
  ;  (["Starkes Erdbeben vor der Küste Japans – Behörden warnen vor Tsunami"]
  ;   ["Vor der Küste Japans kommt es zu einem schweren Erdbeben der Stärke 7,2. Die Behörden warnen vor bis zu drei Meter hohen Wellen. Betreiber der Atomkraftwerke der betroffenen Gegend führen Sicherheitsüberprüfungen durch."])
  ;  (["„Loyalitätsprobleme“ im Krisenfall – CDU-Politiker wollen Ende der doppelten Staatsbürgerschaft"]
  ;   ["Ein amerikanischer Senator will in den USA die doppelte Staatsbürgerschaft abschaffen. Für Deutschland könnten Teile dieses Plans eine Blaupause darstellen, finden Teile der Union."])
  ;  (["Forscher entdeckt eine der seltensten Pflanzen der Welt auf Picknickplatz"]
  ;   ["Bei einem Fototermin inmitten eines beliebten Ausflugsziels haben Forscher ein wahres Natur-Juwel entdeckt: Zwischen Laub und Wurzeln wächst eine ausgesprochen seltene Pflanze. Die Art zeichnet sich durch eine spezielle Ernährungsweise aus."])
  ;  (["Krippenspiel an der Volksbühne – man glaubt es kaum"]
  ;   ["Seit 800 Jahren wird im Abendland die Weihnachtsgeschichte nach Franz von Assisi nachgespielt. An Berlins Krawalltheater, der Volksbühne, wird die Tradition zu einer urkommunistischen Erfahrung. Brecht und Eisler sind als „Hyperchristen“ dabei – und ein Esel für die frohe Botschaft."])
  ;  (["„Tatsächlich gibt es für Vitamin C kaum Evidenz“"]
  ;   ["Von Nasensprays bis Honig: Nicht alles, was gut klingt, wirkt auch. Hausärztin, Kardiologe, HNO-Arzt und Lungenfacharzt – vier Mediziner verraten, was bei Erkältung, Grippe und Infekt wirklich hilft. Ein Tipp überrascht dabei besonders."])
  ;  (["„Sie erinnert mich an einen faulen Apfel“ – Trump attackiert Kollegin nach TV-Interview"]
  ;   ["Marjorie Taylor Greene galt lange als eine der glühendsten Unterstützerinnen von Donald Trump – bis zur Kontroverse um die Epstein-Akten. Nun sprach sie in einem Interview über parteiinterne Kritik am US-Präsidenten. Die Reaktion folgte prompt."])
  ;  (["„Werden nicht darum herumkommen, zwischen verschiedenen Berufsgruppen zu differenzieren“"]
  ;   ["Der „Renten-Rebell“ und CDU-Abgeordnete Nicklas Kappe erklärt, wie es nach dem zusammengebrochenen Aufstand der Jungen Gruppe in der Sozialpolitik weitergehen soll. Für ihn ist wichtig, dass bei den Altersbezügen künftig die „Lebensleistung“ stärker berücksichtigt wird."])
  ;  (["Personalbeben bei Red Bull"]
  ;   ["Nach dem verpassten WM-Titel stellt sich der Formel-1-Rennstall von Red Bull neu auf. Nach der Entlassung von Teamchef Christian Horner geht nun auch der zweite mächtige Funktionär im Team vorzeitig von Bord."])
  ;  (["Er hatte 28 Identitäten – Polizei nimmt abgelehnten Asylbewerber fest"]
  ;   ["Ohne gültigen Schengen-Aufenthaltstitel, mit Einreise- und Aufenthaltsverbot für Deutschland ist ein 37-jähriger Libanese in einem Zug in Aachen den Behörden ins Netz gegangen. Jetzt droht ihm die Abschiebung."])
  ;  (["Wer nie über Bärbel Bas gelacht hat, darf früher in Rente gehen"]
  ;   ["Dagegen kann die Mitgliedschaft in der Jungen Gruppe den Renteneintritt erheblich verzögern."])
  ;  (["Bei der Fußball-WM gibt es ein „Pride-Spiel“ – Ort und Paarung sind brisant"]
  ;   ["In den USA soll es bei der kommenden Fußball-Weltmeisterschaft ein „Pride-Spiel“ im Zeichen der örtlichen LGBTQ+-Community geben. Nach der Auslosung steht nun die Paarung fest. Sie könnte kaum brisanter sein."])
  ;  (["„Da steht zwar Frieden drauf, aber es  hat mit Frieden nichts zu tun“"]
  ;   ["Mit dem 28-Punkte-Plan soll der Krieg zwischen Russland und der Ukraine beendet werden. Die Ukraine käme jedoch deutlich schlechter davon als Russland. „Russland kommt stückweise voran, deshalb zeigt Putin keine Handlungsbereitschaft“, sagt Sicherheitsexperte Patrick Keller."])
  ;  (["Ein „Weiter so“ ist nicht hinnehmbar"]
  ;   ["Das Lagebild des Bundeskriminalamtes „Kriminalität im Kontext von Zuwanderung“ zeigt: Syrische und afghanische Tatverdächtige liegen bei Gewalt-, Sexual- und Drogendelikten deutlich über ihrem Anteil an der Gruppe der Geflüchteten. Es besteht dringender Handlungsbedarf."])
  ;  (["„Alarmglocken in Europa müssen jetzt ganz massiv schrillen“"]
  ;   ["Die neue US-Sicherheitsstrategie unter Präsident Trump stellt laut CDU-Politiker Röttgen eine zweite Zeitenwende für Europa dar. Über die aktuellen Entwicklungen berichtet US-Korrespondent Michael Wüllenweber."])
  ;  (["So plant Schalke den Umbau seines Stadions"]
  ;   ["Der FC Schalke ist wieder angesagt. Immer mehr Fans wollen den Tabellenführer der Zweiten Liga sehen. Dem will der Klub gerecht werden – mit einem Umbau der Arena. In den Überlegungen spielt auch der Bierkonsum der Anhänger eine wichtige Rolle."])
  ;  (["„Kein Anlass für den Verdacht, dass Frau Weidel rechtsextremistisch sein könnte“"]
  ;   ["AfD-Parteichefin Alice Weidel sorgt mit Nazi-Parolen und scharfer Kritik am Verfassungsschutz für neue Kontroversen. Trotz der getätigten Äußerungen sieht Staatsrechtler Volker Boehme-Neßler keinen Anlass, Weidel als rechtsextrem einzustufen."])
  ;  (["„Die, die darauf aufmerksam machen, werden schnell in die Rassistenecke gedrängt“"]
  ;   ["Der BKA-Bericht zur Kriminalstatistik zeigt, dass ausländische Straftäter im Gegensatz zu den deutschen deutlich überrepräsentiert sind. „Diese jungen Männer denken gar nicht daran, den Rechtsstaat zu achten“, sagt der Vorsitzende der Polizeigewerkschaft Rainer Wendt."])
  ;  (["„Irreversible Strukturschäden“ drohen – Uni fordert klare politische Entscheidungen"]
  ;   ["Die Universität Hamburg schlägt Alarm: Ohne schnelle Entscheidungen drohen bereits nächstes Jahr Einbußen in Lehre und Forschung. Die zuständige Wissenschaftsbehörde sieht sich in guten Gesprächen, die CDU drängt Bürgermeister Tschentscher auf einen klaren Kurs."])
  ;  (["„Islamistische Bedrohung war nie weg“ – Israelfeindliche Parolen auf Palästina-Demo"]
  ;   ["Bei einer Palästina-Demo in Berlin wurden erneut israelfeindliche und islamistische Parolen gerufen. Inzwischen hat sich auch Sinan Seelen, Präsident des Bundesamts für Verfassungsschutz, zu den Vorfällen geäußert."])
  ;  (["„Eine Schande für Deutschland“ – Islamisten laufen durch Berlin und rufen „Allahu akbar“"]
  ;   ["In Berlin haben Demonstranten am Samstag israelfeindliche und terrorverherrlichende Parolen gerufen."])
  ;  (["„Bin ich zu schwach?“ Gesa Krauses bitteres Marathon-Debüt"]
  ;   ["Es war der Start in eine neue sportliche Welt für Hindernisläuferin Gesa Krause – doch er misslang. Die 33-Jährige musste bei ihrem Marathon-Debüt aufgeben. Sehr ehrlich und ausführlich meldet sie sich nun zu Wort."])
  ;  (["Polizei verfolgt einen Wolf durch Lüdenscheids Innenstadt"]
  ;   ["In Lüdenscheid hat die Polizei einen Wolf in der Innenstadt gesichtet und versucht, das Tier mit einem Streifenwagen in ein Waldstück zu lenken. Ein Polizist filmte den ungewöhnlichen Einsatz."])
  ;  (["„Deswegen werfen die Ukrainer Reserven in diese Schlachten“"]
  ;   ["Präsident Selenskyj besucht London für Friedensgespräche. Der Kreml bleibt misstrauisch, auch wegen Trumps wechselhafter Haltung. An der Front greift die Ukraine derzeit aus einem bestimmten Grund auf Reserven zurück, wie Christoph Wanner berichtet."])
  ;  (["Die Oper, die ihren Komponisten fast den Kopf gekostet hätte"]
  ;   ["„Chaos statt Musik“, urteilte die russische Tageszeitung „Prawda“ zur Premiere der einzigen Oper von Dmitri Schostakowitsch. Zuvor hatte Stalin wutentbrannt die Vorstellung verlassen. Zum 50. Todestag eröffnet die Mailänder Scala ihre Saison mit „Lady Macbeth“ – inszeniert von Bayreuths großer „Ring“-Hoffnung."])
  ;  (["Studentin von „Hochschule for Palestine“ posiert mit Waffen-Attrappe an Universität"]
  ;   ["Schwere Vorwürfe gegen eine Studentin der Hochschule Darmstadt: Sie veröffentlichte von sich ein Foto mit einer Fake-Waffe in der Hand. Die Universität schaltete sofort die Ermittlungsbehörden ein. Die 23-Jährige gehört offenbar zu der Gruppe „Hochschule for Palestine“."])
  ;  (["„Es sind doch ein paar zarte Pflänzlein der Hoffnung“"]
  ;   ["Die deutsche Industrie zeigt leichte Erholung, doch Herausforderungen wie die Konkurrenz aus China und US-Zölle bleiben. Die Autoindustrie setzt auf E-Mobilität und Porsche kämpft mit seiner Strategie. „Die Wende ist noch nicht geschafft“, sagt Kapitalmarktstratege Stefan Riße bei Katja Losch."])
  ;  (["„Besorgniserregende Entwicklungen im Linksextremismus“, sagt der Verfassungsschutz-Präsident"]
  ;   ["Sinan Seelen warnt vor der Gleichzeitigkeit hybrider Bedrohungen, Extremismus sowie islamistischem Terror. Gleichzeitig stellt er sich vor sein Bundesamt: „Unsere Mitarbeiter verdienen Respekt und Anerkennung“, so der Verfassungsschutz-Präsident bei WELT TV."])
  ;  (["Mit einem Klick zu 5,5 Millionen – so gewann ein Hamburger beim Online-Lotto"]
  ;   ["Spiel 77 statt sechs Richtige: Ein Lottospieler aus Hamburger hat richtig viel Geld gewonnen. Dabei haben ihm allerdings nicht die klassischen Lottozahlen Glück gebracht – sondern ein Kreuz an anderer Stelle."]))

(def conforming-xhtml
  "<!DOCTYPE html SYSTEM 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>
    <html xmlns='http://www.w3.org/1999/xhtml'>
      <article>Hello</article>
    </html>")

;; parse XML as `input-stream` (with validation; network access to dtd file on the internet)
(-> conforming-xhtml
    .getBytes
    io/input-stream
    xml/parse)
; {:tag :html,
;  :attrs {:xmlns "http://www.w3.org/1999/xhtml"},
;  :content [{:tag :article, :attrs nil, :content ["Hello"]}]}

(defn non-validating [stream content-handler]
  (..
   (doto (SAXParserFactory/newInstance)
     (.setFeature "http://apache.org/xml/features/nonvalidating/load-external-dtd" false))
   (newSAXParser)
   (xml/parse stream content-handler)))

;; no network connection (conducive to testing)
(-> conforming-xhtml
    .getBytes
    io/input-stream
    (xml/parse non-validating))
; {:tag :html,
;  :attrs {:xmlns "http://www.w3.org/1999/xhtml"},
;  :content [{:tag :article, :attrs nil, :content ["Hello"]}]}

;; `xml/emit` reverses the process of `xml/parse`
(xml/emit conforming-xhtml)
; (out) <?xml version='1.0' encoding='UTF-8'?>
; (out) <!DOCTYPE html SYSTEM 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>
; (out)     <html xmlns='http://www.w3.org/1999/xhtml'>
; (out)       <article>Hello</article>
; (out)     </html>
