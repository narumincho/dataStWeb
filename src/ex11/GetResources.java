package ex11;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetResources {
    static public void main(String[] args) {
        try {
            org.w3c.dom.Document xml = getXmlDocumentFromUrl(new URL("http://rss.itmedia.co.jp/rss/2.0/news_products.xml"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList itemNodeList = (NodeList) xPath.evaluate("//item", xml, XPathConstants.NODESET);

            List<Article> articleList = new ArrayList<>();
            for (int i = 0; i < itemNodeList.getLength(); i++) {
                Node itemNode = itemNodeList.item(i);

                String title = (String) xPath.evaluate("title/text()", itemNode, XPathConstants.STRING);
                String description = (String) xPath.evaluate("description/text()", itemNode, XPathConstants.STRING);
                String link = (String) xPath.evaluate("link/text()", itemNode, XPathConstants.STRING);
                Document document = Jsoup.connect(link).get();
                Elements pElements = document.getElementsByTag("p");
                List<YenAndParagraph> yenAndParagraphList = new ArrayList<>();
                for (Element element : pElements) {
                    String text = element.text();
                    Matcher priceString = Pattern.compile("(\\d+)円").matcher(text);
                    while (priceString.find()) {
                        yenAndParagraphList.add(new YenAndParagraph(Integer.parseInt(priceString.group(1)), text));
                    }
                }
                articleList.add(new Article(title, description, yenAndParagraphList));
            }
            System.out.println(articleList);

        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
        }
    }

    static private org.w3c.dom.Document getXmlDocumentFromUrl(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS implementation = (DOMImplementationLS) registry.getDOMImplementation("XML 1.0");
            // 読み込み対象の用意
            LSInput input = implementation.createLSInput();
            input.setByteStream(inputStream);
            input.setEncoding("UTF-8");
            // 構文解析器(parser)の用意
            LSParser parser = implementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            parser.getDomConfig().setParameter("namespaces", false);
            // DOMの構築
            return parser.parse(input);

        } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class Article {
        private String title;
        private String description;
        private List<YenAndParagraph> yenAndParagraphList;

        public Article(String title, String description, List<YenAndParagraph> yenAndParagraphList) {
            this.title = title;
            this.description = description;
            this.yenAndParagraphList = yenAndParagraphList;
        }

        @Override
        public String toString() {
            return "title:" + this.title + "\n"
                    + "description:" + this.description + "\n"
                    + "yenAndParagraphList:" + yenAndParagraphList + "\n";
        }
    }

    static class YenAndParagraph {
        private int price;
        private String paragraph;

        public YenAndParagraph(int price, String paragraph) {
            this.price = price;
            this.paragraph = paragraph;
        }

        public int getPrice() {
            return price;
        }

        public String getParagraph() {
            return paragraph;
        }

        @Override
        public String toString() {
            return "yen:" + this.price + "\n"
                    + "paragraph:" + this.paragraph + "\n";
        }
    }
}
