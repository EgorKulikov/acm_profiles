package net.egork.teaminfo.data;

import java.io.PrintWriter;

/**
 * @author egor@egork.net
 */
public class Record {
    public final int id;
    public final University university;
    public final Team team;
    public final Person coach;
    public final Person[] contestants = new Person[3];

    public Record(int id) {
        this.id = id;
        university = new University();
        team = new Team();
        coach = new Person();
        for (int i = 0; i < 3; i++) {
            contestants[i] = new Person();
        }
    }

    public void print(PrintWriter out) {
        out.println("University: " + university.getFullName());
        out.println("Team: " + team.getName());
        out.println();
        out.println("Coach:");
        printPerson(coach, out);
        out.println();
        for (int i = 0; i < 3; i++) {
            out.println("Contestant " + (i + 1) + ":");
            printPerson(contestants[i], out);
            out.println();
        }
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
    }

    private void printPerson(Person person, PrintWriter out) {
        out.println("Name: " + person.getName());
        out.println("TC handle: " + person.getTcHandle());
        out.println("CF handle: " + person.getCfHandle());
        out.println("Achievements:");
        for (Achievement achievement : person.getAchievements()) {
            out.println(achievement.achievement);
        }
    }
}
