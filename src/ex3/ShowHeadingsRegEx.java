package ex3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowHeadingsRegEx {
    public static void main(String[] args) {
        String urlString;
        if (args.length == 0) {
            urlString = "https://www.dendai.ac.jp/";
        } else {
            urlString = args[0];
        }
        try {
            String html = getHtmlFromUrl(new URL(urlString));
            Matcher matcher = Pattern.compile("<h\\d.*>(.*)</h\\d>").matcher(html);
            while (matcher.find()) {
                System.out.println(matcher.group(1));
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
