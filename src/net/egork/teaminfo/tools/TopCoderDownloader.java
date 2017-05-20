package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
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
public class TopCoderDownloader {
    static Log log = LogFactory.getLog(TopCoderDownloader.class);

    public static void main(String... args) throws Exception {
        String page = loadPage("http://www.topcoder.com/tc?module=BasicData&c=dd_coder_list");
        log.info("Page downloaded");
        int index;
        List<Person> persons = new ArrayList<>();
        int count = 0;
        int start = 0;
        while ((index = page.indexOf("<row>", start)) != -1) {
            start = index;
            Person person = new Person();
            start = page.indexOf("<coder_id>", start);
            start = page.indexOf(">", start) + 1;
            person.setTcId(page.substring(start, page.indexOf("<", start)));
            start = page.indexOf("<handle>", start);
            start = page.indexOf(">", start) + 1;
            person.setTcHandle(page.substring(start, page.indexOf("<", start)));
            start = page.indexOf("<alg_rating", start);
            start = page.indexOf("ing", start) + 3;
            if (page.charAt(start) == '/') {
                continue;
            }
            start++;
            person.setTcRating(Integer.parseInt(page.substring(start, page.indexOf("<", start))));
            if (++count % 100 == 0) {
                log.info(count + " processed");
            }
            if (person.getTcHandle().equals("eagle93")) {
                continue;
            }
            if (person.getTcHandle().equals("Vytautas")) {
                continue;
            }
            persons.add(person);
        }
        Utils.mapper.writeValue(new File("input/topcoder.json"), persons);
    }
}
