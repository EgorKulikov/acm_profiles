package net.egork.teaminfo.tools;

import java.io.PrintWriter;

import static net.egork.teaminfo.Utils.*;

/**
 * @author egor@egork.net
 */
public class PersonalProfileDownloader {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 128; i++) {
            String page = readPage("input/pages/" + i);
            page = page.substring(page.indexOf("Contestants"));
            for (int j = 0; j < 3; j++) {
                page = page.substring(page.indexOf("people/") + 7);
                String id = page.substring(0, page.indexOf("\""));
                String profile = loadPage("http://myicpc.icpcnews.com/World-Finals-2016/people/" + id);
                PrintWriter out = new PrintWriter("input/persons/" + id);
                out.print(profile);
                out.close();
            }
            System.out.println(i + " done");
        }
    }
}
