package app;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.ArrayList;
import java.util.List;

public class PageST2A implements Handler {
    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page2A.html";

    @Override
    public void handle(Context context) throws Exception {
        // Initialize the session list if it doesn't exist
        if (context.sessionAttribute("lastInputs") == null) {
            context.sessionAttribute("lastInputs", new ArrayList<List<String>>());
        }

        // Create a simple HTML webpage in a StringBuilder
        StringBuilder html = new StringBuilder("<html>");
        
        // Add some Head information
        html.append("<head>")
            .append("<title>Focused View of Food Waste/Loss by Country</title>")
            .append("<link rel='stylesheet' type='text/css' href='common.css' />")
            .append("</head>");
        
        // Add the body
        html.append("<body>");
        
        // Add the topnav
        html.append("""
            <div class='topnav'>
                <a href='/'><img src='logo.png' class='top-image' alt='RMIT logo' height='75'></a>
                <a href='/'>Landing Page</a>
                <a href='mission.html'>Our Mission</a>
                <a href='page2A.html'>Focused View of loss/waste change by country</a>
                <a href='page2B.html'>Focused View of loss/waste change by food group</a>
                <a href='page3A.html'>Identify locations with similar food waste/loss percentages</a>
                <a href='page3B.html'>Exploring food commodities and groups</a>
            </div>
        """);

        // Add header content block
        html.append("""
            <div class='header'>
                <h1>Focused view of loss/waste change by Country</h1>
                <p>On this page, you will be able to view food loss data for a country of your choice between a set range of years.
                to begin, on the left hand side in the filter bar, select a country. From here, set a start and end year for the data
                range that is to be displayed. You can choose to display which details will be displayed (Commodity, Activity,
                Food Supply Stage or cause of loss) if you select the Show All Data button at the bottom of the page. You can also
                select to have the data displayed in ascending or descending order. Hit submit and get your data!!</p>
            </div>
        """);

        // Add Div for page Content
        html.append("<div class='data-container'>");
        
        // Add filter section
        html.append("""
            <div class='filter-section'>
                <h2>Filters</h2>
                <form method='post' action='/page2A.html'> <!-- Ensure correct form action -->
                    <label for='country'>Country:</label>
                    <select name='country' id='country'>
                        <!-- Populate options dynamically -->
                        """).append(JDBCConnection.getCountriesforDropdown()).append("""
                    </select><br><br>
                    <label for='year-start'>Year Start:</label>
                    <input type='number' id='year-start' name='year-start' value='1966' min='1966' max='2022' required><br><br>
                    <label for='year-end'>Year End:</label>
                    <input type='number' id='year-end' name='year-end' value='2022' min='1966' max='2022' required><br><br> 
                    <h2>Filter Fields</h2>
                    <input type='checkbox' id='commodity' name='commodity' value='commodity'>
                    <label for='commodity'>Commodity</label><br>
                    <input type='checkbox' id='activity' name='activity' value='activity'>
                    <label for='activity'>Activity</label><br>
                    <input type='checkbox' id='supply' name='supply' value='supply'>
                    <label for='supply'>Food Supply Stage</label><br>
                    <input type='checkbox' id='loss' name='loss' value='loss'>
                    <label for='loss'>Cause of Loss</label><br><br>
                    <label for='sort-by'>Sort By:</label>
                    <select name='sortAscDesc' id='sort-by'>
                        <option value='Ascend'>Ascend</option>
                        <option value='Descend'>Descend</option>
                    </select><br><br>
                    <label for='show-all-data'>Show All Data:</label>
                    <input type='checkbox' id='show-all-data' name='show-all-data' value='true'>
                    <button type='submit'>Apply Filters</button>
                </form>
                <label for='lastInputs'>View last 5 inputs:</label>
                <select id='lastInputs' readonly>
            """);

        // Retrieve last inputs from session and populate dropdown
        List<List<String>> lastInputs = context.sessionAttribute("lastInputs");
        for (List<String> input : lastInputs) {
            html.append("<option>").append("Country: ").append(input.get(0))
                .append(", Year Start: ").append(input.get(1))
                .append(", Year End: ").append(input.get(2))
                .append(", Commodity: ").append(input.get(3))
                .append(", Activity: ").append(input.get(4))
                .append(", Supply: ").append(input.get(5))
                .append(", Loss: ").append(input.get(6))
                .append(", Sort: ").append(input.get(7))
                .append(", Show All Data: ").append(input.get(8))
                .append("</option>");
        }

        html.append("""
                </select>
            </div>
        """);

        // Handle form submission
        if (context.method().equalsIgnoreCase("post")) {
            String country = context.formParam("country");
            String yearStart = context.formParam("year-start");
            String yearEnd = context.formParam("year-end");
            boolean commodity = context.formParam("commodity") != null;
            boolean activity = context.formParam("activity") != null;
            boolean supply = context.formParam("supply") != null;
            boolean loss = context.formParam("loss") != null;
            String sort = context.formParam("sortAscDesc").equals("Ascend") ? "ASC" : "DESC";
            boolean showAllData = context.formParam("show-all-data") != null;

            // Save current input to session
            List<String> currentInput = List.of(
                country, yearStart, yearEnd, 
                String.valueOf(commodity), String.valueOf(activity), 
                String.valueOf(supply), String.valueOf(loss), 
                sort, String.valueOf(showAllData)
            );

            if (lastInputs.size() == 5) {
                lastInputs.remove(0);
            }
            lastInputs.add(currentInput);
            context.sessionAttribute("lastInputs", lastInputs);

            // Append the initial data retrieval and display
            html.append("<div class='results centered'>")
                .append(JDBCConnection.get2ADifference(country, yearStart, yearEnd))
                .append(JDBCConnection.get2AYear(country, yearStart))
                .append(JDBCConnection.get2AYear(country, yearEnd));
         
            // If show all data is checked, call get2AResults
            if (showAllData) {
                html.append(JDBCConnection.get2AResults(country, yearStart, yearEnd, 
                                                          commodity, activity, supply, loss, sort));
            }
        }

        // Close Content div
        html.append("</div></div>");

        // Footer
        html.append("""
            <div class='footer'>
                <p>COSC2803 - Studio Project Starter Code (Apr24)</p>
                <p>COSC2803 - Programming studio 1 project</p>
                <p>Completed by Benjamin Beattie s4007769 and Seth Tootell s4095464</p>
            </div>
        """);

        // Finish the HTML webpage
        html.append("</body></html>");

        // Makes Javalin render the webpage
        context.html(html.toString());
    }
}
