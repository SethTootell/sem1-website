package app;
import java.util.ArrayList;
import java.util.List;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class PageST3A implements Handler {
    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page3A.html";
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
            .append("<title>Subtask 3.1</title>")
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
                <h1>Identify locations with similar food waste/loss percentages</h1>
                <p>On this page, you will be able to Identify locations with similar food waste/loss percentages for a set year. To begin, on the left hand side in the filter bar, select a country. From here, set a start year for the data range that is to be displayed. You can choose to display which details will be displayed (similar by and similarity terms). You can also select the number of similar data points which are displayed. Hit submit and get your data!!</p>
            </div>
        """);

        // Add Div for page Content
        html.append("<div class='data-container'>");

        // Add filter section
        html.append("""
            <div class='filter-section'>
                <h2>Filters</h2>
                <form method='post' action='/page3A.html'>
                    <label for='cr'>Country or Region:</label>
                    <select name='cr' id='cr' onchange='toggleDropdown()'>
                        <option value='country'>Country</option>
                        <option value='region'>Region</option>
                    </select><br><br>
                    <div id='country-dropdown' style='display: block;'>
                        <label for='country'>Country:</label>
                        <select name='country' id='country'>
                            """ + JDBCConnection.getCountriesforDropdown() + """
                        </select><br><br>
                    </div>
                    <div id='region-dropdown' style='display: none;'>
                        <label for='region'>Region:</label>
                        <select name='region' id='region'>
                            """ + JDBCConnection.getRegionsforDropdown() + """
                        </select><br><br>
                    </div>
                    <div id='simbycountry-dropdown' style='display: none;'>
                        <label for='simbycountry'>Similar By (Country):</label>
                        <select name='simbycountry' id='simbycountry' onchange='toggleValueDropdown()'>
                            <option value='food'>Select similarity in terms of the foods products they have in common</option>
                            <option value='percentage'>Select similarity in terms of the overall percentage of food loss/waste</option>
                            <option value='both'>Select similarity in terms of both common foods products and their loss/waste percentage</option>
                        </select><br><br>
                    </div>
                    <div id='simbyregion-dropdown' style='display: none;'>
                        <label for='simbyregion'>Similar By (Region):</label>
                        <select name='simbyregion' id='simbyregion' onchange='toggleValueDropdown()'>
                            <option value='food'>Select similarity in terms of the foods products they have in common</option>
                            <option value='percentage'>Select similarity in terms of the overall percentage of food loss/waste</option>
                        </select><br><br>
                    </div>
                    <div id='value-dropdown' style='display: none;'>
                        <label for='value'>Similarity in terms of:</label>
                        <select name='value' id='value'>
                            <option value='abs'>the absolute values - ignoring food products that are not common between locations</option>
                            <option value='overlap'>the level of overlap - food products that are not common to the selected country impact (reduce) the similarity score</option>
                        </select><br><br>
                    </div>
                    <label for='year-start'>Year Start:</label>
                    <input type='number' id='year-start' name='year-start' value='1966' min='1966' max='2022' required><br><br>
                    <label for='num_similar_groups'>Select how many similar groups?:</label>
                    <input type='number' id='num_similar_groups' name='num_similar_groups' value='5' style='resize: horizontal'><br><br>
                    <button type='submit'>Apply Filters</button>
                </form>
                <label for='lastInputs'>View last 5 inputs</label>
                <select id='lastInputs' readonly>
            """);

        // Retrieve last inputs from session and populate dropdown
        List<List<String>> lastInputs = context.sessionAttribute("lastInputs");
        for (List<String> input : lastInputs) {
            if (input.get(0).equals("country")) {
                html.append("<option>").append("Country: ").append(input.get(1)).append(", Similar By: ").append(input.get(2)).append(", Year Start: ").append(input.get(5)).append(", Groups: ").append(input.get(6)).append("</option>");
            } else if (input.get(0).equals("region")) {
                html.append("<option>").append("Region: ").append(input.get(1)).append(", Similar By: ").append(input.get(3)).append(", Year Start: ").append(input.get(5)).append(", Groups: ").append(input.get(6)).append("</option>");
            }
        }

        html.append("""
                </select>
            </div>
            <script>
                function toggleDropdown() {
                    var cr = document.getElementById('cr').value;
                    var countryDropdown = document.getElementById('country-dropdown');
                    var regionDropdown = document.getElementById('region-dropdown');
                    var simbycountryDropdown = document.getElementById('simbycountry-dropdown');
                    var simbyregionDropdown = document.getElementById('simbyregion-dropdown');
                    var valueDropdown = document.getElementById('value-dropdown');
                    if (cr == 'country') {
                        countryDropdown.style.display = 'block';
                        regionDropdown.style.display = 'none';
                        simbycountryDropdown.style.display = 'block';
                        simbyregionDropdown.style.display = 'none';
                        toggleValueDropdown();
                    } else if (cr == 'region') {
                        countryDropdown.style.display = 'none';
                        regionDropdown.style.display = 'block';
                        simbycountryDropdown.style.display = 'none';
                        simbyregionDropdown.style.display = 'block';
                        toggleValueDropdown();
                    }
                }
                function toggleValueDropdown() {
                    var simbycountryValue = document.getElementById('simbycountry').value;
                    var simbyregionValue = document.getElementById('simbyregion').value;
                    var valueDropdown = document.getElementById('value-dropdown');
                    if ((simbycountryValue == 'food' || simbycountryValue == 'both') && simbyregionValue != 'percentage') {
                        valueDropdown.style.display = 'block';
                    } else if (simbyregionValue == 'food' && simbycountryValue != 'percentage') {
                        valueDropdown.style.display = 'block';
                    } else {
                        valueDropdown.style.display = 'none';
                    }
                }
                // Initial call to set initial state
                toggleDropdown();
            </script>
        """);

        // Variables for form parameters
        boolean isCountry = context.formParam("cr", "").equals("country");
        String simbycountry = "";
        String simbyregion = "";
        boolean isAbsolute = false;

        // Handle form submission
        if (context.method().equalsIgnoreCase("post")) {
            String country = context.formParam("country");
            String region = context.formParam("region");
            simbycountry = context.formParam("simbycountry", "");
            simbyregion = context.formParam("simbyregion", "");
            String value = context.formParam("value", "");
            String yearStart = context.formParam("year-start");
            String numSimilarGroups = context.formParam("num_similar_groups");

            List<String> currentInput = List.of(context.formParam("cr", ""), country, simbycountry, simbyregion, value, yearStart, numSimilarGroups);
            if (!currentInput.isEmpty()) {
                if (lastInputs.size() == 5) {
                    lastInputs.remove(0);
                }
                lastInputs.add(currentInput);
            }

            // Determine similarity table value
            if (simbycountry.equalsIgnoreCase("percentage")) {
                html.append(JDBCConnection.get3APercentageCountry(country, yearStart, numSimilarGroups).replace("_value", ""));
            } else if (simbyregion.equalsIgnoreCase("percentage")) {
                html.append(JDBCConnection.get3APercentageRegion(region, yearStart, numSimilarGroups).replace("_value", ""));
            } else if (simbyregion.equalsIgnoreCase("food") && value.equalsIgnoreCase("abs") && !isCountry) {
                html.append(JDBCConnection.get3AABSR(region, yearStart, numSimilarGroups));
            } else if (simbycountry.equalsIgnoreCase("food") && value.equalsIgnoreCase("abs") && isCountry) {
                html.append(JDBCConnection.get3AABS(country, yearStart, numSimilarGroups));
            } else if (simbycountry.equalsIgnoreCase("food") && value.equalsIgnoreCase("overlap") && isCountry) {
                html.append(JDBCConnection.get3AOVC(country, yearStart, numSimilarGroups));
            } else if (simbycountry.equalsIgnoreCase("food") && value.equalsIgnoreCase("overlap") && !isCountry) {
                html.append(JDBCConnection.get3AOVCR(region, yearStart, numSimilarGroups));
            } else if (simbycountry.equalsIgnoreCase("both") && value.equalsIgnoreCase("abs") && isCountry) {
                html.append(JDBCConnection.getbotha(country, yearStart, numSimilarGroups));
            } else if (simbycountry.equalsIgnoreCase("both") && value.equalsIgnoreCase("overlap") && isCountry) {
                html.append(JDBCConnection.getbothp(country, yearStart, numSimilarGroups));
            }

            if ("abs".equals(value)) {
                isAbsolute = true;
            } else if ("overlap".equals(value)) {
                isAbsolute = false;
            }
            html.append("</div>");
            html.append("</div>");

            if (isCountry) {
            html.append("<div class='header'>")
            .append("<h2>Your selected countries Data:</h2>")
            .append("</div>");
            html.append("<div class='data-container'>");
            context.sessionAttribute("lastInputs", lastInputs);
            html.append(JDBCConnection.getA1(country, yearStart, numSimilarGroups).replace("_value", ""));
            html.append("</div>");
            html.append("<div class='data-container'>");
            html.append(JDBCConnection.get2A(country, yearStart).replace("_value", ""));
            html.append("</div>");
            }
            if (!isCountry) {
                html.append("<div class='header'>")
                .append("<h2>Your selected Regions Data:</h2>")
                .append("</div>");
                html.append("<div class='data-container'>");
                context.sessionAttribute("lastInputs", lastInputs);
                html.append(JDBCConnection.getA11(region, yearStart, numSimilarGroups).replace("_value", ""));
                html.append("</div>");
                html.append("<div class='data-container'>");
                html.append(JDBCConnection.get2A1(region, yearStart).replace("_value", ""));
                html.append("</div>");
            }

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


