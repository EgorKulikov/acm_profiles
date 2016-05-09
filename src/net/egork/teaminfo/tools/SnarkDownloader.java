/*package net.egork.teaminfo.tools;

import net.egork.teaminfo.data.Contestant;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
public class SnarkDownloader {
    public static void main(String[] args) throws Exception {
        String page = loadPage("http://www.snarknews.info/index.cgi?data=all");
        page = page.substring(page.indexOf("<td class=\"maintext\">"));
        int index;
        List<String> ids = new ArrayList<>();
        while ((index = page.indexOf("<tr><td>")) != -1) {
            page = page.substring(index + 8);
            page = page.substring(page.indexOf("data=plr/"));
            index = page.indexOf(".dat");
            String id = page.substring(0, index);
            ids.add(id);
        }
        PrintWriter out = new PrintWriter("input/snark_personal.cvs");
        for (String id : ids) {
            if (id.contains("Andrius_Stankevičius")) {
                continue;
            }
            id = id.replace("'", "_").replace("Artyom_Vasiliev", "Artyom_Vasil_ev");
            page = loadPage("http://www.snarknews.info/index.cgi?" + id + ".dat");
            page = page.substring(page.indexOf("<center><h3>"));
            page = page.substring(page.indexOf("\">") + 2);
            index = page.indexOf('(');
            int other = page.indexOf("<");
            if (index == -1 || other != -1 && index > other) {
                index = other;
            }
            Contestant contestant = new Contestant();
            String name = page.substring(0, index).trim();
            contestant.setName(name);
            index = page.indexOf("On TopCoder:");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                index = page.indexOf("<");
                String tcHandle = page.substring(0, index);
                contestant.setTcHandle(tcHandle);
            }
            index = page.indexOf("On Codeforces:");
            if (index != -1) {
                page = page.substring(index);
                page = page.substring(page.indexOf(">") + 1);
                index = page.indexOf("<");
                String cfHandle = page.substring(0, index);
                contestant.setCfHandle(cfHandle);
            }
            while ((index = page.indexOf("<br>")) != -1) {
                page = page.substring(index + 4);
                index = page.indexOf("<");
                String achievement = page.substring(0, index).trim();
                contestant.addAchievement(achievement);
            }
            out.println(contestant);
            System.out.println(name + " processed");
        }
        out.close();
    }
}
*/