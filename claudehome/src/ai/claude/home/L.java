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
        p("tonight", "Tonight", "आज रात", "Heute Abend");
    }

    static int idx() { return "hi".equals(lang) ? 1 : "de".equals(lang) ? 2 : 0; }
    static String t(String k) { String[] v = T.get(k); return v == null ? k : v[idx()]; }
    static String f(String k, Object... a) { return String.format(locale(), t(k), a); }
    static Locale locale() { return "hi".equals(lang) ? new Locale("hi", "IN") : "de".equals(lang) ? Locale.GERMANY : Locale.ENGLISH; }
    /** Hindi uses Latin digits on this screen so times stay easy to read. */
    static Locale dateLocale() { return "hi".equals(lang) ? Locale.forLanguageTag("hi-IN-u-nu-latn") : locale(); }
}
