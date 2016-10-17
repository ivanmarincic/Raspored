package com.idiotnation.raspored;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DegreeLoader {

    public List getDegrees() throws IOException {
        List rasporedUrls = new ArrayList();
        String url = "http://www.fsr.ba/index.php?option=com_content&view=article&id=125&Itemid=1198";
        try{
            Document doc = Jsoup.connect(url).get();
            Elements links1 = doc.select("table.raspored_str").select("tbody").select("tr").get(1).select("td");
            Elements links2 = doc.select("table.raspored_rac").select("tbody").get(1).select("tr").get(1).select("td");
            Elements links3 = doc.select("table.raspored_rac").select("tbody").get(3).select("tr").get(1).select("td");
            Elements ps = links1.get(0).select("p");
            ps.addAll(links1.get(1).select("p"));
            for (int j = 0; j < ps.size(); j++) {
                if (ps.get(j).select("a").size() == 0) {
                    rasporedUrls.add("NN");
                } else {
                    Element link = ps.get(j).select("a").get(0);
                    if (link.attr("abs:href").toString().length() >= 32) {
                        if (link.attr("abs:href").toString().substring(0, 32).equals("https://drive.google.com/file/d/")) {
                            rasporedUrls.add(link.attr("abs:href").toString());
                        }
                    }
                }
            }
            ps.clear();
            ps = links2.get(0).select("p");
            ps.addAll(links2.get(1).select("p"));
            for (int j = 0; j < ps.size(); j++) {
                if (ps.get(j).select("a").size() == 0) {
                    rasporedUrls.add("NN");
                } else {
                    Element link = ps.get(j).select("a").get(0);
                    if (link.attr("abs:href").toString().length() >= 32) {
                        if (link.attr("abs:href").toString().substring(0, 32).equals("https://drive.google.com/file/d/")) {
                            rasporedUrls.add(link.attr("abs:href").toString());
                        }
                    }
                }
            }
            ps.clear();
            ps = links3.get(0).select("p");
            for (int j = 0; j < ps.size(); j++) {
                if (ps.get(j).select("a").size() == 0) {
                    rasporedUrls.add("NN");
                } else {
                    Element link = ps.get(j).select("a").get(0);
                    if (link.attr("abs:href").toString().length() >= 32) {
                        if (link.attr("abs:href").toString().substring(0, 32).equals("https://drive.google.com/file/d/")) {
                            rasporedUrls.add(link.attr("abs:href").toString());
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return rasporedUrls;
    }
}
