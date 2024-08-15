package app;

import java.util.ArrayList;

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
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @author Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class PageIndex implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Header information
        html += "<head>" +
                "<title>Homepage</title>";

        // Add some CSS (external file) and inline styles
        html += "<link rel='stylesheet' type='text/css' href='common.css' />";
        html += "<style>" +
                ".centered-content { text-align: center; }" +
                ".header { display: flex; align-items: center; justify-content: center; }" +
                ".header .topics { width: 80%; margin-top: 20px; }" +
                // other styles ...
                "</style>";
        html += "</head>";

        // Start the body
        html += "<body>";

        // Add the top navigation bar
        html += """
            <div class='topnav'>
                <a href='/'><img src='logo.png' class='top-image' alt=' logo' height='75'></a>
                <a href='/'>Landing Page</a>
                <a href='mission.html'>Our Mission</a>
                <a href='page2A.html'>Focused View of loss/waste change by country</a>
                <a href='page2B.html'>Focused View of loss/waste change by food group</a>
                <a href='page3A.html'>Identify locations with similar food waste/loss percentages</a>
                <a href='page3B.html'>Exploring food commodities and groups</a>
            </div>
        """;


        html += """
        <div class='header' style='text-align: center;'>
        <h1>OUR TOPICS</h1>
        </div>
        """;


        // Add header content block with topics
        html += """
            <div class='header' style='text-align: center;'>
                <div class='topics'>
                    <div class='topic active'>
                        <p><a href='mission.html'>Our Mission</a></p>
                        <p>View our overall mission, as well as personas and Student IDs.</p>
                        <div class='topics-buttons'>
                            <a href='mission.html'><button style='font-size: 125%;'>Visit Page</button></a>
                        </div>
                    </div>
                    <div class='topic'>
                        <p><a href='page2A.html'>Focused View of loss/waste change by country</a></p>
                        <p>View changes in food loss and waste in a certain country over a given period of time.</p>
                        <div class='topics-buttons'>
                            <a href='page2A.html'><button style='font-size: 125%;'>Visit Page</button></a>
                        </div>
                    </div>
                    <div class='topic'>
                        <p><a href='page2B.html'>Focused View of loss/waste change by food group</a></p>
                        <p>View changes in food loss and waste for a certain food or commodity group over a period of time.</p>
                        <div class='topics-buttons'>
                            <a href='page2B.html'><button style='font-size: 125%;'>Visit Page</button></a>
                        </div>
                    </div>
                    <div class='topic'>
                        <p><a href='page3A.html'>Identify locations with similar food waste/loss percentages</a></p>
                        <p>View locations with similar levels of food waste and loss, depending on certain filters.</p>
                        <div class='topics-buttons'>
                            <a href='page3A.html'><button style='font-size: 125%;'>Visit Page</button></a>
                        </div>
                    </div>
                    <div class='topic'>
                        <p><a href='page3B.html'>Exploring food commodities and groups</a></p>
                        <p>View food and commodity with similar levels of food waste and loss, depending on certain filters</p>
                        <div class='topics-buttons'>
                            <a href='page3B.html'><button style='font-size: 125%;'>Visit Page</button></a>
                        </div>
                    </div>
                </div>
            </div>
        """;

//logo
        html += """
            <div class='centered-content'>
                <img src='logo.png' alt='Logo' style='max-width: 40%; height: 50%;'>
            </div>
        """;

        // Add HTML for the new content section
        html += """
            <div class='content' style='text-align: center;'>
                <h1>DATA SNAPSHOT</h1>
                <div class='topics'>
                    <div class='topic'>
                        <p>The data range services the years: 1966-2022</p>
                    </div>
                    <div class='topic'>
                        <p>The maximum percentage single year loss percentage of any commodity is: 65%</p>
                    </div>
                    <div class='topic'>
                        <p>The commodity of this maximum percentage single year loss percentage is: Cauliflowers and broccoli</p>
                    </div>
                </div>
            </div>
        """;

        // Add footer
        html += """
            <div class='footer'>
            <p>COSC2803 - Programming studio 1 project</p>
            <p>Completed by Benjamin Beattie s4007769 and Seth Tootell s4095464</p>
            </div>
        """;

        // Add JavaScript to handle topic switching
        html += """
            <script>
                let currentTopicIndex = 0;
                const topics = document.querySelectorAll('.topic');

                function showNextTopic() {
                    topics[currentTopicIndex].classList.remove('active');
                    currentTopicIndex = (currentTopicIndex + 1) % topics.length;
                    topics[currentTopicIndex].classList.add('active');
                }
            </script>
        """;

        // Close the HTML document
        html += "</body></html>";

        // Render the HTML using Javalin
        context.html(html);
    }
}