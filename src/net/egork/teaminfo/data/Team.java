package net.egork.teaminfo.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class Team {
    private String name;
    private List<String> regionals;
    private int openCupPlace = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getRegionals() {
        return Collections.unmodifiableList(regionals);
    }

    public void setRegionals(List<String> regionals) {
        this.regionals = regionals;
    }

    public int getOpenCupPlace() {
        return openCupPlace;
    }

    public void setOpenCupPlace(int openCupPlace) {
        this.openCupPlace = openCupPlace;
    }

    public void updateFrom(Team team) {
        if (team.name != null) {
            name = team.name;
        }
        if (team.regionals != null) {
            regionals = team.regionals;
        }
        if (team.openCupPlace != -1) {
            openCupPlace = team.openCupPlace;
        }
    }
}
