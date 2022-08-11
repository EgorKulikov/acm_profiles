package net.egork.teaminfo.data;

import com.fasterxml.jackson.databind.JsonNode;
import net.egork.teaminfo.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author egor@egork.net
 */
public class CodeforcesUser {
    static Log log = LogFactory.getLog(CodeforcesUser.class);

    private String handle;
    private String firstName;
    private String lastName;
    private int rating;

    public static List<Person> getCodeforcesUsers() throws IOException {
        String apiRequest = "user.ratedList?activeOnly=false";
        JsonNode node = Utils.codeforcesApiRequest(apiRequest);
        java.util.Iterator<JsonNode> elements = node.elements();
        List<CodeforcesUser> users = new ArrayList<>();
        log.info("Read: " + node.size());
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            CodeforcesUser user = new CodeforcesUser();
            JsonNode handle = next.get("handle");
            if (handle != null && !handle.asText().equals("gsahil")
                    && !handle.asText().equals("youssseeef") && !handle.asText().equals("kryptox")
                    && !handle.asText().equals("abacadaea")) {
                user.handle = handle.asText();
            }
            JsonNode firstName = next.get("firstName");
            if (firstName != null) {
                user.firstName = firstName.asText();
            }
            JsonNode lastName = next.get("lastName");
            if (lastName != null) {
                user.lastName = lastName.asText();
            }
            JsonNode rating = next.get("rating");
            if (rating != null) {
                user.rating = rating.asInt();
            }
            users.add(user);
        }
        List<Person> persons = new ArrayList<>();
        Map<String, Person> byHandle = new HashMap<>();
        for (CodeforcesUser user : users) {
            Person person = new Person().setName(getName(user)).setCfHandle(user.handle).setCfRating(user.rating);
            if (user.firstName != null && user.lastName != null) {
                person.addAltName(user.lastName + " " + user.firstName);
            }
            persons.add(person);
            byHandle.put(person.getCfHandle(), person);
        }
//        addCodeforcesAchievements(byHandle);
        return persons;
    }

    public static void addCodeforcesAchievements() throws IOException {
        Map<String, Person> byHandle = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("input/codeforces_contest.cvs"));
        reader.readLine();
        String s;
        while ((s = reader.readLine()) != null) {
            String[] tokens = s.split(";");
            String id = tokens[0];
            String name = tokens[1];
            String top3Priority = tokens[2];
            String otherPriority = tokens[3];
            log.info("Starting to process contest " + id);
            JsonNode contest;
            while (true) {
                try {
                    contest = Utils.codeforcesApiRequest("contest.standings?contestId=" + id + (otherPriority.equals
                            ("0") ? "&from=1&count=3" : ""));
                    break;
                } catch (Throwable e) {
                    log.warn("Throttled, waiting");
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            contest = contest.get("rows");
            Iterator<JsonNode> iterator = contest.elements();
            while (iterator.hasNext()) {
                JsonNode row = iterator.next();
                JsonNode party = row.get("party");
                JsonNode members = party.get("members");
                int rank = row.get("rank").asInt();
                Iterator<JsonNode> memberIterator = members.elements();
                while (memberIterator.hasNext()) {
                    String handle = memberIterator.next().get("handle").asText();
                    Person person = byHandle.get(handle);
                    if (person == null) {
                        person = new Person().setCfHandle(handle);
                        byHandle.put(handle, person);
                    }
                    person.addAchievement(new Achievement(name + " " + (rank <= 3 ? rank + Utils.appropriateEnd
                            (rank) : "finalist"), Integer.parseInt(tokens[4]), rank <= 3 ? Integer.parseInt
                            (top3Priority) + (3 - rank) * 5 : Integer.parseInt(otherPriority)));
                }
            }
        }
        Utils.mapper.writeValue(new File("input/codeforces_achivements.json"), new ArrayList<>(byHandle.values()));
    }

    private static String getName(CodeforcesUser user) {
        if (user.firstName == null) {
            return user.lastName;
        } else if (user.lastName == null) {
            return user.firstName;
        } else {
            return user.firstName + " " + user.lastName;
        }
    }

    public static void main(String... args) throws IOException {
        Utils.mapper.writeValue(new File("input/codeforces.json"), getCodeforcesUsers());
        log.info("codeforces loaded");
    }
}
