package app;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.ArrayList;
import java.util.List;

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 */
public class PageST3B implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page3B.html";

    @Override
    public void handle(Context context) throws Exception {
        // Initialize the session list if it doesn't exist
        if (context.sessionAttribute("lastInputs") == null) {
            context.sessionAttribute("lastInputs", new ArrayList<List<String>>());
        }
    
        // Create a simple HTML webpage in a String
        String html = "<html>";
    
        // Add some Head information
        html += "<head>" +
                "<title>Exploring Food Commodities And Groups</title>" +
                "<link rel='stylesheet' type='text/css' href='common.css' />" +
                "<style>" +
                "  .filter-section label, .filter-section input, .filter-section select, .filter-section button {" +
                "    display: block;" +
                "    margin-bottom: 10px;" +
                "  }" +
                "  .filter-section button {" +
                "    margin-top: 20px;" +
                "  }" +
                "</style>" +
                "</head>";
    
        // Add the body
        html += "<body>";
    
        // Add the topnav
        html += "<div class='topnav'>" +
                "<a href='/'><img src='logo.png' class='top-image' alt='RMIT logo' height='75'></a>" +
                "<a href='/'>Landing Page</a>" +
                "<a href='mission.html'>Our Mission</a>" +
                "<a href='page2A.html'>Focused View of loss/waste change by country</a>" +
                "<a href='page2B.html'>Focused View of loss/waste change by food group</a>" +
                "<a href='page3A.html'>Identify locations with similar food waste/loss percentages</a>" +
                "<a href='page3B.html'>Exploring food commodities and groups</a>" +
                "</div>";
    
        // Add header content block
        html += "<div class='header'>" +
                "<h1>Exploring food commodities and groups</h1>" +
                "<p>On this page, you will be able to Explore food commodities and groups. " +
                "To begin, on the left-hand side in the filter bar, select a Food type." +
                " From here select the similarity in terms of, and how many groups you wish to display. " +
                " Hit submit and get your data!!</p>" +
                "<p>Similarity Scores are used to make comparisons on this table. 100 is the highest and represents a direct match in the data. 0 is the lowest and represents no match whatsoever in the data.</p>" +
                "</div>";
    
        // Add Div for page Content
        html += "<div class='data-container'>";
    
        // Add form and filters
        html += "<div class='filter-section'>" +
                "<h2>Filters</h2>" +
                "<form method='post' action='/page3B.html' id='filterForm'>" +
                "<label for='food'>Food</label>" +
                "<select name='food' id='food'>" +
                ST3BFilter.getFoodforDropdown() + // Populate food dropdown options
                "</select>" +
                "<label for='similarity'>Similar in terms of...</label>" +
                "<select name='similarity' id='similarity'>" +
                "<option value='food_loss_ratio'>ratio of food loss to food waste (% Average)</option>" +
                "<option value='high_loss_waste'>highest percentage of food loss/waste</option>" +
                "<option value='low_loss_waste'>lowest percentage of food loss/waste</option>" +
                "</select>" +
                "<label for='num_similar_groups'>Select how many similar groups?</label>" +
                "<input type='number' id='num_similar_groups' name='num_similar_groups' value='5' min='1' max='21' required>" +
                "<button type='submit'>Apply Filters</button>" +
                "</form>";
    
        // Add dropdown for last 5 inputs
        html += "<label for='lastInputs'>View last 5 inputs</label>" +
                "<select id='lastInputs' onchange='populateFields()'>" +
                "<option value=''>Select previous inputs</option>";
    
        // Retrieve last inputs from session and populate dropdown
        List<List<String>> lastInputs = context.sessionAttribute("lastInputs");
        for (List<String> input : lastInputs) {
            html += "<option value='" + String.join(",", input) + "'>" + String.join(", ", input) + "</option>";
        }
    
        html += "</select>";

    
    
        html += "</div>"; // End of filter-section
    
        // Check if form was submitted (POST method)
        if (context.method().equalsIgnoreCase("post")) {
            // Retrieve form parameters
            String selectedFood = context.formParam("food").replace("_value", "");
            String similarTerms = context.formParam("similarity");
            String similarGroups = context.formParam("num_similar_groups");
    
            // Store the current inputs in the session
            List<String> currentInput = List.of(selectedFood, similarTerms, similarGroups);
            lastInputs.add(currentInput);
            if (lastInputs.size() > 5) {
                lastInputs.remove(0); // Keep only the last 5 inputs
            }
            context.sessionAttribute("lastInputs", lastInputs);
    
            // Call JDBC method to execute SQL query and get HTML table
            if (similarTerms.equalsIgnoreCase("high_loss_waste")) {
                html += JDBCConnection.test(selectedFood, similarGroups);
            } else if (similarTerms.equalsIgnoreCase("low_loss_waste")) {
                html += JDBCConnection.test1(selectedFood, similarGroups);
            } else if (similarTerms.equalsIgnoreCase("food_loss_ratio")) {
                html += JDBCConnection.compareGroups(selectedFood, similarGroups);
            }


            html += "</div>";
            html += "</div>";
    
            if (similarTerms.equalsIgnoreCase("high_loss_waste")) {
                html += "<div class='header'>";
                html += "<h2>Your selected commodities Data:</h2>";
                html += "</div>";
                html += "<div class='data-container'>";
                context.sessionAttribute("lastInputs", lastInputs);
                html += JDBCConnection.testA(selectedFood, similarGroups);
                html += "</div>";
                
            }
            else if (similarTerms.equalsIgnoreCase("low_loss_waste")) {
                html += "<div class='header'>";
                html += "<h2>Your selected commodities Data:</h2>";
                html += "</div>";
                html += "<div class='data-container'>";
                context.sessionAttribute("lastInputs", lastInputs);
                html += JDBCConnection.test1A(selectedFood, similarGroups);
                html += "</div>";
            }
            else if (similarTerms.equalsIgnoreCase("food_loss_ratio")) {
                html += "<div class='header'>";
                html += "<h2>Your selected commodities Data:</h2>";
                html += "</div>";
                html += "<div class='data-container'>";
                context.sessionAttribute("lastInputs", lastInputs);
                html += JDBCConnection.getRatio(selectedFood);
                html += "</div>";
        }
        }

        
        // Append query result HTML to existing HTML content
    
        // Close Content div
        html += "</div>";
    
        // Footer
        html += """
            <div class='footer'>
            <p>COSC2803 - Programming studio 1 project</p>
            <p>Completed by Benjamin Beattie s4007769 and Seth Tootell s4095464</p>
            </div>
        """;
    
        // Finish the HTML webpage
        html += "</body></html>";
    
        // Render the HTML response using Javalin context
        context.html(html);
    }
    
}


