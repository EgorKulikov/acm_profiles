package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
 */
public class WFData {
    private static Log log = LogFactory.getLog(WFData.class);
    static Map<String, Integer> priority = new HashMap<>();

    static {
        priority.put("gold", 4000);
        priority.put("silver", 3000);
        priority.put("bronze", 2000);
    }

    public static void main(String... args) throws Exception {
        Map<String, String> medals = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("input/medals.csv"));
        String s = null;
        while ((s = reader.readLine()) != null) {
            String[] tokens = s.split(";");
            medals.put(tokens[0].trim() + " " + tokens[1], tokens[2]);
        }
        List<Person> persons = new ArrayList<>();
        for (int year = 1999; year <= 2017; year++) {
            String page = loadPage("https://icpc.baylor.edu/worldfinals/teams/" + year);
            int index;
            String team = null;
            String medal = null;
            while ((index = page.indexOf("<span class=\"teamMember\">")) != -1) {
                int teamIndex = page.indexOf("<span class=\"gridCols\">");
                if (teamIndex != -1 && teamIndex < index) {
                    page = page.substring(teamIndex);
                    page = page.substring(page.indexOf(">") + 1);
                    team = page.substring(0, page.indexOf("\n")).trim().replace("&amp;", "&");
                    index = page.indexOf("<span class=\"teamMember\">");
                    medal = medals.get(team + " " + year);
                    medals.remove(team + " " + year);
                }
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                String name = page.substring(0, page.indexOf(","));
                page = page.substring(page.indexOf("<span class=\"role\">"));
                page = page.substring(page.indexOf(">") + 1);
                String role = page.substring(0, page.indexOf("<"));
                if ("Lei Chen".equals(name) && year != 2017) {
                    continue;
                }
                if ("Timothy Smith".equals(name) && year != 2017) {
                    continue;
                }
                if ("Yash Kumar".equals(name) && year != 2017) {
                    continue;
                }
                if ("Ahmed Hamed".equals(name) && year != 2015 && year != 2017) {
                    continue;
                }
                if ("Felipe Souza".equals(name) && year != 2012 && year != 2017) {
                    continue;
                }
                if ("Thanh Trung Nguyen".equals(name) && year != 2017) {
                    continue;
                }
                if ("Hao Cui".equals(name) && year != 2017) {
                    continue;
                }
                if ("Coach".equals(role)) {
                    if ("Hao Wu".equals(name) && year != 2017) {
                        continue;
                    }
                    if (medal != null) {
                        if ("win".equals(medal)) {
                            persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC win as " +
                                    "coach", year, 5000)));
                        } else {
                            persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC " + medal +
                                    " medal as coach", year, priority.get(medal))));
                        }
                    } else {
                        persons.add(new Person().setName(name).addAchievement(new Achievement("World Finals coach",
                                year, 1000)));
                    }
                } else if ("Contestant".equals(role)) {
                    if (medal != null) {
                        if ("win".equals(medal)) {
                            persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC win", year,
                                    10000)));
                        } else {
                            persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC " + medal +
                                    " medal", year, priority.get(medal) * 2)));
                        }
                    } else {
                        persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC Finalist",
                                year, 1000)));
                    }
                }
            }
            log.info(year + " processed");
        }
        if (!medals.isEmpty()) {
            for (String ss : medals.keySet()) {
                log.error(ss + " was not found");
            }
//            log.error(medals.size());
        }
        Utils.mapper.writeValue(new File("input/wf.json"), persons);
    }
}
