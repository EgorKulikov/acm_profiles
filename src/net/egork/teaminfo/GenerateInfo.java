package net.egork.teaminfo;

import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import net.egork.teaminfo.data.Record;
import net.egork.teaminfo.data.University;
import net.egork.teaminfo.tools.PersonalDatabase;
import net.egork.teaminfo.tools.ProfileDownloader;
import net.egork.teaminfo.tools.SnarkRaw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
 */
public class GenerateInfo {
    private static Log log = LogFactory.getLog(GenerateInfo.class);

    static {
        InputStream stream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        if (stream != null) {
            try {
                System.getProperties().load(stream);
            } catch (IOException e) {
                log.error("Can't load properties", e);
            }
        }
    }

    static Record[] records = new Record[129];

    public static void main(String[] args) throws Exception {
        //Getting ready
        init();
        readIds();
        readMyICPC();

        //Teams
        readShortNames();
        readSnark();
        readRegionalChamps();

        //Personal
        readPersonalDatabase();

        saveResults();
        saveRepeatFinalists();
    }

    private static void readRegionalChamps() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/regional_champs.txt"));
        String s;
        while ((s = reader.readLine()) != null) {
            for (int i = 1; i <= 128; i++) {
                University university = records[i].university;
                if (university.getFullName().equals(s)) {
                    university.setRegionalChampionships(university.getRegionalChampionships() + 1);
                }
            }
        }
        log.info("Regional champs done");
    }

    private static void saveRepeatFinalists() throws Exception {
        PrintWriter out = new PrintWriter("repeat.cvs");
        for (int i = 1; i <= 128; i++) {
            Record record = records[i];
            List<String> good = new ArrayList<>();
            for (Person person : record.contestants) {
                for (Achievement achievement : person.getAchievements()) {
                    if (achievement.achievement.startsWith("ACM ICPC Finalist")) {
                        good.add(";" + person.getName() + ";" + achievement.achievement.substring(achievement
                                .achievement.length() - 4));
                    }
                }
            }
            if (good.size() != 0) {
                out.print(record.university.getFullName() + ";" + record.university.getRegion() + ";" + good.size());
                for (String s : good) {
                    out.print(s);
                }
                out.println();
            }
        }
        out.close();
        log.info("Repeats processed");
    }

    private static void readPersonalDatabase() throws Exception {
        if (Boolean.getBoolean("recreateUserDatabase")) {
            PersonalDatabase.main();
        }
        List<Person> persons = Utils.readList("input/database.json", Person.class);
        int coachesFound = 0;
        int participantsFound = 0;
        for (int i = 1; i <= 128; i++) {
            Record record = records[i];
            boolean coachFound = false;
            for (Person person : persons) {
                if (record.coach.isSamePerson(person)) {
                    record.coach.updateFrom(person);
                    coachFound = true;
                    break;
                }
            }
            if (coachFound) {
//                log.info("Data for coach " + record.coach.getName());
                coachesFound++;
            }
            for (Person contestant : record.contestants) {
                boolean found = false;
                for (Person person : persons) {
                    if (contestant.isSamePerson(person)) {
                        contestant.updateFrom(person);
                        found = true;
                        break;
                    }
                }
                if (found) {
//                    log.info("Data for contestant " + contestant.getName());
                    participantsFound++;
                }
            }
        }
        for (int i = 1; i <= 128; i++) {
            Record record = records[i];
            record.coach.compressAchievements();
            for (Person contestant : record.contestants) {
                contestant.compressAchievements();
            }
        }
        log.info("Personal info integrated");
        log.info("Coaches: " + coachesFound + "/128 found");
        log.info("Participants: " + participantsFound + "/384 found");
    }

    private static void readSnark() throws Exception {
        if (Boolean.getBoolean("reloadSnarkTable")) {
            SnarkRaw.main();
        }
        List<University> universities = Utils.readList("input/snark_data.csv", University.class);
        BufferedReader reader = new BufferedReader(new FileReader("input/snark_to_long.cvs"));
        reader.readLine();
        Map<String, String> map = new HashMap<>();
        String s;
        while ((s = reader.readLine()) != null) {
            String[] tokens = s.split("#");
            map.put(tokens[0], tokens[1]);
        }
        for (int i = 1; i <= 128; i++) {
            boolean found = false;
            for (University university : universities) {
                if (sameUniversity(records[i].university, university, map)) {
                    found = true;
                    university.setFullName(null);
                    records[i].university.updateFrom(university);
                    break;
                }
            }
            if (!found) {
                log.info("Debut for " + records[i].university.getFullName());
                University university = new University();
                university.setAppearances(0);
                university.setWins(0);
                university.setGold(0);
                university.setSilver(0);
                university.setBronze(0);
                university.setRegionalChampionships(0);
                records[i].university.updateFrom(university);
            }
        }
        log.info("University achievements loaded");
    }

    private static void readShortNames() throws Exception {
        University[] universities = new University[128];
        BufferedReader reader = new BufferedReader(new FileReader("input/short.cvs"));
        for (int i = 0; i < 128; i++) {
            String[] tokens = reader.readLine().split(";");
            universities[i] = new University();
            universities[i].setFullName(tokens[0]);
            universities[i].setShortName(tokens[1]);
            universities[i].setHashTag(tokens[2]);
        }
        for (int i = 1; i <= 128; i++) {
            boolean found = false;
            for (int j = 0; j < 128; j++) {
                if (Objects.equals(universities[j].getFullName(), records[i].university.getFullName())) {
                    records[i].university.updateFrom(universities[j]);
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("Short name for " + records[i].university.getFullName() + " not found");
            }
        }
        log.info("University short names loaded");
    }

    private static void readMyICPC() throws Exception {
        if (Boolean.getBoolean("reloadTeamProfiles")) {
            ProfileDownloader.main();
        }
        Record[] pageRecords = new Record[129];
        for (int i = 1; i <= 128; i++) {
            String page = readPage("input/pages/" + i);
            pageRecords[i] = new Record(i);
            page = page.substring(page.indexOf("<div id=\"pageTitle\""));
            page = page.substring(page.indexOf("<h2>") + 4);
            int index = page.indexOf("</h2>");
            String uniName = page.substring(0, index).trim();
            pageRecords[i].university.setFullName(uniName);
            page = page.substring(index);
            page = page.substring(page.indexOf("<h4>"));
            page = page.substring(page.indexOf("<td>") + 4);
            page = page.substring(page.indexOf(">") + 1);
            index = page.indexOf("</a>");
            String coachName = page.substring(0, index);
            pageRecords[i].coach.setName(coachName);
            page = page.substring(index);
            page = page.substring(page.indexOf("<h4>"));
            page = page.substring(page.indexOf("<td>") + 4);
            page = page.substring(page.indexOf(">") + 1);
            index = page.indexOf("</a>");
            String contestant1Name = page.substring(0, index);
            pageRecords[i].contestants[0].setName(contestant1Name);
            page = page.substring(index);
            page = page.substring(page.indexOf("<td>") + 4);
            page = page.substring(page.indexOf(">") + 1);
            index = page.indexOf("</a>");
            String contestant2Name = page.substring(0, index);
            pageRecords[i].contestants[1].setName(contestant2Name);
            page = page.substring(index);
            page = page.substring(page.indexOf("<td>") + 4);
            page = page.substring(page.indexOf(">") + 1);
            index = page.indexOf("</a>");
            String contestant3Name = page.substring(0, index);
            pageRecords[i].contestants[2].setName(contestant3Name);
            page = page.substring(index);
            page = page.substring(page.indexOf("Team name:"));
            page = page.substring(page.indexOf("<td>") + 4);
            index = page.indexOf("</td>");
            String teamName = page.substring(0, index);
            pageRecords[i].team.setName(teamName);
            page = page.substring(index);
            page = page.substring(page.indexOf("Region:"));
            page = page.substring(page.indexOf("<td>") + 4);
            index = page.indexOf("</td>");
            String region = page.substring(0, index);
            pageRecords[i].university.setRegion(region);
            page = page.substring(index);
            page = page.substring(page.indexOf("Homepage:"));
            page = page.substring(page.indexOf("<td>") + 4);
            page = page.substring(page.indexOf(">") + 1);
            index = page.indexOf("</a>");
            String homePage = page.substring(0, index);
            pageRecords[i].university.setUrl(homePage);
            page = page.substring(index);
            page = page.substring(page.indexOf("Regional results"));
            List<String> regionalResults = new ArrayList<>();
            while ((index = page.indexOf("<h5 class=\"panel-title\">")) != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                index = page.indexOf("</h5>");
                String regName = page.substring(0, index);
                page = page.substring(index);
                page = page.substring(page.indexOf("Rank:"));
                page = page.substring(page.indexOf("<td>") + 4);
                index = page.indexOf("</td>");
                String place = page.substring(0, index);
                page = page.substring(index);
                regionalResults.add(regName + ", " + place + appropriateEnd(Integer.parseInt(place)));
            }
            pageRecords[i].team.setRegionals(regionalResults);
        }
        for (int i = 1; i <= 128; i++) {
            boolean found = false;
            for (int j = 1; j <= 128; j++) {
                if (pageRecords[j] != null &&
                        Objects.equals(pageRecords[j].university.getFullName(), records[i].university.getFullName())) {
                    records[i].university.updateFrom(pageRecords[j].university);
                    records[i].team.updateFrom(pageRecords[j].team);
                    records[i].coach.updateFrom(pageRecords[j].coach);
                    for (int k = 0; k < 3; k++) {
                        records[i].contestants[k].updateFrom(pageRecords[j].contestants[k]);
                    }
                    pageRecords[j] = null;
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("No page for " + records[i].university.getFullName());
            }
        }
        for (int i = 1; i <= 128; i++) {
            if (pageRecords[i] != null) {
                log.error("No match for " + pageRecords[i].university.getFullName());
            }
        }
        log.info("MyICPC loaded");
    }

    private static void saveResults() throws Exception {
        for (int i = 1; i <= 128; i++) {
            Utils.mapper.writeValue(new File("output/" + i + ".json"), records[i]);
        }
        Utils.mapper.writeValue(new File("output/all.json"), Arrays.asList(Arrays.copyOfRange(records, 1, 129)));
    }

    private static void readIds() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/ids.txt"));
        reader.readLine();
        for (int i = 1; i <= 128; i++) {
            University university = new University();
            university.setFullName(reader.readLine());
            records[i].university.updateFrom(university);
        }
        log.info("ids loaded");
    }

    private static void init() {
        for (int i = 1; i <= 128; i++) {
            records[i] = new Record(i);
        }
        log.info("Init done");
    }
}
