package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
 */
public class IOIDownloader {
    static Log log = LogFactory.getLog(IOIDownloader.class);

    public static void main(String... args) throws Exception {
        String mainPage = loadPage("http://stats.ioinformatics.org/halloffame/all");
        mainPage = mainPage.substring(mainPage.indexOf("people/") + 1);
        int index;
        int count = 0;
        List<Person> persons = new ArrayList<>();
        while ((index = mainPage.indexOf("people/")) != -1) {
            mainPage = mainPage.substring(index + 7);
            String id = mainPage.substring(0, mainPage.indexOf("\""));
            String page = loadPage("http://stats.ioinformatics.org/people/" + id);
            Person person = new Person();
            page = page.substring(page.indexOf("<div class=\"participantname\">"));
            page = page.substring(page.indexOf(">") + 6);
            person.setName(page.substring(0, page.indexOf("<")));
            while ((index = page.indexOf("<div class=\"alternatename\">")) != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                person.addAltName(page.substring(0, page.indexOf("<")));
            }
            index = page.indexOf("http://community.topcoder.com/tc?module=MemberProfile");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf("cr=") + 3);
                String tcId = page.substring(0, page.indexOf("\""));
                person.setTcId(tcId);
            }
            index = page.indexOf("http://codeforces.com/profile/");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf("profile/") + 8);
                person.setCfHandle(page.substring(0, page.indexOf("\"")));
            }
            page = page.substring(page.indexOf("Medal</a>"));
            List<String> goldMedals = new ArrayList<>();
            List<String> silverMedals = new ArrayList<>();
            List<String> bronzeMedals = new ArrayList<>();
            List<String> noMedals = new ArrayList<>();
            page = page.substring(0, page.indexOf("</table>"));
            while ((index = page.indexOf("<tr>")) != -1) {
                page = page.substring(index);
                index = page.indexOf("olympiads/");
                String start = page.substring(0, index);
                page = page.substring(index);
                page = page.substring(page.indexOf("/") + 1);
                String year = page.substring(0, page.indexOf("\""));
                if (start.contains("gold")) {
                    goldMedals.add(year);
                } else if (start.contains("silver")) {
                    silverMedals.add(year);
                } else if (start.contains("bronze")) {
                    bronzeMedals.add(year);
                } else {
                    noMedals.add(year);
                }
            }
            if (!goldMedals.isEmpty()) {
                Collections.reverse(goldMedals);
                person.addAchievement(new Achievement("IOI Gold Medalist (" + Utils.getYears(goldMedals) + ")", 50 +
                        goldMedals.size()));
            }
            if (!silverMedals.isEmpty()) {
                Collections.reverse(silverMedals);
                person.addAchievement(new Achievement("IOI Silver Medalist (" + Utils.getYears(silverMedals) + ")", 30 +
                        silverMedals.size()));
            }
            if (!bronzeMedals.isEmpty()) {
                Collections.reverse(bronzeMedals);
                person.addAchievement(new Achievement("IOI Bronze Medalist (" + Utils.getYears(bronzeMedals) + ")", 20 +
                        bronzeMedals.size()));
            }
            if (!noMedals.isEmpty()) {
                Collections.reverse(noMedals);
                person.addAchievement(new Achievement("IOI Participant (" + Utils.getYears(noMedals) + ")", 10 +
                        noMedals.size()));
            }
            persons.add(person);
            if (++count % 100 == 0) {
                log.info(count + " processed");
            }
        }
        mapper.writeValue(new File("input/ioi.json"), persons);
    }
}
