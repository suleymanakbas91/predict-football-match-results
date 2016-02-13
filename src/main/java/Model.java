
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
    public static String TOKEN = "c01f377372564f10854877919d090ffe";
    
    public static String getFixturesHtml() {
        String result = "";
        
        Team[] teams = getTeams();
        Fixture[] fixtures = getFixtures();
        result = createHtml(teams, fixtures);
             
        return result;
    }
    
    public static Team[] getTeams() {
        String response = "";
        try {
            URL api = new URL("http://api.football-data.org/v1/soccerseasons/398/teams");
            URLConnection connection = api.openConnection();
            connection.setRequestProperty("X-Auth-Token", TOKEN);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));    
            String inputLine = "";
            while ((inputLine = in.readLine()) != null) 
                response += inputLine;
            in.close();
        } catch (MalformedURLException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (response.isEmpty()) {
            System.out.println("Teams response is empty!");
            return null;
        }
        
        Team[] teams = Team.createTeams(response);
        return teams;
    }
    
    public static Fixture[] getFixtures() {
        String response = "";
        try {            
            URL url = new URL("http://api.football-data.org/v1/fixtures/?league=PL");
            
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("X-Auth-Token", TOKEN);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));    
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
                response += inputLine;
            in.close();
        } catch (MalformedURLException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Fixture[] fixtures = Fixture.createFixtures(response);
        return fixtures;
    }
    
    public static String createHtml(Team[] teams, Fixture[] fixtures) {        
                
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
        sb.append("<th> Home Team Wins </th>");  
        sb.append("<th> Draws </th>");  
        sb.append("<th> Away Team Wins </th>");  
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        
        for(Fixture fixture:fixtures) {  
            Team homeTeam = null;
            Team awayTeam = null;
            for(Team team:teams) {
                if (team.name.equals(fixture.homeTeamName)) {
                    homeTeam = team;
                } else if (team.name.equals(fixture.awayTeamName)) {
                    awayTeam = team;
                }     
            }
            
            if (homeTeam == null || awayTeam == null){
                break;
            }
            
            int homeTeamMarketValue = Integer.parseInt(homeTeam.squadMarketValue.substring(0, homeTeam.squadMarketValue.length()-2).replace(",", ""));
            int awayTeamMarketValue = Integer.parseInt(awayTeam.squadMarketValue.substring(0, awayTeam.squadMarketValue.length()-2).replace(",", ""));
            
            double homeTeamWinProbability = calculateHomeTeamProbability(homeTeamMarketValue, awayTeamMarketValue);
            homeTeamWinProbability = (double) (homeTeamWinProbability * 200 / 3);   
            String homeTeamWin = String.format("%.1f", homeTeamWinProbability) + "%";
            
            double awayTeamWinProbability = calculateAwayTeamProbability(homeTeamMarketValue, awayTeamMarketValue);
            awayTeamWinProbability = (double) (awayTeamWinProbability * 200 / 3);
            String awayTeamWin = String.format("%.1f", awayTeamWinProbability) + "%";
                         
            double drawProbabilityDb = (double) 1/3 * 100;
            String draw = String.format("%.1f", drawProbabilityDb) + "%";
            
            sb.append("<tr>");
            sb.append("<td> ").append(dateFormat.format(fixture.date)).append(" </td>");
            sb.append("<td> ").append(fixture.homeTeamName).append(" </td>");
            sb.append("<td> ").append(fixture.awayTeamName).append(" </td>");
            sb.append("<td> ").append(homeTeamWin).append(" </td>");
            sb.append("<td> ").append(draw).append(" </td>");
            sb.append("<td> ").append(awayTeamWin).append(" </td>");
            sb.append("</tr>");
            
        }
        
        sb.append("</table>");
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    public static double calculateHomeTeamProbability(int homeTeamMarketValue, int awayTeamMarketValue) {
        double homeTeamGoalPrediction = (double) (2.3421278023676446E-9 * homeTeamMarketValue) + 0.7821113685873299;        
        double awayTeamGoalPrediction = (double) (2.3421278023676446E-9 * awayTeamMarketValue) + 0.7821113685873299; 
               
        return (double) homeTeamGoalPrediction / (homeTeamGoalPrediction + awayTeamGoalPrediction);
    }
    
    public static double calculateAwayTeamProbability(int homeTeamMarketValue, int awayTeamMarketValue) {
        double homeTeamGoalPrediction = (double) (2.3421278023676446E-9 * homeTeamMarketValue) + 0.7821113685873299;        
        double awayTeamGoalPrediction = (double) (2.3421278023676446E-9 * awayTeamMarketValue) + 0.7821113685873299;  
        
        return (double) awayTeamGoalPrediction / (homeTeamGoalPrediction + awayTeamGoalPrediction);
    }

    public static class Fixture {
        Links _links;
        Date date;
        String status;
        int matchDay;
        String homeTeamName;
        String awayTeamName;
        Result result;
        
        public static Fixture[] createFixtures(String json) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject)parser.parse(json);
            //JsonElement countElement = object.get("count");
            JsonArray teamsArray = (JsonArray) object.get("fixtures");

            //int count = gson.fromJson(countElement, Integer.class); 
            Fixture[] fixtures = gson.fromJson(teamsArray, Fixture[].class);
            
            return fixtures;
        }
    }
    
    public static class Team {        
        String name;
        String code;
        String shortName;
        String squadMarketValue;
        String crestUrl;
        
        @SerializedName("_links")
        Links links;

        public static Team[] createTeams(String json) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject)parser.parse(json);            
            JsonArray teamsArray = (JsonArray) object.get("teams");
            
            Team[] teams = gson.fromJson(teamsArray, Team[].class);

            return teams;
        }
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
