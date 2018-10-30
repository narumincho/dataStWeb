package ex3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowHeadings {
    public static void main(String[] args) {
        String urlString;
        if (args.length == 0) {
            urlString = "https://www.dendai.ac.jp/";
        } else {
            urlString = args[0];
        }
        try {
            String html = getHtmlFromUrl(new URL(urlString));
            int i = 0;
            while (true) {
                int pos = html.indexOf("<h", i);
                if (pos == -1) {
                    break;
                }
                i = pos + 2;
                if (!"123456".contains(html.substring(i, i + 1))) {
                    continue;
                }
                int start = html.indexOf(">", i + 1) + 1;
                i = start + 1;
                int end = html.indexOf("</h", i);
                System.out.println(html.substring(start, end));
            }
        } catch (MalformedURLException e) {
            System.err.println("不正なURL: " + urlString);
        }
    }

    static String getHtmlFromUrl(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
            StringBuffer sb = new StringBuffer();
            String b;
            while ((b = br.readLine()) != null) {
                sb.append(b).append("\n");
            }
            return sb.toString();

        } catch (IOException e) {
            System.err.println("入出力エラー: " + e.toString());
            return "";
        }
    }
}
