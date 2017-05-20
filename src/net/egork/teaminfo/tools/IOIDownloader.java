package net.egork.teaminfo.tools;

import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
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
            while ((index = page.indexOf("<div class=\"alternatename\">")) != -1 && !person.getName().equals("Po-En Chen")) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                person.addAltName(page.substring(0, page.indexOf("<")));
            }
            index = page.indexOf("http://community.topcoder.com/tc?module=MemberProfile");
            if (index != -1 && person.getName().equals("Konstantin Semenov")) {
                page = page.substring(index);
                page = page.substring(page.indexOf("cr=") + 3);
                String tcId = page.substring(0, page.indexOf("\""));
                person.setTcId(tcId);
            }
            index = page.indexOf("http://codeforces.com/profile/");
            if (index != -1 && !person.getName().equals("Chun Yin Samspon Lee")) {
                page = page.substring(index);
                page = page.substring(page.indexOf("profile/") + 8);
                person.setCfHandle(page.substring(0, page.indexOf("\"")));
            }
            page = page.substring(page.indexOf("Medal</a>"));
            page = page.substring(0, page.indexOf("</table>"));
            while ((index = page.indexOf("<tr>")) != -1) {
                page = page.substring(index);
                index = page.indexOf("olympiads/");
                String start = page.substring(0, index);
                page = page.substring(index);
                page = page.substring(page.indexOf("/") + 1);
                String year = page.substring(0, page.indexOf("\""));
                if (start.contains("gold")) {
                    person.addAchievement(new Achievement("IOI Gold Medalist", Integer.parseInt(year), 50));
                } else if (start.contains("silver")) {
                    person.addAchievement(new Achievement("IOI Silver Medalist", Integer.parseInt(year), 30));
                } else if (start.contains("bronze")) {
                    person.addAchievement(new Achievement("IOI Bronze Medalist", Integer.parseInt(year), 20));
                } else {
                    person.addAchievement(new Achievement("IOI Participant", Integer.parseInt(year), 10));
                }
            }
            person.setIoiID(id);
            persons.add(person);
            if (++count % 100 == 0) {
                log.info(count + " processed");
            }
        }
        mapper.writeValue(new File("input/ioi.json"), persons);
    }
}
