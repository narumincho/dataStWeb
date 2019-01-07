package tan;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetResources {
    public static void main(String[] args) {
        try {
            // Amazonから取得
            Document amazonRss = getRssDocumentFromUrl(new URL("https://www.amazon.co.jp/gp/rss/bestsellers/watch/ref=zg_bs_watch_rsslink"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList amazonRssItemList = (NodeList) xPath.evaluate("//item", amazonRss, XPathConstants.NODESET);
            List<Item> amazonExistModelNumberItemList = new ArrayList<>();
            List<Item> rakutenExistModelNumberItemList = new ArrayList<>();


            System.out.println("amazonの腕時計の中で最も人気がある商品(1時間更新)");
            for (int i = 0; i < amazonRssItemList.getLength(); i++) {
                Node itemNode = amazonRssItemList.item(i);
                String title = (String) xPath.evaluate("title/text()", itemNode, XPathConstants.STRING);
                System.out.println(title);
                URL link = new URL((String) xPath.evaluate("link/text()", itemNode, XPathConstants.STRING));
                String modelNumber;
                if ((modelNumber = getModelNumber(title)) != null) {
                    amazonExistModelNumberItemList.add(
                            new Item(title, modelNumber, link)
                    );
                }
            }
            System.out.println();

            // 楽天から取得
            Document rakutenRss = getRssDocumentFromUrl(new URL("https://ranking.rakuten.co.jp/rss/daily/558929"));
            NodeList rakutenRssItemList = (NodeList) xPath.evaluate("//item", rakutenRss, XPathConstants.NODESET);
            System.out.println("楽天の人気腕時計ランキング");
            for (int i = 0; i < rakutenRssItemList.getLength(); i++) {
                Node itemNode = rakutenRssItemList.item(i);
                String title = (String) xPath.evaluate("title/text()", itemNode, XPathConstants.STRING);
                System.out.println(title);
                URL link = new URL((String) xPath.evaluate("link/text()", itemNode, XPathConstants.STRING));
//                System.out.println(title);
                String modelNumber;
                if ((modelNumber = getModelNumber(title)) != null) {
                    rakutenExistModelNumberItemList.add(
                            new Item(title, modelNumber, link)
                    );
                }
            }
            System.out.println();

            System.out.println("型番が一致したもの");
            boolean isExistSameModelNumber = false;
            for (Item amazonItem : amazonExistModelNumberItemList) {
                for (Item rakutenItem : rakutenExistModelNumberItemList) {
                    if (amazonItem.equalModelNumber(rakutenItem)) {
                        isExistSameModelNumber = true;
                        System.out.println("型番:" + amazonItem.modelNumber);
                        System.out.println("amazonのタイトル:" + amazonItem.title);
                        System.out.println("amazonのリンク:" + amazonItem.link);
                        System.out.println("楽天のタイトル:" + rakutenItem.title);
                        System.out.println("楽天のリンク:" + rakutenItem.link + "\n");
                    }
                }
            }
            if (!isExistSameModelNumber) {
                System.out.println("一致したものがない");
            }

        } catch (MalformedURLException | XPathExpressionException e) {
            e.printStackTrace();
        }

    }

    public static Document getRssDocumentFromUrl(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            // amazonのRSS取得にはUserAgentが必要
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            DOMImplementationLS implementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");

            // 読み込み対象の設定
            LSInput input = implementation.createLSInput();
            input.setByteStream(inputStream);
            input.setEncoding("UTF-8");

            // 構文解析器の生成
            LSParser parser = implementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            parser.getDomConfig().setParameter("namespaces", false);
            // DOMの構築
            Document resultDocument = parser.parse(input);
            return resultDocument;

        } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getModelNumber(String text) {
        Matcher modelNumberMatcher = Pattern.compile("\\w+-+[\\w-]+").matcher(text);
        String modelNumber = null;
        if (modelNumberMatcher.find()) {
            modelNumber = modelNumberMatcher.group();
        }
        return modelNumber;
    }

    static class Item {
        public String title;
        public String modelNumber;
        public URL link;

        Item(String title, String modelNumber, URL link) {
            this.title = title;
            this.modelNumber = modelNumber;
            this.link = link;
        }

        boolean equalModelNumber(Item item) {
            return this.modelNumber.equals(item.modelNumber);
        }
    }
}
