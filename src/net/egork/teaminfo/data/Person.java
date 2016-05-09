package net.egork.teaminfo.data;

import net.egork.teaminfo.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class Person {
    private String name;
    private List<String> altNames = new ArrayList<>();
    private String tcHandle;
    private int tcRating = -1;
    private String cfHandle;
    private int cfRating = -1;
    private List<Achievement> achievements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<String> getAltNames() {
        return Collections.unmodifiableList(altNames);
    }

    public String getTcHandle() {
        return tcHandle;
    }

    public int getTcRating() {
        return tcRating;
    }

    public String getCfHandle() {
        return cfHandle;
    }

    public int getCfRating() {
        return cfRating;
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(achievements);
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Person setTcHandle(String tcHandle) {
        this.tcHandle = tcHandle;
        return this;
    }

    public Person setTcRating(int tcRating) {
        this.tcRating = tcRating;
        return this;
    }

    public Person setCfHandle(String cfHandle) {
        this.cfHandle = cfHandle;
        return this;
    }

    public Person setCfRating(int cfRating) {
        this.cfRating = cfRating;
        return this;
    }

    public Person addAltName(String name) {
        altNames.add(name);
        return this;
    }

    public Person addAchievement(Achievement achievement) {
        achievements.add(achievement);
        Collections.sort(achievements);
        return this;
    }

    public boolean isCompatible(Person other) {
        return Utils.compatible(tcHandle, other.tcHandle) && Utils.compatible(cfHandle, other.cfHandle);
    }

    public void updateFrom(Person other) {
        if (name == null || name.equals(other.name)) {
            name = other.name;
        } else {
            if (!altNames.contains(other.name)) {
                altNames.add(other.name);
            }
        }
        for (String altName : other.altNames) {
            if (!altNames.contains(altName)) {
                altNames.add(altName);
            }
        }
        if (other.tcHandle != null) {
            tcHandle = other.tcHandle;
        }
        if (other.tcRating != -1) {
            tcRating = other.tcRating;
        }
        if (other.cfHandle != null) {
            cfHandle = other.cfHandle;
        }
        if (other.cfRating != -1) {
            cfRating = other.cfRating;
        }
        achievements.addAll(other.achievements);
        Collections.sort(achievements);
    }

    public boolean isSamePerson(Person other) {
        if (name != null) {
            if (name.equals(other.name) || other.altNames.contains(name)) {
                return true;
            }
        }
        for (String name : altNames) {
            if (name.equals(other.name) || other.altNames.contains(name)) {
                return true;
            }
        }
        if (tcHandle != null && tcHandle.equals(other.tcHandle)) {
            return true;
        }
        return cfHandle != null && cfHandle.equals(other.cfHandle);
    }
}
