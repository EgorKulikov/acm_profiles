package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.CodeforcesUser;
import net.egork.teaminfo.data.Person;
import net.egork.teaminfo.data.Record;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

import static net.egork.teaminfo.GenerateInfo.TEAM_NUM;

/**
 * @author egor@egork.net
 */
public class PersonalDatabase {
    static Log log = LogFactory.getLog(PersonalDatabase.class);

    static Map<String, Person> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static Map<String, Person> byTc = new HashMap<>();
    static Map<String, Person> byTcId = new HashMap<>();
    static Map<String, Person> byCf = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static Map<String, Person> byIOIId = new HashMap<>();

    public static void main(String... args) throws Exception {
        if (Boolean.getBoolean("reloadCodeforcesUsers")) {
            CodeforcesUser.main();
        }
        add("input/codeforces.json", true);
        if (Boolean.getBoolean("reloadTopCoder")) {
            TopCoderDownloader.main();
        }
        add("input/topcoder.json");
        log.info("TopCoder Processed");
//        addSnarkCorrection();
//        add("input/corrections.json");

        if (Boolean.getBoolean("reloadCodeforcesAchievements")) {
            CodeforcesUser.addCodeforcesAchievements();
        }
        add("input/codeforces_achivements.json");
        log.info("Codeforces processed");
        if (Boolean.getBoolean("reloadIOI")) {
            IOIDownloader.main();
        }
        add("input/ioi.json");
        log.info("IOI Processed");
        if (Boolean.getBoolean("reloadWFData")) {
            WFData.main();
        }
        add("input/wf_new.json");
        log.info("WF Processed");
        addOldWf();
        if (Boolean.getBoolean("reloadSnarknews")) {
            SnarkDownloader.main();
        }
        add("input/snark.json");
        log.info("Snark Processed");
        linkTcCf();
        saveDatabase();
        log.info("Database created");
    }

    private static void addOldWf() throws Exception {
        List<Person> persons = Utils.readList("input/old_wf.json", Person.class);
        List<Person> filtered = new ArrayList<>();
        for (Person person : persons) {
            Achievement achievement = person.getAchievements().get(0);
            if (achievement.year <= 2008 && achievement.achievement.toLowerCase().contains("coach")) {
                filtered.add(person);
            }
        }
        add(false, filtered);
    }

