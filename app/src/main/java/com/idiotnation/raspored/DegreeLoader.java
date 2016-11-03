package com.idiotnation.raspored;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DegreeLoader extends AsyncTask<Void, Void, Void> {

    onFinihListener onFinishListener;
    Context context;

    public DegreeLoader(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            onFinishListener.onFinish(getDegrees());
        } catch (Exception e) {
            e.printStackTrace();
            onFinishListener.onFinish(null);
        }
        return null;
    }

    public List getDegrees() throws IOException {
        List rasporedUrls = new ArrayList();
        String url = "http://www.fsr.ba/index.php?option=com_content&view=article&id=125&Itemid=1198";
        String matchUrl = "http://intranet.fsr.ba/intranetfsr/teamworks.dll/calendar/";
        try {
            Document doc = Jsoup.connect(url).timeout(3000).get();
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
                    if (link.attr("abs:href").toString().contains(matchUrl)) {
                        rasporedUrls.add(link.attr("abs:href").toString());
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
                    if (link.attr("abs:href").toString().contains(matchUrl)) {
                        rasporedUrls.add(link.attr("abs:href").toString());
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
                    if (link.attr("abs:href").toString().contains(matchUrl)) {
                        rasporedUrls.add(link.attr("abs:href").toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rasporedUrls;
    }

    public void setOnFinishListener(onFinihListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface onFinihListener {
        void onFinish(List list);
    }

}
