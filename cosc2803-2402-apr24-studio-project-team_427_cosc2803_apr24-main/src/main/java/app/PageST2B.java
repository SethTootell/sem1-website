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
 *
 * @Author: Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @Author: Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @Author: Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */
public class PageST2B implements Handler {
    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page2B.html";

    @Override
    public void handle(Context context) throws Exception {
        // Initialize the session list if it doesn't exist
        if (context.sessionAttribute("lastInputs") == null) {
            context.sessionAttribute("lastInputs", new ArrayList<List<String>>());
        }

        // Create a simple HTML webpage in a String
        StringBuilder html = new StringBuilder("<html>");

        // Add some Head information
        html.append("<head>")
            .append("<title>Focused View of Food Loss/Waste By Food Group</title>")
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
                <h1>Focused View of Food Loss/Waste by Food Group</h1>
                <p>On this page, you will be able to view Focused View of loss/waste change by food group between a set range of years.
                to begin, on the left hand side in the filter bar, select a commodity. From here, set a start and end year for the data
                range that is to be displayed. You can choose to display which details will be displayed (Commodity, Activity,
                Food Supply Stage or cause of loss. You can also
                select to have the data displayed in ascending or descending order. Hit submit and get your data!!</p>
            </div>
        """);

        // Add Div for page Content
        html.append("<div class='data-container'>");

        // Add filter section
        html.append("""
            <div class='filter-section'>
                <h2>Filters</h2>
                <form method='post' action='/page2B.html'>
                    <label for='commodity'>Commodity:</label>
                    <select name='commodity' id='commodity'>
                        <!-- Populate options dynamically -->
                        %s
                    </select><br><br>
                    <label for="year-start">Year Start:</label>
                    <input type='number' name='year-start' id='year-start' value='1966'><br><br>
                    <label for="year-end">Year End:</label>
                    <input type='number' name='year-end' id='year-end' value='2022'><br><br>
                    <h2>Filter Fields</h2>
                    <input type='checkbox' id='activity' name='activity' value='activity' checked>
                    <label for='activity'>Activity</label><br>
                    <input type='checkbox' id='supply' name='supply' value='supply' checked>
                    <label for='supply'>Food Supply Stage</label><br>
                    <input type='checkbox' id='loss' name='loss' value='loss' checked>
                    <label for='loss'>Cause of Loss</label><br><br>
                    <label for="sort-by">Sort By:</label>
                    <select name='sort-by' id='sort-by'>
                        <option value='Ascend'>Ascend</option>
                        <option value='Descend'>Descend</option>
                    </select><br><br>
                    <button type='submit'>Apply Filters</button>
                </form>
                <label for='lastInputs'>View last 5 inputs:</label>
                <select id='lastInputs' readonly>
            """.formatted(ST2BFilter.getFoodGroupsInDropdown()));

        // Retrieve last inputs from session and populate dropdown
        List<List<String>> lastInputs = context.sessionAttribute("lastInputs");
        for (List<String> input : lastInputs) {
            html.append("<option>").append("Commodity: ").append(input.get(0))
                .append(", Year Start: ").append(input.get(1))
                .append(", Year End: ").append(input.get(2))
                .append(", Activity: ").append(input.get(3))
                .append(", Supply: ").append(input.get(4))
                .append(", Loss: ").append(input.get(5))
                .append(", Sort: ").append(input.get(6))
                .append("</option>");
        }

        html.append("""
                </select>
            </div>
        """);

        // Handle form submission
        if (context.method().equalsIgnoreCase("post")) {
            String foodGroup = context.formParam("commodity", "Cereals");
            String yearStart = context.formParam("year-start", "1966");
            String yearEnd = context.formParam("year-end", "2022");
            boolean isActivityActive = context.formParam("activity") != null;
            boolean isFoodSupplyStageActive = context.formParam("supply") != null;
            boolean isCauseOfLossActive = context.formParam("loss") != null;
            String sortByParam = context.formParam("sort-by", "Ascend");
            String sort = "Ascend".equals(sortByParam) ? "Asc" : "Desc";

            // Save current input to session
            List<String> currentInput = List.of(foodGroup, yearStart, yearEnd, 
                                                String.valueOf(isActivityActive), 
                                                String.valueOf(isFoodSupplyStageActive), 
                                                String.valueOf(isCauseOfLossActive), sort);

            if (lastInputs.size() == 5) {
                lastInputs.remove(0);
            }
            lastInputs.add(currentInput);
            context.sessionAttribute("lastInputs", lastInputs);

            // Generate table with the filtered data
            html.append("""
                <div class='table-wrapper'>
                    <div class='results centered'>
                        %s
                    </div>
                </div>
            """.formatted(ST2BFilter.generateTable(foodGroup, yearStart, yearEnd, isActivityActive, isFoodSupplyStageActive, isCauseOfLossActive, sort)));
        }

        // Close Content div
        html.append("</div>");

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
