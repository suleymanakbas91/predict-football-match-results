/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcreation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author suleyman
 */
public class ModelCreation {

    public static String TOKEN = "c01f377372564f10854877919d090ffe";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { 
        
        int size = writeDataIntoFile();
        double[][] x = new double[size][2];
        double[] y = new double[size];        
        readDataFromFile(x, y);
        
//        TTest tTest = new TTest();
//        System.out.println("p value for home value = " + tTest.tTest(x[0], y));
//        System.out.println("p value for away value = " + tTest.tTest(x[1], y));
//        
        System.out.println("Average mean squared error: " + apply10FoldCrossValidation(x, y));

//        double[] predictions = new double[size];
//        for (int i = 0; i < size; i++) {             
//            predictions[i] = 0.5622255342802198 + (1.0682845275289186E-9 * x[i][0]) + (-9.24614306976538E-10 * x[i][1]);
//                               
//            //System.out.print("Actual: " + y[i]);
//            //System.out.println(" Predicted: " + predicted);
//        }
//        
//        System.out.println(calculateMeanSquaredError(y, predictions));
                
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        regression.setNoIntercept(true);
        printRegressionStatistics(regression);
        
        //Team[] teams2014 = getTeams(354);
        //Team[] teams2015 = getTeams(398, 2015);

        //Team[] teams = concatTeams(teams2014, teams2015);
        
//        HashMap<Integer, ArrayList<Integer>> marketValueGoalsDataset = createMarketValueGoalsDataset(teams2014);
//
//        SimpleRegression regression = new SimpleRegression();
//                
//        Set<Integer> marketValues = marketValueGoalsDataset.keySet();
//        for (Integer marketValue:marketValues) {
//            ArrayList<Integer> goals = marketValueGoalsDataset.get(marketValue);
//            int totalGoals = 0;
//            for(Integer goal:goals) {
//                regression.addData(marketValue, goal);
//                totalGoals += goal;
//            }
//            double avg = (double) totalGoals / goals.size();
//            System.out.println("Team Value: " + marketValue + ", Goal Average: " + avg);
//        }      
//        
//        System.out.println("Intercept: " + regression.getIntercept());
//        System.out.println("Slope: " + regression.getSlope());
//        System.out.println("R^2: " + regression.getRSquare());
        
        //LinearRegression.calculateLinearRegression(marketValueGoalsDataset);
    }
    
    public static double apply10FoldCrossValidation(double [][] x, double[] y) {
        int subSize = y.length / 10;
        ArrayList<Integer> indeces = new ArrayList();        
        for (int i = 0; i < y.length; i++) {
            indeces.add(i);
        }
        Collections.shuffle(indeces);
        
        double[] meanSquaredErrors = new double[10];
        int count = 0;        
        for (int i = 0; i < 10; i++) {            
            System.out.println("-------------Fold " + i + "--------------");
            double[][] subXTest = new double[subSize][2];
            double[] subYTest = new double[subSize];
            double[][] subXTraining = new double[y.length - subSize][2];
            double[] subYTraining = new double[y.length - subSize];
                  
            for (int j = 0; j < i*subSize; j++) {
                int index = indeces.get(count);                
                count++;
                subXTraining[j][0] = x[index][0];                
                subXTraining[j][1] = x[index][1];
                subYTraining[j] = y[index];
            }
            
            for (int j = 0; j < subSize; j++) {
                int index = indeces.get(count);                
                count++;
                subXTest[j][0] = x[index][0];                
                subXTest[j][1] = x[index][1];
                subYTest[j] = y[index];
            }
            
            for (int j = i*subSize; j < y.length - subSize; j++) {
                int index = indeces.get(count);                
                count++;
                subXTraining[j][0] = x[index][0];                
                subXTraining[j][1] = x[index][1];
                subYTraining[j] = y[index];
            }
            
            count = 0;
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression(); 
            regression.newSampleData(subYTraining, subXTraining);
            regression.setNoIntercept(true);   
            meanSquaredErrors[i] = evaluateModel(regression, subXTest, subYTest);            
        }     
        
        double sum = 0;
        for (int i = 0; i < meanSquaredErrors.length; i++) {
            sum += meanSquaredErrors[i];
        }
        return (double) sum / meanSquaredErrors.length;
        
    }
    