    private static void addSnarkCorrection() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input/import_snark.csv"));
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < TEAM_NUM * 5; i++) {
            String s = reader.readLine();
            if (s == null || i % 5 == 0 || i % 5 == 4) {
                continue;
            }
            String[] data = s.split(";", -1);
            Person person = new Person();
            person.setName(data[0]);
            if (!data[1].isEmpty()) {
                if (data[1].startsWith("0-")) {
                    person.setTcHandle(data[1].substring(2));
                } else {
                    person.setTcHandle(data[1]);
                }
            }
            if (!data[2].isEmpty()) {
                if (data[2].startsWith("0-")) {
                    person.setCfHandle(data[2].substring(2));
                } else {
                    person.setCfHandle(data[2]);
                }
            }
            for (int j = 3; j < data.length; j++) {
                if (data[j].startsWith("IOI")) {
                    person.setIoiID(data[j].split(":")[1]);
                }
            }
            persons.add(person);
        }
        add(false, persons);
    }

    private static void linkTcCf() {
        for (Person person : new HashSet<>(byTc.values())) {
            if (byCf.containsKey(person.getTcHandle())) {
                Person match = byCf.get(person.getTcHandle());
                if (match != person && match.isCompatible(person)) {
                    person.updateFrom(match);
                    if (person.getName() != null) {
                        byName.put(person.getName(), person);
                    }
                    for (String name : person.getAltNames()) {
                        byName.put(name, person);
                    }
                    if (person.getTcHandle() != null) {
                        byTc.put(person.getTcHandle(), person);
                    }
                    if (person.getTcId() != null) {
                        byTcId.put(person.getTcId(), person);
                    }
                    if (person.getCfHandle() != null) {
                        byCf.put(person.getCfHandle(), person);
                    }
                }
            }
        }
    }

    private static void add(String filename) throws Exception {
        add(filename, false);
    }

    private static void add(String filename, boolean ninetyThreePercentMoreSkipableThanYouThink) throws Exception {
        List<Person> persons = Utils.readList(filename, Person.class);
        add(ninetyThreePercentMoreSkipableThanYouThink, persons);
    }

    private static void add(boolean ninetyThreePercentMoreSkipableThanYouThink, List<Person> persons) {
        for (Person person : persons) {
            Person remake = new Person();
            remake.updateFrom(person);
            person = remake;
            if (!ninetyThreePercentMoreSkipableThanYouThink && person.getCfHandle() != null && byCf.get(person.getCfHandle()) == null) {
                person.setCfHandle(null);
            }
            boolean good = true;
            Set<Person> incompatible = new HashSet<>();
            if (person.getName() != null && byName.containsKey(person.getName()) && !byName.get(person.getName())
                    .isCompatible(person)) {
                incompatible.add(byName.get(person.getName()));
                good = false;
            }
            for (String name : person.getAltNames()) {
                if (byName.containsKey(name) && !byName.get(name).isCompatible(person)) {
                    incompatible.add(byName.get(name));
                    good = false;
                }
            }
            if (person.getTcHandle() != null && byTc.containsKey(person.getTcHandle()) &&
                    !byTc.get(person.getTcHandle()).isCompatible(person)) {
                incompatible.add(byTc.get(person.getTcHandle()));
                good = false;
            }
            if (person.getCfHandle() != null && byCf.containsKey(person.getCfHandle()) &&
                    !byCf.get(person.getCfHandle()).isCompatible(person)) {
                incompatible.add(byCf.get(person.getCfHandle()));
                good = false;
            }
            if (!good) {
                if (!ninetyThreePercentMoreSkipableThanYouThink) {
                    String message = "Something fishy with " + person.getName() + " " + person.getTcHandle() + " " + person.getCfHandle() + " " + person.getAltNames();
                    for (Person other : incompatible) {
                        message += ";" + other.getName() + " " + other.getTcHandle() + " " + other.getCfHandle() + " " + other.getAltNames();
                    }
                    log.info(message);
                }
                continue;
            }
            Set<Person> added = new HashSet<>();
            if (person.getName() != null && byName.containsKey(person.getName())) {
                Person current = byName.get(person.getName());
                if (!added.contains(current)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            List<String> altNames = new ArrayList<>(person.getAltNames());
            for (String name : altNames) {
                if (byName.containsKey(name)) {
                    Person current = byName.get(name);
                    if (!added.contains(current)) {
                        person.updateFrom(current);
                        added.add(current);
                    }
                }
            }
            if (person.getTcHandle() != null && byTc.containsKey(person.getTcHandle())) {
                Person current = byTc.get(person.getTcHandle());
                if (!added.contains(current)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            if (person.getTcId() != null && byTcId.containsKey(person.getTcId())) {
                Person current = byTcId.get(person.getTcId());
                if (!added.contains(current)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            if (person.getCfHandle() != null && byCf.containsKey(person.getCfHandle())) {
                Person current = byCf.get(person.getCfHandle());
                if (!added.contains(current)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            if (person.getIoiID() != null && byIOIId.containsKey(person.getIoiID())) {
                Person current = byIOIId.get(person.getIoiID());
                if (!added.contains(current)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            if (person.getName() != null) {
                byName.put(person.getName(), person);
            }
            for (String name : person.getAltNames()) {
                byName.put(name, person);
            }
            if (person.getTcHandle() != null) {
                byTc.put(person.getTcHandle(), person);
            }
            if (person.getTcId() != null) {
                byTcId.put(person.getTcId(), person);
            }
            if (person.getCfHandle() != null) {
                byCf.put(person.getCfHandle(), person);
            }
            if (person.getIoiID() != null) {
                byIOIId.put(person.getIoiID(), person);
            }
        }
    }

    private static void saveDatabase() throws IOException {
        Set<Person> allPersons = new HashSet<>();
        allPersons.addAll(byName.values());
        allPersons.addAll(byTc.values());
        allPersons.addAll(byCf.values());
        List<Person> persons = new ArrayList<>(allPersons);
        Utils.mapper.writeValue(new File("input/database.json"), persons);
        persons.stream().forEach(Person::compressAchievements);
        Utils.mapper.writeValue(new File("output/compressed_database.json"), persons);
        PrintWriter out = new PrintWriter("output/personal.txt");
        for (Person person : persons) {
            Record.printPerson(person, out);
            out.println();
        }
        out.close();
    }
}
