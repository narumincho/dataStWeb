package ex7;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooShoppingSearcherBySize {

    public static void main(String[] args) {
        try {
            Document xml = getXMLFromUrl(new URL("https://shopping.yahoo.co.jp/rss?p=棚"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList itemNodeList = (NodeList) xPath.evaluate("//item", xml, XPathConstants.NODESET);

            for (int i = 0; i < itemNodeList.getLength(); i++) {
                Node itemNode = itemNodeList.item(i);

                String description = (String) xPath.evaluate("description/text()", itemNode, XPathConstants.STRING);
                Matcher matcher = Pattern.compile("幅(\\d*)cm").matcher(description);
                if (matcher.find()) {
                    try {
                        int width = Integer.parseInt(matcher.group(1));

                        if (width < 65) {
                            System.out.println("タイトル:" + xPath.evaluate("title/text()", itemNode, XPathConstants.STRING));
                            System.out.println("リンク:" + xPath.evaluate("link/text()", itemNode, XPathConstants.STRING));
                            System.out.println("説明:" + description);
                            System.out.println();

                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (MalformedURLException | XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    static Document getXMLFromUrl(URL url) {
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
}