    public static double evaluateModel(OLSMultipleLinearRegression regression, double[][] subXTest, double[] subYTest){
        System.out.println("Adjusted R^2 = " + regression.calculateAdjustedRSquared());
        System.out.println("R^2 = " + regression.calculateRSquared());
        System.out.println("Residual Sum Of Squares = " + regression.calculateResidualSumOfSquares());
        System.out.println("Total Sum of Squares = " + regression.calculateTotalSumOfSquares());
        
        double[] parameters = regression.estimateRegressionParameters();        
        double[] predictions = new double[subYTest.length];
        
        for (int i = 0; i < subYTest.length; i++) {
            double prediction = parameters[0] + (parameters[1] * subXTest[i][0]) + (parameters[2] * subXTest[i][1]);
            predictions[i] = prediction;
        }
        
        double meanSquaredError = calculateMeanSquaredError(subYTest, predictions);
        System.out.println("Mean Squared Error = " + meanSquaredError);
        return meanSquaredError;        
    }
    
    public static double calculateMeanSquaredError(double[] actuals, double[] predictions) {
        int size = actuals.length;
        double sum_sq = 0;
        for (int i = 0; i < size; i++) {
            double err = predictions[i] - actuals[i];
            sum_sq += (err*err);
        }
        double mse = (double) sum_sq / size;
        return mse;
    }
    
    public static void printRegressionStatistics(OLSMultipleLinearRegression regression) {
        System.out.println("Adjusted R^2 = " + regression.calculateAdjustedRSquared());
        System.out.println("R^2 = " + regression.calculateRSquared());
        System.out.println("Residual Sum Of Squares = " + regression.calculateResidualSumOfSquares());
        System.out.println("Total Sum of Squares = " + regression.calculateTotalSumOfSquares());
        
        double[] standardErrors = regression.estimateRegressionParametersStandardErrors();        
        double[] residuals = regression.estimateResiduals();
        double[] parameters = regression.estimateRegressionParameters();
        
        int residualdf = residuals.length-parameters.length;
        for (int i=0; i < parameters.length; i++){
            double coeff = parameters[i];
            double tstat = parameters[i] / regression.estimateRegressionParametersStandardErrors()[i];
            double pvalue = new TDistribution(residualdf).cumulativeProbability(-FastMath.abs(tstat))*2;
            
            System.out.println("Coefficient(" + i + ") : " + coeff);
            System.out.println("Standard Error(" + i + ") : " + standardErrors[i]);
            System.out.println("t-stats(" + i +") : " + tstat);
            System.out.println("p-value(" + i +") : " + pvalue);
        }   
    }
    
