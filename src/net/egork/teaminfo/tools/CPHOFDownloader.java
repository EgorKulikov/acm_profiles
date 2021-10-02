package net.egork.teaminfo.tools;

import net.egork.teaminfo.data.Achievement;
import net.egork.teaminfo.data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.egork.teaminfo.Utils.mapper;

/**
 * @author egor@egork.net
 */
public class CPHOFDownloader {
    static Log log = LogFactory.getLog(CPHOFDownloader.class);

    public static void main(String... args) throws Exception {
        List<Map> countries = mapper.readValue(
                new URL("https://cphof.org/countriesdata"),
                List.class
        );
        List<String> countriesURL = new ArrayList<>();
        for (Map country : countries) {
            String s = (String) country.get("name");
            int k = s.indexOf("\" class=") - 3;
            String url = s.substring(k, k + 3);
            countriesURL.add(url);
        }

        List<Person> persons = new ArrayList<>();

        try {
            for (String url : countriesURL) {
                System.out.println(url);
                List<Map> people = mapper.readValue(
                        new URL("https://cphof.org/countrydata/" + url),
                        List.class
                );
                for (Map person : people) {
                    String s = (String) person.get("name");
                    String url2 = "https://cphof.org/profile/" +
                            encode(
                            s.substring(18, s.indexOf("\">"))
                            );
                    persons.add(loadPerson(url2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapper.writeValue(new File("input/cphof.json"), persons);
    }

    private static String encode(String s) {
        String res = "";
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c) || Character.isLetter(c)) {
                res += c;
            } else {
                res += "%" + Integer.toHexString(c);
            }
        }
        return res;
    }

    private static Person loadPerson(String url) throws Exception {
        System.out.println(url);
        Person person = new Person();
        Document doc;
        int tries = 0;
        while (true) {
            try {
                doc = Jsoup.connect(url).get();
                break;
            } catch (Exception e) {
                tries++;
                if (tries > 100) {
                    throw e;
                }
            }
        }
        Element main = doc.selectFirst("#content-wrapper");
        Element name = main.selectFirst("h3");
        person.setName(name.text());
        System.out.println("name: " + person.getName());
        Element profiles = main.select(".text-nowrap").get(1);
        for (Element child : profiles.children()) {
            Elements a = child.children();
            if (a.isEmpty()) continue;
            String s = a.last().text();
            if (s.endsWith(" at Codeforces")) {
                person.setCfHandle(s.substring(0,
                        s.length() - " at Codeforces".length()));
                System.out.println("cf: " + person.getCfHandle());
            }
            if (s.endsWith(" at Topcoder")) {
                person.setTcHandle(s.substring(0,
                        s.length() - " at Topcoder".length()));
                System.out.println("tc: " + person.getTcHandle());
            }
        }
        Element table = main.selectFirst("#timelineTable");
        Element head = table.selectFirst("tr");
        List<Integer> years = new ArrayList<>();
        for (int i = 1; i < head.children().size(); i++) {
            years.add(Integer.parseInt(head.children().get(i).text()));
        }
        Elements rows = table.select("tr");
        double multiplier = 1;
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            if (row.children().size() > 1) {
                String contestName = row.selectFirst("td").text();
                if (contestName.equals("ICPC World Finals")) continue;

                Elements a = row.select("td");
                for (int j = 0; j < a.size() - 1; j++) {
                    int year = years.get(j);
                    Element td = a.get(j + 1);
                    String s = td.text().trim();
                    if (s.isEmpty() || s.equals("-")) continue;

                    if (contestName.equals("IOI")) {
                        try {
                            String mdl = td.selectFirst("img").attr("title");
                            int p = 0;
                            if (mdl.startsWith("Gold")) {
                                p = 100;
                            }
                            if (mdl.startsWith("Silver")) {
                                p = 80;
                            }
                            if (mdl.startsWith("Bronze")) {
                                p = 60;
                            }
                            person.addAchievement(new Achievement(
                                    contestName + " " + mdl, year, p
                            ));
                        } catch (Exception e) {
                        }
                        continue;
                    }


                    if (s.equals("1")) {
                        person.addAchievement(new Achievement(
                                contestName + " Champion", year, (int) (100 * multiplier)
                        ));
                    } else if (s.equals("2")) {
                        person.addAchievement(new Achievement(
                                contestName + " 2nd", year, (int) (80 * multiplier)
                        ));
                    } else if (s.equals("3")) {
                        person.addAchievement(new Achievement(
                                contestName + " 3rd", year, (int) (60 * multiplier)
                        ));
                    } else {
                        boolean number = true;
                        try {
                            Integer.parseInt(s);
                        } catch (Exception e) {
                            number = false;
                        }
                        if (number) {
                            person.addAchievement(new Achievement(
                                    contestName + " Finalist", year, (int) (50 * multiplier)
                            ));
                        }
                    }

                }
            } else {
                if (row.text().trim().equals("Regional Contests:")) {
                    multiplier = .7;
                }
                if (row.text().trim().equals("Local Contests:")) {
                    multiplier = .5;
                }
            }
        }
        return person;
    }
}
