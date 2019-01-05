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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetResources {
    static public void main(String[] args) {
        try {
            org.w3c.dom.Document xml = getXmlDocumentFromUrl(new URL("http://rss.itmedia.co.jp/rss/2.0/news_products.xml"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList itemNodeList = (NodeList) xPath.evaluate("//item", xml, XPathConstants.NODESET);

            List<PriceWithParagraphAndArticle> priceWithParagraphAndArticleList = new ArrayList<>();

            for (int i = 0; i < itemNodeList.getLength(); i++) {
                Node itemNode = itemNodeList.item(i);

                String title = (String) xPath.evaluate("title/text()", itemNode, XPathConstants.STRING);
                String description = (String) xPath.evaluate("description/text()", itemNode, XPathConstants.STRING);
                String link = (String) xPath.evaluate("link/text()", itemNode, XPathConstants.STRING);
                Document document = Jsoup.connect(link).get();
                Elements pElements = document.getElementsByTag("p");

                for (Element element : pElements) {
                    String text = element.text();
                    Matcher priceString = Pattern.compile("([\\d,千万億]+)円").matcher(text);
                    while (priceString.find()) {
                        priceWithParagraphAndArticleList.add(
                                new PriceWithParagraphAndArticle(
                                        parseInt(priceString.group(1)),
                                        text,
                                        title,
                                        description,
                                        new URL(link)
                                )
                        );
                    }
                }
            }
            Collections.sort(priceWithParagraphAndArticleList);
            priceWithParagraphAndArticleList.forEach(System.out::println);

        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
        }
    }

    private static int parseInt(String text) {
        int unDigitsNum = 0;
        int digitNum = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int num;
            if ((num = "0123456789".indexOf(String.valueOf(c))) != -1) {
                unDigitsNum = unDigitsNum * 10 + num;
            } else if (c == '千') {
                digitNum = unDigitsNum * 1000;
                unDigitsNum = 0;
            } else if (c == '万') {
                digitNum = unDigitsNum * 10000;
                unDigitsNum = 0;
            } else if (c == '億') {
                digitNum = unDigitsNum * 100000000;
                unDigitsNum = 0;
            }
        }
        return digitNum + unDigitsNum;
    }

    static private org.w3c.dom.Document getXmlDocumentFromUrl(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS implementation = (DOMImplementationLS) registry.getDOMImplementation("XML 1.0");

            LSInput input = implementation.createLSInput();
            input.setByteStream(inputStream);
            input.setEncoding("UTF-8");

            LSParser parser = implementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            parser.getDomConfig().setParameter("namespaces", false);

            return parser.parse(input);

        } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class PriceWithParagraphAndArticle implements Comparable<PriceWithParagraphAndArticle> {
    private int price;
    private String paragraph;
    private String title;
    private String description;
    private URL link;

    PriceWithParagraphAndArticle(int price, String paragraph, String title, String description, URL link) {
        this.price = price;
        this.paragraph = paragraph;
        this.title = title;
        this.description = description;
        this.link = link;
    }

    @Override
    public String toString() {
        return "登場した値段:" + this.price + "\n" +
                "段落:" + this.paragraph + "\n" +
                "記事のタイトル:" + this.title + "\n" +
                "記事の説明:" + this.description + "\n" +
                "URL:" + this.link + "\n";
    }

    @Override
    public int compareTo(PriceWithParagraphAndArticle o) {
        return Integer.compare(this.price, o.price);
    }
}
