package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import static net.egork.teaminfo.Utils.loadPage;

/**
 * @author egor@egork.net
 */
public class SnarkDownloader {
    static Log log = LogFactory.getLog(SnarkDownloader.class);

    static Set<String> skipable = new HashSet<>();
    static Map<String, Integer> priority = new HashMap<>();
    static Map<String, String> shortName = new HashMap<>();

    static void add(String name, String shortName, int priority) {
        SnarkDownloader.priority.put(name, priority);
        SnarkDownloader.shortName.put(name, shortName);
    }

    static {
        skipable.add("Codeforces powered tournaments");
        skipable.add("International Olympiad in Informatics");
        skipable.add("ACM International Collegiate Programming Contest");

        add("Russian Code Cup Finalist", "RCC Finalist", 25);
        add("TopCoder Open Runner-Up", "TCO 2nd", 105);
        add("TopCoder Open Semifinalist", "TCO Semifinalist", 50);
        add("TopCoder Collegiate Challenge Semifinalist", "TCCC Semifinalist", 50);
        add("Google Code Jam Finalist", "GCJ Finalist", 50);
        add("Google Code Jam Runner-Up", "GCJ 2nd", 105);
        add("Kotlin Challenge Winner", "Kotlin Winner", 40);
        add("Facebook Hacker Cup Third Place", "FBHC 3rd", 50);
        add("Bayan Contest Finalist", "Bayan Finalist", 25);
        add("TopCoder Invitational Semifinalist", "TCI Semifinalist", 50);
        add("Yandex.Algorithm Fourth Place", "Yandex.Algorithm Finalsit", 25);
        add("Yandex.Algorithm Winner", "Yandex.Algorithm Winner", 60);
        add("Russian Code Cup Third Place", "RCC 3rd", 50);
        add("TopCoder High School Finalist", "THSC Finalist", 33);
        add("TopCoder Collegiate Challenge Third Place", "TCCC 3rd", 100);
        add("Google Code Jam Winner", "GCJ Winner", 110);
        add("TopCoder Open Winner", "TCO Winner", 110);
        add("TopCoder High School Winner", "TCHS Winner", 60);
        add("Yandex.Algorithm Finalist", "Yandex.Algorithm Finalist", 25);
        add("Kotlin Challenge Third Place", "Kotlin 3rd", 30);
        add("TopCoder Invitational Runner-Up", "TCI 2nd", 105);
        add("Kotlin Challenge Runner-Up", "Kotling 2nd", 35);
        add("TopCoder Invitational Finalist", "TCI Finalist", 75);
        add("Bayan Contest Runner-Up", "Bayan 2nd", 55);
        add("Bayan Contest Third Place", "Bayan 3rd", 50);
        add("TopCoder Invitational Third Place", "TCI 3rd", 100);
        add("Russian Code Cup Runner-Up", "RCC 2nd", 55);
        add("TopCoder High School Third Place", "TCHS 3rd", 50);
        add("TTB Onsite Winner", "TTB Winner", 40);
        add("Google Code Jam-Europe Runner-Up", "GCJE 2nd", 55);
        add("Yandex.Algorithm Fifth Place", "Yandex.Algorithm Finalist", 25);
        add("Facebook Hacker Cup Finalist", "FBHC Finalist", 25);
        add("TopCoder High School Semifinalist", "TCHS", 25);
        add("TopCoder Collegiate Challenge Finalist", "TCCC Finalist", 75);
        add("Google Code Jam-Europe Finalist", "GCJE Finalist", 25);
        add("Russian Code Cup Winner", "RCC Winner", 60);
        add("Kotlin Challenge Finalist", "Kotlin Finalist", 15);
        add("Google Code Jam Third Place", "GCJ 3rd", 100);
        add("Yandex.Algorithm Third Place", "Yandex.Algorithm 3rd", 50);
        add("TopCoder Invitational Winner", "TCI Winner", 110);
        add("TTB Onsite Runner-Up", "TTB 2nd", 35);
        add("Yandex.Algorithm Runner-Up", "Yandex.Algorithm 2nd", 55);
        add("TTB Onsite Finalist", "TTB Finalist", 15);
        add("TopCoder Open Third Place", "TCO 3rd", 100);
        add("TopCoder High School Runner-Up", "THSC 2nd", 55);
        add("TopCoder Open Finalist", "TCO Finalist", 75);
        add("Bayan Contest Winner", "Bayan Winner", 50);
        add("Facebook Hacker Cup Winner", "FBHC Winner", 60);
        add("Facebook Hacker Cup Runner-Up", "FBHC 2nd", 55);
        add("TopCoder Collegiate Challenge Winner", "TCCC Winner", 110);
        add("TopCoder Collegiate Challenge Runner-Up", "TCCC 2nd", 105);
        add("TTB Onsite Third Place", "TTB 3rd", 30);
        add("Distributed Google Code Jam Winner", "DGCJ Winner", 110);
        add("Distributed Google Code Jam Runner-Up", "DGCJ 2nd", 105);
        add("Distributed Google Code Jam Third Place", "DGCJ 3rd", 100);
        add("Distributed Google Code Jam Finalist", "DGCJ Finalist", 50);
    }

