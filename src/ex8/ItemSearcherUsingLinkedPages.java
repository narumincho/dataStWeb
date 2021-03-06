package ex8;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemSearcherUsingLinkedPages {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("検索したいワードを入力してください");
        String searchWord = scanner.nextLine();
        System.out.println(searchWord + "を検索しています……");
        try {
            Document xml = getXMLFromUrl(new URL("https://shopping.yahoo.co.jp/rss?p=" + searchWord));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList itemNodeList = (NodeList) xPath.evaluate("//item", xml, XPathConstants.NODESET);
            ArrayList<Item> itemList = new ArrayList<>();

            for (int i = 0; i < itemNodeList.getLength(); i++) {
                itemList.add(new Item(itemNodeList.item(i)));
            }
            itemList.forEach(item -> {
                System.out.println(item);
                System.out.println();
            });

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

    static class Item {
        public String title;
        public URL url;
        public String description;
        public int price;
        public ArrayList<String> paragraphList = new ArrayList<>();

        Item(Node itemNode) throws XPathExpressionException, MalformedURLException, NumberFormatException {
            XPath xPath = XPathFactory.newInstance().newXPath();
            this.title = (String) xPath.evaluate("title/text()", itemNode, XPathConstants.STRING);
            this.url = new URL((String) xPath.evaluate("link/text()", itemNode, XPathConstants.STRING));
            Matcher paragraphMatcher = Pattern.compile("<p>([^<]+)</p>").matcher(getHtmlFromUrl(this.url));
            while(paragraphMatcher.find()){
                paragraphList.add(paragraphMatcher.group(1));
            }
            this.description = (String) xPath.evaluate("description/text()", itemNode, XPathConstants.STRING);
            Matcher matcher = Pattern.compile("([\\d,]*)円").matcher(description);
            if (matcher.find()) {
                String priceText = matcher.group(1).replace(",", "");
                this.price = Integer.parseInt(priceText);
                return;
            }
            System.out.println("円を見つけられなかった");
        }

        @Override
        public String toString() {
            return "タイトル:" + this.title + "\n" +
                    "価格:" + this.price + "\n" +
                    "URL:" + this.url + "\n" +
                    "説明:" + this.description + "\n" +
                    "詳しい説明" + this.paragraphList;
        }
    }

    static String getHtmlFromUrl(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String b;
            while ((b = br.readLine()) != null) {
                sb.append(b);
            }
            return sb.toString();

        } catch (IOException e) {
            System.err.println("入出力エラー: " + e.toString());
            return "";
        }
    }
}
