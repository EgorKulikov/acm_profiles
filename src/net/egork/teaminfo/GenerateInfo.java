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
    public static final int TEAM_NUM = 133;
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

    static Record[] records = new Record[TEAM_NUM + 1];

    public static void main(String[] args) throws Exception {
        //Getting ready
        init();
//        readIds();
        readMyICPC();
        readPersons();

        //Teams
//        readShortNames();
        readSnark();
        readRegionalChamps();
        readOpenCup();

//        readAltNames();

        //Personal
        readPersonalDatabase();

        saveResults();
        saveRatings();
        saveUniversities();
        checkSnark();
//        saveRepeatFinalists();
//        saveHRForm();
    }

    private static void checkSnark() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input/import_snark.csv"));
        PrintWriter errors = new PrintWriter("output/errors.txt");
        for (int i = 0; i < TEAM_NUM * 5; i++) {
            String s = reader.readLine();
            if (s == null || i % 5 == 0 || i % 5 == 4) {
                continue;
            }
            String[] data = s.split(";", -1);
            Person person = records[i / 5 + 1].contestants[i % 5 - 1];
            if (!person.getName().equals(data[0])) {
                errors.println(person.getName() + " has wrong name");
            }
            String tcHandle = null;
            if (!data[1].isEmpty()) {
                if (data[1].startsWith("0-")) {
//                    tcHandle = data[1].substring(2);
                } else {
                    tcHandle = data[1];
                }
            }
            String cfHandle = null;
            if (!data[2].isEmpty()) {
                if (data[2].startsWith("0-")) {
//                    cfHandle = data[2].substring(2);
                } else {
                    cfHandle = data[2];
                }
            }
            String ioiId = null;
            String prevFinal = null;
            for (int j = 3; j < data.length; j++) {
                if (data[j].startsWith("IOI")) {
                    ioiId = data[j].split(":")[1];
                } else if (data[j].startsWith("PL")) {
                    prevFinal = data[j].substring(2);
                }
            }
            if (!Utils.equals(tcHandle, person.getTcHandle())) {
                errors.println(person.getName() + " has wrong TC handle " + tcHandle);
            }
            if (!Utils.equals(cfHandle, person.getCfHandle())) {
                errors.println(person.getName() + " has wrong CF handle " + cfHandle);
            }
            if (!Utils.equals(ioiId, person.getIoiID())) {
                errors.println(person.getName() + " has wrong IOI id" + ioiId);
            }
            Set<String> personWF = new HashSet<>();
            for (Achievement achievement : person.getAchievements()) {
                if (achievement.priority >= 1000 && !achievement.achievement.toLowerCase().contains("coach")) {
                    String t = achievement.achievement;
                    for (int j = 0; j < t.length() - 3; j++) {
                        if (t.substring(j, j + 4).matches("\\d\\d\\d\\d")) {
                            personWF.add(t.substring(j, j + 4));
                        }
                    }
                }
            }
            Set<String> snarkWF = prevFinal == null ? Collections.emptySet() : Collections.singleton(prevFinal);
            if (!snarkWF.equals(personWF)) {
                errors.println(person.getName() + " has wrong finals");
            }
        }
        errors.close();
    }

    private static void saveUniversities() throws IOException {
        List<University> results = new ArrayList<>();
        for (int i = 1; i <= TEAM_NUM; i++) {
            results.add(records[i].university);
        }
        Utils.mapper.writeValue(new File("output/univs.json"), results);
    }

    private static void saveRatings() throws FileNotFoundException {
        NavigableSet<Person> contestantsTC = new TreeSet<>((o1, o2) -> {
            if (o1.getTcRating() != o2.getTcRating()) {
                return o2.getTcRating() - o1.getTcRating();
            }
            return o1.getTcHandle().compareTo(o2.getTcHandle());
        });
        NavigableSet<Person> contestantsAndCoachesTC = new TreeSet<>((o1, o2) -> {
            if (o1.getTcRating() != o2.getTcRating()) {
                return o2.getTcRating() - o1.getTcRating();
            }
            return o1.getTcHandle().compareTo(o2.getTcHandle());
        });
        NavigableSet<Person> contestantsCF = new TreeSet<>((o1, o2) -> {
            if (o1.getCfRating() != o2.getCfRating()) {
                return o2.getCfRating() - o1.getCfRating();
            }
            return o1.getCfHandle().compareTo(o2.getCfHandle());
        });
        NavigableSet<Person> contestantsAndCoachesCF = new TreeSet<>((o1, o2) -> {
            if (o1.getCfRating() != o2.getCfRating()) {
                return o2.getCfRating() - o1.getCfRating();
            }
            return o1.getCfHandle().compareTo(o2.getCfHandle());
        });
        for (int i = 1; i <= TEAM_NUM; i++) {
            Person coach = records[i].coach;
            if (coach.getTcRating() != -1) {
                contestantsAndCoachesTC.add(coach);
            }
            if (coach.getCfRating() != -1) {
                contestantsAndCoachesCF.add(coach);
            }
            for (Person contestant : records[i].contestants) {
                if (contestant.getTcRating() != -1) {
                    contestantsTC.add(contestant);
                    contestantsAndCoachesTC.add(contestant);
                }
                if (contestant.getCfRating() != -1) {
                    contestantsCF.add(contestant);
                    contestantsAndCoachesCF.add(contestant);
                }
            }
        }
        PrintWriter pw = new PrintWriter("output/contestants_cf.csv");
        for (Person person : contestantsCF) {
            pw.println(person.getName() + ";" + person.getCfHandle() + ";" + person.getCfRating());
        }
        pw.close();
        pw = new PrintWriter("output/contestants_and_coaches_cf.csv");
        for (Person person : contestantsAndCoachesCF) {
            pw.println(person.getName() + ";" + person.getCfHandle() + ";" + person.getCfRating());
        }
        pw.close();
        pw = new PrintWriter("output/contestants_and_coaches_tc.csv");
        for (Person person : contestantsAndCoachesTC) {
            pw.println(person.getName() + ";" + person.getTcHandle() + ";" + person.getTcRating());
        }
        pw.close();
        pw = new PrintWriter("output/contestants_tc.csv");
        for (Person person : contestantsTC) {
            pw.println(person.getName() + ";" + person.getTcHandle() + ";" + person.getTcRating());
        }
        pw.close();
    }

    private static void readPersons() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input/persons_data.csv"));

        int coach = 0;
        int contestant = 0;

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String[] data = line.split("~", -1);
            String role = data[6];
            if (role.contains("Team: Coach") || role.equals("Team: Contestant")) {
                Person person = new Person();
                person.setName(data[0]);
                person.addAltName(data[1] + " " + data[2]);
                if (!data[7].isEmpty()) {
                    person.setTwitterHandle(data[7]);
                }
                if (!data[9].isEmpty()) {
                    person.setCfHandle(convertHandle(data[9]));
                }
                if (!data[8].isEmpty()) {
                    person.setTcHandle(convertHandle(data[8]));
                }
                boolean found = false;
                for (int i = 1; i <= TEAM_NUM; i++) {
                    if (data[5].equals(records[i].university.getShortName())) {
                        found = true;
                        if (role.contains("Team: Coach")) {
                            if (Utils.samePerson(records[i].coach, person)) {
                                records[i].coach.updateFrom(person);
                                coach++;
                            }
                        } else {
                            boolean saved = false;
                            for (int j = 0; j < 3; j++) {
                                if (Utils.samePerson(records[i].contestants[j], person)) {
                                    records[i].contestants[j].updateFrom(person);
                                    saved = true;
                                    contestant++;
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    log.error("University not found " + data[5]);
                }
            }
        }

        reader = new BufferedReader(new FileReader("input/personal_update.csv"));

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String[] data = line.split("~", -1);
            String role = data[4];
            if (role.contains("Team: Coach") || role.equals("Team: Contestant")) {
                Person person = new Person();
                person.setName(data[0]);
                if (!data[6].isEmpty()) {
                    person.setCfHandle(convertHandle(data[6]));
                }
                if (!data[5].isEmpty()) {
                    person.setTcHandle(convertHandle(data[5]));
                }
                boolean found = false;
                for (int i = 1; i <= TEAM_NUM; i++) {
                    if (data[3].substring(data[3].indexOf(',') + 2).equals(records[i].university.getShortName())) {
                        found = true;
                        if (role.contains("Team: Coach")) {
                            if (Utils.samePerson(records[i].coach, person)) {
                                records[i].coach.updateFrom(person);
                                coach++;
                            }
                        } else {
                            boolean saved = false;
                            for (int j = 0; j < 3; j++) {
                                if (Utils.samePerson(records[i].contestants[j], person)) {
                                    records[i].contestants[j].updateFrom(person);
                                    saved = true;
                                    contestant++;
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    log.error("University not found " + data[5]);
                }
            }
        }
        log.info("Persons done, " + coach + "/133 coaches, " + contestant + "/399 contestants");
    }

    private static String convertHandle(String handle) {
        if (handle.endsWith("/")) {
            handle = handle.substring(0, handle.length() - 1);
        }
        return handle.substring(handle.lastIndexOf('/') + 1);
    }

    private static void readAltNames() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/possible.cvs"));
        String s;
        reader.readLine();
        while ((s = reader.readLine()) != null) {
            String[] tokens = s.split("#");
            for (int i = 1; i <= TEAM_NUM; i++) {
                for (int j = 0; j < 3; j++) {
                    if (records[i].contestants[j].getName().equals(tokens[0])) {
                        records[i].contestants[j].addAltName(tokens[1]);
                    }
                }
            }
        }
    }

    private static void readOpenCup() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/opencup.txt"));
        String s;
        while ((s = reader.readLine()) != null) {
            String[] tokens = s.split(";");
            boolean found = false;
            for (int i = 1; i <= TEAM_NUM; i++) {
                Record record = records[i];
                if (record.university.getFullName().equals(tokens[0])) {
                    record.team.setOpenCupPlace(Integer.parseInt(tokens[1]));
                    record.team.setOpenCupTimes(Integer.parseInt(tokens[2]));
                    found = true;
                }
            }
            if (!found) {
                log.error("Can't find " + tokens[0]);
            }
        }
    }

    private static void saveHRForm() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/important.txt"));
        Set<String> important = new HashSet<>();
        String s;
        while ((s = reader.readLine()) != null) {
            important.add(s);
        }
        PrintWriter outImp = new PrintWriter("output/imp.txt");
        PrintWriter out = new PrintWriter("output/all.txt");
        for (int i = 1; i <= TEAM_NUM; i++) {
            records[i].print(out);
            out.println("\f");
            if (important.contains(records[i].university.getFullName())) {
                records[i].print(outImp);
            }
        }
        out.close();
        outImp.close();
    }

    private static void readRegionalChamps() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/regional_champs.txt"));
        String s;
        while ((s = reader.readLine()) != null) {
            for (int i = 1; i <= TEAM_NUM; i++) {
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
        for (int i = 1; i <= TEAM_NUM; i++) {
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
        PrintWriter out = new PrintWriter("output/possible.cvs");
        List<Person> persons = Utils.readList("input/database.json", Person.class);
        int coachesFound = 0;
        int participantsFound = 0;
        for (int i = 1; i <= TEAM_NUM; i++) {
            Record record = records[i];
            boolean coachFound = false;
            for (Person person : persons) {
                if (record.coach.isSamePerson(person)) {
                    record.coach.updateFrom(person);
                    coachFound = true;
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
                    }
                }
                if (found) {
//                    log.info("Data for contestant " + contestant.getName());
                    participantsFound++;
                }
            }
        }
        out.close();
        for (int i = 1; i <= TEAM_NUM; i++) {
            Record record = records[i];
            record.coach.compressAchievements();
            for (Person contestant : record.contestants) {
                contestant.compressAchievements();
            }
        }
        log.info("Personal info integrated");
        log.info("Coaches: " + coachesFound + "/133 found");
        log.info("Participants: " + participantsFound + "/399 found");
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
        for (int i = 1; i <= TEAM_NUM; i++) {
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
                university.setAppearances(1);
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
        University[] universities = new University[TEAM_NUM];
        BufferedReader reader = new BufferedReader(new FileReader("input/short.cvs"));
        for (int i = 0; i < TEAM_NUM; i++) {
            String[] tokens = reader.readLine().split(";");
            universities[i] = new University();
            universities[i].setFullName(tokens[0]);
            universities[i].setShortName(tokens[1]);
            universities[i].setHashTag(tokens[2]);
        }
        for (int i = 1; i <= TEAM_NUM; i++) {
            boolean found = false;
            for (int j = 0; j < TEAM_NUM; j++) {
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
        Record[] pageRecords = new Record[TEAM_NUM + 1];
        PrintWriter badHashes = new PrintWriter("output/bad_hash_tag.txt");
        PrintWriter pw = new PrintWriter("output/mapping");
        for (int i = 1; i <= TEAM_NUM; i++) {
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
            page = page.substring(page.indexOf("Contestants"));
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
            page = page.substring(page.indexOf("http://icpc.baylor.edu/institution/logo/") + "http://icpc.baylor.edu/institution/logo/".length());
            String logoID = page.substring(0, page.indexOf('"'));
            pw.println(logoID + "\t" + i);
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
            page = readPage("input/pages/" + i + "s");
            page = page.substring(page.indexOf("Tweet about #"));
            page = page.substring(13);
            String hashTag = page.substring(0, page.indexOf("#")).trim();
            if (hashTag.isEmpty()) {
                log.error("No hashtag for " + pageRecords[i].university.getFullName());
                badHashes.println(pageRecords[i].university.getFullName());
            }
            pageRecords[i].university.setHashTag(hashTag);
        }
        pw.close();
        badHashes.close();
        for (int i = 1; i <= TEAM_NUM; i++) {
            boolean found = false;
            for (int j = 1; j <= TEAM_NUM; j++) {
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
        for (int i = 1; i <= TEAM_NUM; i++) {
            if (pageRecords[i] != null) {
                log.error("No match for " + pageRecords[i].university.getFullName());
            }
        }
        log.info("MyICPC loaded");
    }

    private static void saveResults() throws Exception {
        PrintWriter out = new PrintWriter("output/all.cvs");
        for (int i = 1; i <= TEAM_NUM; i++) {
            Utils.mapper.writeValue(new File("output/" + i + ".json"), records[i]);
            out.println(records[i].university.getFullName());
            for (int j = 0; j < 3; j++) {
                Person contestant = records[i].contestants[j];
                out.println(contestant.getName() + ";" + (contestant.getCfHandle() != null ? contestant.getCfHandle() : "") + ";" + (contestant.getTcHandle() != null ? contestant.getTcHandle() : ""));
            }
        }
        Utils.mapper.writeValue(new File("output/all.json"), Arrays.asList(Arrays.copyOfRange(records, 1, TEAM_NUM + 1)));
        out.close();
    }

    private static void readIds() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/ids.txt"));
        reader.readLine();
        for (int i = 1; i <= TEAM_NUM; i++) {
            University university = new University();
            university.setFullName(reader.readLine());
            records[i].university.updateFrom(university);
        }
        log.info("ids loaded");
    }

    private static void init() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input/teamdata.csv"));
        for (int i = 1; i <= TEAM_NUM; i++) {
            records[i] = new Record(i);
            String[] data = reader.readLine().split("~");
            records[i].university.setMyIcpcId(data[1]);
            records[i].university.setRegion(getRegionById(data[2]));
            records[i].team.setName(data[3]);
            records[i].university.setFullName(data[4]);
            records[i].university.setShortName(data[5]);
            records[i].university.setUrl(data[6]);
        }
        log.info("Init done");
    }

    private static String getRegionById(String regionId) {
        switch (regionId) {
            case "11742": return "Asia";
            case "11735": return "Europe";
            case "11737": return "Africa and the Middle East";
            case "11738": return "North America";
            case "11744": return "Latin America";
            case "11743": return "South Pacific";
            default: throw new RuntimeException();
        }
    }
}
