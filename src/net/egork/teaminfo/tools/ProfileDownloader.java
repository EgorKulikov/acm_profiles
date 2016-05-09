package net.egork.teaminfo.tools;

import java.io.PrintWriter;

import static net.egork.teaminfo.Utils.loadPage;

/**
 * @author egor@egork.net
 */
public class ProfileDownloader {
    public static void main(String... args) throws Exception {
        String page = loadPage("http://myicpc.icpcnews.com/World-Finals-2016/teams");
        for (int i = 1; i <= 128; i++) {
            int index = page.indexOf("Team profile");
            String before = page.substring(0, index);
            page = page.substring(index + 1);
            int href = before.lastIndexOf("href=\"");
            before = before.substring(href + 6);
            String link = "http://myicpc.icpcnews.com" + before.substring(0, before.indexOf("\""));
            String content = loadPage(link);
            PrintWriter out = new PrintWriter("input/pages/" + i);
            out.print(content);
            out.close();
            System.out.println(i + " ready!");
        }
    }
}
