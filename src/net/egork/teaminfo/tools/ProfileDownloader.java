package net.egork.teaminfo.tools;

import net.egork.teaminfo.GenerateInfo;

import java.io.PrintWriter;

import static net.egork.teaminfo.Utils.loadPage;

/**
 * @author egor@egork.net
 */
public class ProfileDownloader {
    public static void main(String... args) throws Exception {
        String page = loadPage("http://myicpc.icpcnews.com/World-Finals-2017/teams");
        for (int i = 1; i <= GenerateInfo.TEAM_NUM; i++) {
            int index = page.indexOf("Team profile");
            String before = page.substring(0, index);
            page = page.substring(index + 1);
            int href = before.lastIndexOf("href=\"");
            before = before.substring(href + 6);
            String end = before.substring(0, before.indexOf("\""));
            end = end.substring(0, end.indexOf("/profile"));
            String link = "http://myicpc.icpcnews.com" + end;
            String content = loadPage(link);
            PrintWriter out = new PrintWriter("input/pages/" + i);
            out.print(content);
            out.close();
            link = "http://myicpc.icpcnews.com" + end + "/social";
            content = loadPage(link);
            out = new PrintWriter("input/pages/" + i + "s");
            out.print(content);
            out.close();
            System.out.println(i + " ready!");
        }
    }
}
