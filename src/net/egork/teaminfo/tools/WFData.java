package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
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
public class WFData {
    private static Log log = LogFactory.getLog(WFData.class);

    public static void main(String... args) throws Exception {
        List<Person> persons = new ArrayList<>();
        for (int year = 1999; year <= 2015; year++) {
            String page = loadPage("https://icpc.baylor.edu/worldfinals/teams/" + year);
            int index;
            while ((index = page.indexOf("<span class=\"teamMember\">")) != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                String name = page.substring(0, page.indexOf(","));
                page = page.substring(page.indexOf("<span class=\"role\">"));
                page = page.substring(page.indexOf(">") + 1);
                String role = page.substring(0, page.indexOf("<"));
                if ("Lei Chen".equals(name)) {
                    continue;
                }
                if ("Felipe Souza".equals(name) && year != 2012) {
                    continue;
                }
                if ("Coach".equals(role)) {
                    persons.add(new Person().setName(name).addAchievement(new Achievement("World Finals coach",
                            year, 1000)));
                } else if ("Contestant".equals(role)) {
                    persons.add(new Person().setName(name).addAchievement(new Achievement("ACM ICPC Finalist",
                            year, 1000)));
                }
            }
            log.info(year + " processed");
        }
        Utils.mapper.writeValue(new File("input/wf.json"), persons);
    }
}
