
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author suleyman
 */
public class Model {
    public static String getFixtures() {
        String result = "";
        try {            
            URL api = new URL("http://api.football-data.org/v1/fixtures/?league=PL");
            URLConnection yc = api.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));    
            String inputLine = "";
            while ((inputLine = in.readLine()) != null) 
                result += inputLine;
            in.close();
        } catch (MalformedURLException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        result = jsonParser(result);
        
        return result;
    }
    
    public static String jsonParser(String json) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject)parser.parse(json);
        JsonElement countElement = object.get("count");
        JsonArray teamsArray = (JsonArray) object.get("fixtures");
        
        int count = gson.fromJson(countElement, Integer.class); 
        Fixture[] fixtures = gson.fromJson(teamsArray, Fixture[].class);
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<style>" +
            "td { padding: 6px; border: 1px solid #ccc; text-align: left; }" + 
            "th { background: #333; color: white; font-weight: bold; padding: 6px; border: 1px solid #ccc; text-align: left;}" +
            "</style>");
        sb.append("</head><body>");
        sb.append("<table>");
        sb.append("<th> Date </th>");
        sb.append("<th> Home Team </th>");
        sb.append("<th> Away Team </th>");            
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm zzz");       
        
        String response = "";
        for(Fixture fixture:fixtures) {            
            response += "" + fixture.homeTeamName + " - " + fixture.awayTeamName + "         ";
            
            sb.append("<tr>");
            sb.append("<td> ").append(dateFormat.format(fixture.date)).append(" </td>");
            sb.append("<td> ").append(fixture.homeTeamName).append(" </td>");
            sb.append("<td> ").append(fixture.awayTeamName).append(" </td>");
            sb.append("</tr>");
            
        }
        
        sb.append("</table>");
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }

    public static class Fixture {
        Links _links;
        Date date;
        String status;
        int matchDay;
        String homeTeamName;
        String awayTeamName;
        Result result;
    }
    
    public static class Links {
        Link self;
        Link soccerseason;
        Link homeTeam;
        Link awayTeam;
        
        public static class Link {            
            String url;
        }
    }
    
    public static class Result {
        int goalsHomeTeam;
        int goalsAwayTeam;
    }
    
    public static void appendTag(StringBuilder sb, String tag, String contents) {
        sb.append('<').append(tag).append('>');
        sb.append(contents);
        sb.append("</").append(tag).append('>');
    }
    public static void appendDataCell(StringBuilder sb, String contents) {
        appendTag(sb, "td", contents);
    }
    public static void appendHeaderCell(StringBuilder sb, String contents) {
        appendTag(sb, "th", contents);
    }
}
