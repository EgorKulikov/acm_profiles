package net.egork.teaminfo.data;

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
}
