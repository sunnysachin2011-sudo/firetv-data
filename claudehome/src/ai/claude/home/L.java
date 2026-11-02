package ai.claude.home;

import java.util.*;

/** In-app language switch: English, Hindi, German. Team names and app names stay as they are. */
public class L {
    static String lang = "en";   // "en", "hi", "de"
    static final Map<String, String[]> T = new HashMap<>();
    static void p(String k, String en, String hi, String de) { T.put(k, new String[]{ en, hi, de }); }

    static {
        p("tab.foryou", "For you", "आपके लिए", "Für dich");
        p("tab.sport", "Sport", "खेल", "Sport");
        p("tab.news", "News", "समाचार", "Nachrichten");
        p("tab.apps", "Apps", "ऐप्स", "Apps");
        p("greet.m", "Good morning", "सुप्रभात", "Guten Morgen");
        p("greet.a", "Good afternoon", "नमस्कार", "Guten Tag");
        p("greet.e", "Good evening", "शुभ संध्या", "Guten Abend");
        p("cricket", "Cricket", "क्रिकेट", "Cricket");
        p("football", "Football", "फ़ुटबॉल", "Fußball");
        p("livenow", "Live now", "अभी लाइव", "Jetzt live");
        p("live", "LIVE", "लाइव", "LIVE");
        p("today", "Today", "आज", "Heute");
        p("ends", "ends ~%s", "~%s बजे तक", "Ende ~%s");
        p("endsin", "ends in ~%d min", "~%d मिनट बाकी", "Ende in ~%d Min.");
        p("startsmin", "starts in %d min", "%d मिनट में शुरू", "Beginn in %d Min.");
        p("startsh", "starts in %d h %d min", "%d घंटे %d मिनट में शुरू", "Beginn in %d Std. %d Min.");
        p("finished", "finished", "समाप्त", "beendet");
        p("notapps", "Not on your apps", "आपके ऐप्स पर नहीं", "Nicht in deinen Apps");
        p("notinstalled", "%s is not installed", "%s इंस्टॉल नहीं है", "%s ist nicht installiert");
        p("openlounge", "Open the Lounge", "लाउंज खोलें", "Lounge öffnen");
        p("open", "Open", "खोलें", "Öffnen");
        p("askabout", "Ask Claude about it", "Claude से इसके बारे में पूछें", "Claude dazu fragen");
        p("askany", "Ask Claude anything", "Claude से कुछ भी पूछें", "Frag Claude alles");
        p("asksub", "On the big screen, from your sofa.", "बड़ी स्क्रीन पर, सोफ़े से।", "Auf dem großen Bildschirm, vom Sofa aus.");
        p("askshort", "Ask anything", "कुछ भी पूछें", "Frag alles");
        p("lounge", "Claude Lounge", "Claude लाउंज", "Claude Lounge");
        p("sporttoday", "Sport today", "आज के मैच", "Sport heute");
        p("sportfrom", "Sport  (list from %s)", "खेल  (%s की सूची)", "Sport  (Liste vom %s)");
        p("nomatches", "No matches today", "आज कोई मैच नहीं", "Heute keine Spiele");
        p("nolist", "No list for today yet", "आज की सूची अभी नहीं आई", "Noch keine Liste für heute");
        p("nosport", "Sport list not available", "खेल सूची उपलब्ध नहीं", "Sportliste nicht verfügbar");
        p("sportnote", "Football and men's cricket. New list every morning at 06:46.", "फ़ुटबॉल और पुरुष क्रिकेट। हर सुबह 06:46 पर नई सूची।", "Fußball und Herren-Cricket. Jeden Morgen um 06:46 eine neue Liste.");
        p("alertlive", "%s is live on %s", "%s अभी %s पर लाइव", "%s läuft jetzt auf %s");
        p("alertsoon", "%s starts in %d min on %s", "%s %d मिनट में %s पर", "%s beginnt in %d Min. auf %s");
        p("oktoopen", "OK to open", "खोलने के लिए OK", "OK zum Öffnen");
        p("topstories", "Top stories", "मुख्य समाचार", "Top-Nachrichten");
        p("from", "from %s", "%s से", "vom %s");
        p("nostories", "No stories yet", "अभी कोई समाचार नहीं", "Noch keine Nachrichten");
        p("ingw", "India, Germany, World", "भारत, जर्मनी, दुनिया", "Indien, Deutschland, Welt");
        p("India", "India", "भारत", "Indien");
        p("Germany", "Germany", "जर्मनी", "Deutschland");
        p("World", "World", "दुनिया", "Welt");
        p("newsnote", "New stories every morning at 06:46. OK on a story opens the Lounge to ask about it.", "हर सुबह 06:46 पर नए समाचार। किसी समाचार पर OK दबाएँ, लाउंज में उसके बारे में पूछें।", "Jeden Morgen um 06:46 neue Nachrichten. OK auf einer Meldung öffnet die Lounge für Fragen.");
        p("cologne", "Cologne", "कोलोन", "Köln");
        p("w.clear", "Clear", "साफ़", "Klar");
        p("w.partly", "Partly cloudy", "आंशिक बादल", "Teils bewölkt");
        p("w.cloudy", "Cloudy", "बादल", "Bewölkt");
        p("w.fog", "Fog", "कोहरा", "Nebel");
        p("w.drizzle", "Drizzle", "बूंदाबांदी", "Nieselregen");
        p("w.rain", "Rain", "बारिश", "Regen");
        p("w.snow", "Snow", "बर्फ़", "Schnee");
        p("w.showers", "Showers", "बौछारें", "Schauer");
        p("w.snowshowers", "Snow showers", "हिमपात", "Schneeschauer");
        p("w.thunder", "Thunder", "आंधी-तूफ़ान", "Gewitter");
        p("s.photo", "Photo of the day", "आज की फ़ोटो", "Foto des Tages");
        p("s.dark", "Dark (no photo)", "गहरा (फ़ोटो नहीं)", "Dunkel (kein Foto)");
        p("s.idle5", "Idle screen after 5 min", "5 मिनट बाद आराम स्क्रीन", "Ruhebildschirm nach 5 Min.");
        p("s.idle15", "Idle screen after 15 min", "15 मिनट बाद आराम स्क्रीन", "Ruhebildschirm nach 15 Min.");
        p("s.idleoff", "No idle screen", "आराम स्क्रीन बंद", "Kein Ruhebildschirm");
        p("s.close", "Close", "बंद करें", "Schließen");
        p("firetvhome", "Fire TV Home", "Fire TV होम", "Fire TV Start");
        p("cannotopen", "Cannot open %s", "%s नहीं खुल सका", "%s kann nicht geöffnet werden");
        p("nothingyet", "Nothing to open yet", "अभी खोलने को कुछ नहीं", "Noch nichts zum Öffnen");
        p("s.title", "Settings", "सेटिंग्स", "Einstellungen");
        p("s.lang", "Language", "भाषा", "Sprache");
        p("s.wall", "Background", "पृष्ठभूमि", "Hintergrund");
        p("s.idle", "Idle screen", "आराम स्क्रीन", "Ruhebildschirm");
        p("s.photoshort", "Photo of the day", "आज की फ़ोटो", "Foto des Tages");
        p("s.darkshort", "Dark", "गहरा", "Dunkel");
        p("s.min", "%d min", "%d मिनट", "%d Min.");
        p("s.off", "Off", "बंद", "Aus");
        p("s.hint", "Back closes  ·  OK chooses", "Back से बंद  ·  OK से चुनें", "Zurück schließt  ·  OK wählt");
        p("vs", "v", "बनाम", "–");
        p("f.vpn", "VPN location", "VPN स्थान", "VPN-Standort");
        p("f.fix", "Fix the app", "ऐप ठीक करें", "App reparieren");
        p("f.restart", "Restart app", "ऐप फिर से चलाएँ", "App neu starten");
        p("f.cache", "Clear cache", "कैश साफ़ करें", "Cache leeren");
        p("f.open", "Open", "खोलें", "Öffnen");
        p("f.sub", "Blocked or not loading? Switch the city, then restart the app.", "ब्लॉक है या नहीं चल रहा? शहर बदलें, फिर ऐप दोबारा चलाएँ।", "Gesperrt oder lädt nicht? Stadt wechseln, dann App neu starten.");
        p("f.hint", "Hold OK on a VPN app to open this  \u00b7  Back closes", "VPN \u0910\u092a \u092a\u0930 OK \u0926\u092c\u093e\u0915\u0930 \u0930\u0916\u0947\u0902  \u00b7  Back \u0938\u0947 \u092c\u0902\u0926 \u0915\u0930\u0947\u0902", "OK auf einer VPN-App halten  \u00b7  Zur\u00fcck schlie\u00dft");
        p("f.vpntitle", "VPN to India", "\u092d\u093e\u0930\u0924 \u0915\u0947 \u0932\u093f\u090f VPN", "VPN nach Indien");
        p("f.switched", "VPN: %s", "VPN: %s", "VPN: %s");
        p("f.cachehint", "Press OK on \"Clear cache\", then Back", "\"Clear cache\" पर OK दबाएँ, फिर Back", "OK auf \"Cache leeren\", dann Zurück");
        p("Mumbai", "Mumbai", "मुंबई", "Mumbai");
        p("Delhi", "Delhi", "दिल्ली", "Delhi");
        p("tonight", "Tonight", "आज रात", "Heute Abend");
        // picture setup (1.4)
        p("s.pic", "Picture setup", "पिक्चर सेटअप", "Bildeinstellung");
        p("s.picstart", "Start setup", "सेटअप शुरू करें", "Einrichtung starten");
        p("s.picpat", "Test patterns", "टेस्ट पैटर्न", "Testbilder");
        p("pic.step", "Step %d of %d", "चरण %d / %d", "Schritt %d von %d");
        p("pic.hintw", "OK  next   ·   ◀  back   ·   ▲  hide text   ·   Back  exit", "OK  आगे   ·   ◀  पीछे   ·   ▲  टेक्स्ट छिपाएँ   ·   Back  बाहर", "OK  weiter   ·   ◀  zurück   ·   ▲  Text ausblenden   ·   Zurück  Ende");
        p("pic.hintp", "◀ ▶  pattern   ·   OK  hide text   ·   Back  exit", "◀ ▶  पैटर्न   ·   OK  टेक्स्ट छिपाएँ   ·   Back  बाहर", "◀ ▶  Muster   ·   OK  Text ausblenden   ·   Zurück  Ende");
        p("w.mode.t", "Choose the movie picture mode", "मूवी पिक्चर मोड चुनें", "Filmmodus wählen");
        p("w.mode.b", "On the TV remote, open the Picture settings. Select Movie, Cinema or Filmmaker mode. Set Motion smoothing, Dynamic contrast, Noise reduction and Energy saving to Off.", "TV रिमोट से Picture सेटिंग्स खोलें। Movie, Cinema या Filmmaker मोड चुनें। Motion smoothing, Dynamic contrast, Noise reduction और Energy saving को Off करें।", "Öffne mit der TV-Fernbedienung die Bildeinstellungen. Wähle Film, Kino oder Filmmaker-Modus. Schalte Bewegungsglättung, dynamischen Kontrast, Rauschunterdrückung und Energiesparen aus.");
        p("w.bright.t", "Set Brightness", "Brightness सेट करें", "Helligkeit einstellen");
        p("w.bright.b", "Decrease Brightness until bar 1 is not visible. Bar 2 must stay just visible. Use the room light that you usually have.", "Brightness तब तक कम करें जब तक बार 1 न दिखे। बार 2 हल्का सा दिखना चाहिए। कमरे की सामान्य रोशनी में यह करें।", "Verringere die Helligkeit, bis Balken 1 nicht mehr sichtbar ist. Balken 2 muss gerade noch sichtbar sein. Nutze das übliche Raumlicht.");
        p("w.contrast.t", "Set Contrast", "Contrast सेट करें", "Kontrast einstellen");
        p("w.contrast.b", "Increase Contrast until the boxes on the right merge into one white area. Then decrease Contrast until you can see all 6 boxes.", "Contrast तब तक बढ़ाएँ जब तक दाएँ के बॉक्स एक सफ़ेद हिस्सा न बन जाएँ। फिर Contrast कम करें जब तक सभी 6 बॉक्स न दिखें।", "Erhöhe den Kontrast, bis die rechten Felder zu einer weißen Fläche werden. Verringere ihn dann, bis du alle 6 Felder siehst.");
        p("w.sharp.t", "Set Sharpness", "Sharpness सेट करें", "Schärfe einstellen");
        p("w.sharp.b", "Decrease Sharpness until the black lines have no bright edge. On many TVs, the correct value is 0 or near 0.", "Sharpness तब तक कम करें जब तक काली लाइनों के किनारे पर चमक न रहे। कई TV पर सही मान 0 या उसके पास होता है।", "Verringere die Schärfe, bis die schwarzen Linien keinen hellen Rand haben. Bei vielen TVs ist 0 oder fast 0 richtig.");
        p("w.color.t", "Set Color", "Color सेट करें", "Farbe einstellen");
        p("w.color.b", "Increase Color until the red bar starts to glow. Then decrease it by 2 or 3 steps. The skin tones must look natural. Do not change Tint.", "Color तब तक बढ़ाएँ जब तक लाल बार चमकने न लगे। फिर 2 या 3 कदम कम करें। त्वचा के रंग प्राकृतिक दिखने चाहिए। Tint न बदलें।", "Erhöhe die Farbe, bis der rote Balken zu leuchten beginnt. Verringere sie dann um 2 oder 3 Stufen. Hauttöne müssen natürlich aussehen. Ändere den Farbton nicht.");
        p("w.gray.t", "Check the gray scale", "ग्रे स्केल जाँचें", "Graustufen prüfen");
        p("w.gray.b", "The gray ramp must have no color tint. If it looks blue, set Color temperature to Warm. You must see all 16 steps.", "ग्रे पट्टी में कोई रंग नहीं दिखना चाहिए। नीली दिखे तो Color temperature को Warm करें। सभी 16 कदम दिखने चाहिए।", "Der Graukeil darf keinen Farbstich haben. Wirkt er blau, stelle die Farbtemperatur auf Warm. Du musst alle 16 Stufen sehen.");
        p("w.size.t", "Show the full picture", "पूरी तस्वीर दिखाएँ", "Ganzes Bild zeigen");
        p("w.size.b", "Set Picture size to Just Scan, Screen Fit, Full Pixel or 1:1. You must see the thin white line on all 4 edges.", "Picture size को Just Scan, Screen Fit, Full Pixel या 1:1 करें। चारों किनारों पर पतली सफ़ेद लाइन दिखनी चाहिए।", "Stelle das Bildformat auf Just Scan, Bildschirmanpassung, Full Pixel oder 1:1. Du musst die dünne weiße Linie an allen 4 Rändern sehen.");
        p("w.done.t", "The setup is complete", "सेटअप पूरा हुआ", "Einrichtung fertig");
        p("w.done.b", "The TV keeps these values for this HDMI input. HDR films use separate values. Select Movie mode for HDR too. Press OK to close.", "TV इस HDMI इनपुट के लिए ये मान रखता है। HDR फ़िल्मों के मान अलग होते हैं। HDR के लिए भी Movie मोड चुनें। बंद करने के लिए OK दबाएँ।", "Der TV speichert diese Werte für diesen HDMI-Eingang. HDR-Filme haben eigene Werte. Wähle auch für HDR den Filmmodus. OK schließt.");
        p("p.pluge", "Black level", "काला स्तर", "Schwarzwert");
        p("p.white", "White level", "सफ़ेद स्तर", "Weißwert");
        p("p.ramp", "Gray scale", "ग्रे स्केल", "Graustufen");
        p("p.bars", "Color bars", "रंग पट्टियाँ", "Farbbalken");
        p("p.sharp", "Sharpness", "शार्पनेस", "Schärfe");
        p("p.size", "Picture size", "तस्वीर का आकार", "Bildformat");
        p("p.black", "Black", "काला", "Schwarz");
        p("p.full", "White", "सफ़ेद", "Weiß");
        p("p.red", "Red", "लाल", "Rot");
        p("p.green", "Green", "हरा", "Grün");
        p("p.blue", "Blue", "नीला", "Blau");
        p("p.grey", "Gray 50%", "ग्रे 50%", "Grau 50%");
        p("pic.pat", "Pattern %d of %d", "पैटर्न %d / %d", "Muster %d von %d");
    }

    static int idx() { return "hi".equals(lang) ? 1 : "de".equals(lang) ? 2 : 0; }
    static String t(String k) { String[] v = T.get(k); return v == null ? k : v[idx()]; }
    static String f(String k, Object... a) { return String.format(locale(), t(k), a); }
    static Locale locale() { return "hi".equals(lang) ? new Locale("hi", "IN") : "de".equals(lang) ? Locale.GERMANY : Locale.ENGLISH; }
    /** Hindi uses Latin digits on this screen so times stay easy to read. */
    static Locale dateLocale() { return "hi".equals(lang) ? Locale.forLanguageTag("hi-IN-u-nu-latn") : locale(); }
}
