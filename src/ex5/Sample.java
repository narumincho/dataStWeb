package ex5;

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
import java.net.*;

public class Sample {
    public static void main(String[] args) {
        try {
            URLConnection urlConnection = new URL("https://mainichi.jp/rss/etc/mainichi-flash.rss").openConnection();
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
            Document xml = parser.parse(input);

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList itemNodeList = (NodeList) xPath.evaluate("//item", xml, XPathConstants.NODESET);

            String searchString = "秋";
            System.out.println("「" + searchString + "」という文字がtitleに含まれている記事を探す");

            for (int i = 0; i < itemNodeList.getLength(); i++) {
                Node itemNode = itemNodeList.item(i);

                String title = ((Node) xPath.evaluate("title", itemNode, XPathConstants.STRING)).getTextContent();
                if (title.contains(searchString)) {
                    System.out.println("タイトル:" + title);
                    System.out.println("URL:" + xPath.evaluate("link/text()", itemNode, XPathConstants.STRING));
                }
            }

        } catch (XPathExpressionException | IllegalAccessException | IOException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
