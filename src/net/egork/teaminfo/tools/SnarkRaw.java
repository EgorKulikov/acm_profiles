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
        String page = loadPage("http://finals.snarknews.info/index.cgi?data=stat/uniall&year=2019&class=final2019");
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
            List<Integer> appYears = new ArrayList<>();
            int wins = 0;
            List<Integer> winYears = new ArrayList<>();
            int gold = 0;
            List<Integer> goldYears = new ArrayList<>();
            int silver = 0;
            List<Integer> silverYears = new ArrayList<>();
            int bronze = 0;
            List<Integer> bronzeYears = new ArrayList<>();
            int regionals = 0;
            List<Integer> regYears = new ArrayList<>();
            for (int i = 3; i <= 30; i++) {
                if ("-".equals(tokens[i])) {
                    continue;
                }
                if (tokens[i].endsWith("*")) {
                    regionals++;
                    tokens[i] = tokens[i].substring(0, tokens[i].length() - 2);
                    regYears.add(i + 1989);
                }
                apps++;
                appYears.add(i + 1989);
                if ("<font color=red>1</font>".equals(tokens[i])) {
                    wins++;
                    winYears.add(i + 1989);
                }
                if (tokens[i].contains("red")) {
                    gold++;
                    goldYears.add(i + 1989);
                }
                if (tokens[i].contains("blue")) {
                    silver++;
                    silverYears.add(i + 1989);
                }
                if (tokens[i].contains("green")) {
                    bronze++;
                    bronzeYears.add(i + 1989);
                }
            }
            appYears.add(2020);
            University university = new University();
            university.setFullName(tokens[1]);
            university.setAppearances(apps);
            university.setAppYears(appYears);
            university.setWins(wins);
            university.setWinYears(winYears);
            university.setGold(gold);
            university.setGoldYears(goldYears);
            university.setSilver(silver);
            university.setSilverYears(silverYears);
            university.setBronze(bronze);
            university.setBronzeYears(bronzeYears);
            university.setRegionalChampionships(regionals);
            university.setRegYears(regYears);
            universities.add(university);
        }
        mapper.writeValue(new File("input/snark_data.csv"), universities);
    }
}
