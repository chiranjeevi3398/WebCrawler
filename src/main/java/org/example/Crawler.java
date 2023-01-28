package org.example;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink;
    private int MAX_DEPTH=2;
    public Connection connection;
    public Crawler(){
        //Set up the Connection to MySQL
        connection = DatabaseConnection.getConnection();

        urlLink=new HashSet<String>();
    }
    public void getPageTextAndLinks(String url,int depth){
        if(!urlLink.contains(url)) {
            if(urlLink.add(url)){
                System.out.println(url);
            }
            try{
                //Parsing HTML objects to java Document object
                Document document=Jsoup.connect(url).timeout(5000).get();
                //Get text from Document object
                String text=document.text().length()<500?document.text():document.text().substring(0,499);
                //Print text
                System.out.println(text);
                //Insert data into pages table
                PreparedStatement preparedStstement= connection.prepareStatement("Insert into pages values(?,?,?)");
                preparedStstement.setString(1,document.title());
                preparedStstement.setString(2,url);
                preparedStstement.setString(3,text);
                preparedStstement.executeUpdate();
                //Increase Depth
                depth++;
                //If Depth id Greater than Max then Return
                if(depth>MAX_DEPTH){
                    return;
                }
                //Get HyperLinks Available On the Current Page
                Elements availableLinksOnPage=document.select("a[href]");
                //Run method Recursively for every link Available on the Current Page
                for(Element currentLink:availableLinksOnPage){
                    getPageTextAndLinks(currentLink.attr("abs:href"),depth);
                }
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
            catch (SQLException sqlException){
                sqlException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler=new Crawler();
        crawler.getPageTextAndLinks("http://www.javatpoint.com",0);
    }
}