    public static void main(String... args) throws Exception {
        String page = loadPage("http://www.snarknews.info/index.cgi?data=all");
        page = page.substring(page.indexOf("<td class=\"maintext\">"));
        int index;
        List<String> ids = new ArrayList<>();
        while ((index = page.indexOf("<tr><td>")) != -1) {
            page = page.substring(index + 8);
            page = page.substring(page.indexOf("data=plr/"));
            index = page.indexOf(".dat");
            String id = page.substring(0, index);
            ids.add(id);
        }
//        Set<String> parts = new HashSet<>();
        Set<String> unknownAchievements = new HashSet<>();
        List<Person> contestans = new ArrayList<>();
        for (String id : ids) {
            if (id.contains("Andrius_Stankevičius")) {
                continue;
            }
            id = id.replace("'", "_").replace("Artyom_Vasiliev", "Artyom_Vasil_ev");
            page = loadPage("http://www.snarknews.info/index.cgi?" + id + ".dat");
            page = page.substring(page.indexOf("<center><h3>"));
            page = page.substring(page.indexOf("\">") + 2);
            index = page.indexOf('(');
            int other = page.indexOf("<");
            if (index == -1 || other != -1 && index > other) {
                index = other;
            }
            Person contestant = new Person();
            String name = page.substring(0, index).trim();
            contestant.setName(name);
            index = page.indexOf("On TopCoder:");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                index = page.indexOf("<");
                String tcHandle = page.substring(0, index);
                contestant.setTcHandle(tcHandle);
            }
            index = page.indexOf("On Codeforces:");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                index = page.indexOf("<");
                String cfHandle = page.substring(0, index);
                contestant.setCfHandle(cfHandle);
            }
            while ((index = page.indexOf("<center><h4>")) != -1) {
                page = page.substring(index + 1);
                int endIndex = page.indexOf("<center><h4>");
                String part = page.substring(0, endIndex == -1 ? page.length() : endIndex);
                part = part.substring(11);
                String title = part.substring(0, part.indexOf("<"));
                if (skipable.contains(title)) {
                    continue;
                }
                while ((index = part.indexOf("<br>")) != -1) {
                    part = part.substring(index + 4);
                    index = part.indexOf(":");
                    String achievement = part.substring(0, index);
                    if (!priority.containsKey(achievement)) {
                        unknownAchievements.add(achievement);
                        continue;
                    }
                    part = part.substring(index + 1);
                    String[] tokens = part.substring(0, part.indexOf("<")).split(",");
                    for (String token : tokens) {
                        token = token.trim();
                        if (token.contains("-")) {
                            int start = Integer.parseInt(token.substring(0, token.indexOf('-')).trim());
                            int end = Integer.parseInt(token.substring(token.indexOf('-')).trim() + 1);
                            for (int i = start; i <= end; i++) {
                                contestant.addAchievement(new Achievement(shortName.get(achievement), i, priority.get(achievement)));
                            }
                        } else {
                            if (title.contains("Yandex") && token.equals("2011")) {
                                continue;
                            }
                            contestant.addAchievement(new Achievement(shortName.get(achievement), Integer.parseInt(token), priority.get(achievement)));
                        }
                    }
                }
//                parts.add(title);
            }
//            while ((index = page.indexOf("<br>")) != -1) {
//                page = page.substring(index + 4);
//                index = page.indexOf("<");
//                String achievement = page.substring(0, index).trim();
//                contestant.addAchievement(achievement);
//            }
            log.info(name + " processed");
            contestans.add(contestant);
        }
        PrintWriter pw = new PrintWriter("output/unknown_snark");
        for (String part : unknownAchievements) {
            pw.println(part);
        }
        pw.close();
        Utils.mapper.writeValue(new File("input/snark.json"), contestans);
    }
}
