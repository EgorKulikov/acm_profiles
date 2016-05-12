package net.egork.teaminfo.tools;

import net.egork.teaminfo.Utils;
import net.egork.teaminfo.data.CodeforcesUser;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author egor@egork.net
 */
public class PersonalDatabase {
    static Log log = LogFactory.getLog(PersonalDatabase.class);

    static Map<String, Person> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static Map<String, Person> byTc = new HashMap<>();
    static Map<String, Person> byTcId = new HashMap<>();
    static Map<String, Person> byCf = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void main(String... args) throws Exception {
        if (Boolean.getBoolean("reloadCodeforcesUsers")) {
            CodeforcesUser.main();
        }
        add("input/codeforces.json");
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
        if (Boolean.getBoolean("reloadTopCoder")) {
            TopCoderDownloader.main();
        }
        add("input/topcoder.json");
        if (Boolean.getBoolean("reloadWFData")) {
            WFData.main();
        }
        add("input/wf.json");
        log.info("TopCoder Processed");
        add("input/corrections.json");
        saveDatabase();
        log.info("Database created");
    }

    private static void add(String filename) throws Exception {
        List<Person> persons = Utils.readList(filename, Person.class);
        for (Person person : persons) {
            boolean good = true;
            if (person.getName() != null && byName.containsKey(person.getName()) && !byName.get(person.getName())
                    .isCompatible(person)) {
                good = false;
            }
            for (String name : person.getAltNames()) {
                if (byName.containsKey(name) && !byName.get(name).isCompatible(person)) {
                    good = false;
                }
            }
            if (person.getTcHandle() != null && byTc.containsKey(person.getTcHandle()) &&
                    !byTc.get(person.getTcHandle()).isCompatible(person)) {
                good = false;
            }
            if (person.getCfHandle() != null && byCf.containsKey(person.getCfHandle()) &&
                    !byCf.get(person.getCfHandle()).isCompatible(person)) {
                good = false;
            }
            if (!good) {
                log.info("Something fishy with " + person.getName() + " " + person.getTcHandle() + " " + person.getCfHandle());
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
            if (person.getCfHandle() != null && byTc.containsKey(person.getCfHandle())) {
                Person current = byTc.get(person.getCfHandle());
                if (!added.contains(current) && current.isCompatible(person)) {
                    person.updateFrom(current);
                    added.add(current);
                }
            }
            if (person.getTcHandle() != null && byCf.containsKey(person.getTcHandle())) {
                Person current = byCf.get(person.getTcHandle());
                if (!added.contains(current) && current.isCompatible(person)) {
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
        Utils.mapper.writeValue(new File("input/compressed_database.json"), persons);
    }
}
