package net.egork.teaminfo.tools;

import net.egork.teaminfo.data.University;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
 */
public class SnarkRaw {
    public static void main(String...args) throws Exception {
        String page = loadPage("http://finals.snarknews.info/index.cgi?data=stat/uniall&year=2016&class=final2016");
        page = page.substring(page.indexOf("<th>University</th>"));
        page = page.substring(0, page.indexOf("</table>"));
        int index;
        List<University> universities = new ArrayList<>();
        while ((index = page.indexOf("<tr>")) != -1) {
            page = page.substring(index + 4);
            index = page.indexOf("</tr>");
            String row = page.substring(0, index);
            page = page.substring(index);
            String[] tokens = row.split("</td><td>");
            tokens[1] = tokens[1].substring(tokens[1].indexOf(">") + 1);
            tokens[1] = tokens[1].substring(0, tokens[1].indexOf("<a>"));
            int apps = 1;
            int wins = 0;
            int gold = 0;
            int silver = 0;
            int bronze = 0;
            for (int i = 3; i <= 28; i++) {
                if ("-".equals(tokens[i])) {
                    continue;
                }
                apps++;
                if ("<font color=red>1</font>".equals(tokens[i])) {
                    wins++;
                }
                if (tokens[i].contains("red")) {
                    gold++;
                }
                if (tokens[i].contains("blue")) {
                    silver++;
                }
                if (tokens[i].contains("green")) {
                    bronze++;
                }
            }
            University university = new University();
            university.setFullName(tokens[1]);
            university.setAppearances(apps);
            university.setWins(wins);
            university.setGold(gold);
            university.setSilver(silver);
            university.setBronze(bronze);
            universities.add(university);
        }
        mapper.writeValue(new File("input/snark_data.csv"), universities);
    }
}