    public static void readDataFromFile(double [][] x, double[] y) {
        try {            
            BufferedReader in = new BufferedReader(new FileReader("fixtures.txt"));
            int index = 0;
            String line;
            while ((line = in.readLine()) != null) {
                String[] row = line.split(",");
                x[index][0] = Double.parseDouble(row[0]);
                x[index][1] = Double.parseDouble(row[1]);
                y[index] = Double.parseDouble(row[2]);
                index++;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public static int writeDataIntoFile() {
        int count = 0;
        Team[] teams2014 = getTeams(354);
        Fixture[] fixtures2014 = Fixture.getAllFixtures(354);
              
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("fixtures.txt"));
            for (Fixture fixture:fixtures2014) {
                if (fixture.result.goalsHomeTeam != fixture.result.goalsAwayTeam && fixture.status.equals("FINISHED")) {
                    int homeTeamValue = 0;
                    int awayTeamValue = 0;
                    for (Team team:teams2014) {
                        if (team.name.equals(fixture.homeTeamName)) {
                            homeTeamValue = Integer.parseInt(team.squadMarketValue.substring(0, team.squadMarketValue.length()-2).replace(",", ""));
                        } else if (team.name.equals(fixture.awayTeamName)) {
                            awayTeamValue = Integer.parseInt(team.squadMarketValue.substring(0, team.squadMarketValue.length()-2).replace(",", ""));
                        } 
                    }

                    String result = "";
                    if (fixture.result.goalsHomeTeam > fixture.result.goalsAwayTeam) {
                        result = "1";
                    } else {
                        result = "0";
                    }
                    
                    out.write(homeTeamValue + "," + awayTeamValue + "," + result + "\n");
                    count++;
                }                
            }
            out.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return count;
    }

    public static <T> T[] concatArrays(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static HashMap<Integer, ArrayList<Integer>> createMarketValueGoalsDataset(Team[] teams) {
        HashMap<Integer, ArrayList<Integer>> marketValueGoalsDataset = new HashMap();

        for (Team team : teams) {
            ArrayList<Integer> goals = new ArrayList();
            for (Fixture fixture : team.fixtures) {
                if (fixture.status.equals("FINISHED")) {
                    if (team.name.equals(fixture.homeTeamName)) {
                        goals.add(fixture.result.goalsHomeTeam);
                    } else {
                        goals.add(fixture.result.goalsAwayTeam);
                    }
                }
            }

            String valueStr = team.squadMarketValue.substring(0, team.squadMarketValue.length() - 2).replace(",", "");
            int marketValue = Integer.parseInt(valueStr);
            marketValueGoalsDataset.put(marketValue, goals);
        }

        return marketValueGoalsDataset;
    }
    
    public static Team[] getTeams(int seasonId) {
        String response = "";
        try {
            URL api = new URL("http://api.football-data.org/v1/soccerseasons/" + seasonId + "/teams");
            URLConnection connection = api.openConnection();
            connection.setRequestProperty("X-Auth-Token", TOKEN);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
            in.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (response.isEmpty()) {
            System.out.println("Response is empty!");
            return null;
        }

        Team[] teams = Team.createTeams(response);

        return teams;
    }   

    public static Team[] getTeamsWithFixtures(int seasonId, int seasonYear) {
        String response = "";
        try {
            URL api = new URL("http://api.football-data.org/v1/soccerseasons/" + seasonId + "/teams");
            URLConnection connection = api.openConnection();
            connection.setRequestProperty("X-Auth-Token", TOKEN);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
            in.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (response.isEmpty()) {
            System.out.println("Response is empty!");
            return null;
        }

        Team[] teams = Team.createTeamsWithFixtures(response, seasonYear);

        return teams;
    }   

    public static class Team {

        String name;
        String code;
        String shortName;
        String squadMarketValue;
        String crestUrl;
        Fixture[] fixtures;

        @SerializedName("_links")
        Links links;

        public void setFixtures(Fixture[] fixtures) {
            this.fixtures = fixtures;
        }

        public static Team[] createTeams(String json) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(json);
            JsonArray teamsArray = (JsonArray) object.get("teams");

            Team[] teams = gson.fromJson(teamsArray, Team[].class);

            return teams;
        }
        
        public static Team[] createTeamsWithFixtures(String json, int season) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(json);
            JsonArray teamsArray = (JsonArray) object.get("teams");

            Team[] teams = gson.fromJson(teamsArray, Team[].class);

            Fixture.addFixtureToTeams(teams, season);

            return teams;
        }
    }

    public static class Fixture {

        Links _links;
        Date date;
        String status;
        int matchDay;
        String homeTeamName;
        String awayTeamName;
        Result result;

        public static void addFixtureToTeams(Team[] teams, int season) {
            for (Team team : teams) {
                String fixtureUrl = team.links.fixtures.url + "?season=" + season;
                String response = "";
                try {
                    URL api = new URL(fixtureUrl);
                    URLConnection connection = api.openConnection();
                    connection.setRequestProperty("X-Auth-Token", TOKEN);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));
                    String inputLine = "";
                    while ((inputLine = in.readLine()) != null) {
                        response += inputLine;
                    }
                    in.close();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (response.isEmpty()) {
                    System.out.println("Response is empty!");
                    return;
                }

                Fixture[] fixtures = createFixtures(response);
                team.setFixtures(fixtures);
            }
        }
        
        public static Fixture[] getAllFixtures(int seasonId) {
            String fixtureUrl = "http://api.football-data.org/v1/soccerseasons/" + seasonId + "/fixtures";
            String response = "";
            try {
                URL api = new URL(fixtureUrl);
                URLConnection connection = api.openConnection();
                connection.setRequestProperty("X-Auth-Token", TOKEN);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine = "";
                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }
                in.close();
            } catch (MalformedURLException ex) {
                Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ModelCreation.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (response.isEmpty()) {
                System.out.println("Response is empty!");
                return null;
            }

            Fixture[] fixtures = createFixtures(response);
            return fixtures;
        }

        public static Fixture[] createFixtures(String json) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(json);
            JsonArray teamsArray = (JsonArray) object.get("fixtures");

            Fixture[] fixtures = gson.fromJson(teamsArray, Fixture[].class);
            return fixtures;
        }
    }

    public static class Result {

        int goalsHomeTeam;
        int goalsAwayTeam;
    }

    public static class Links {

        Link self;
        Link fixtures;
        Link players;

        public static class Link {

            @SerializedName("href")
            String url;
        }
    }

}
