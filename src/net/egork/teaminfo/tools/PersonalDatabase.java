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

    static Map<String, Person> byName = new HashMap<>();
    static Map<String, Person> byTc = new HashMap<>();
    static Map<String, Person> byCf = new HashMap<>();

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
            if (person.getName() != null && byName.containsKey(person.getName())) {
                person.updateFrom(byName.get(person.getName()));
            }
            List<String> altNames = new ArrayList<>(person.getAltNames());
            for (String name : altNames) {
                if (byName.containsKey(name)) {
                    person.updateFrom(byName.get(name));
                }
            }
            if (person.getTcHandle() != null && byTc.containsKey(person.getTcHandle())) {
                person.updateFrom(byTc.get(person.getTcHandle()));
            }
            if (person.getCfHandle() != null && byCf.containsKey(person.getCfHandle())) {
                person.updateFrom(byCf.get(person.getCfHandle()));
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
    }
}
