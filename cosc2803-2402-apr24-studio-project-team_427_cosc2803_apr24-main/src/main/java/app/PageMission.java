package app;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import helper.DBHelper;
import java.sql.SQLException;

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

public class PageMission implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/mission.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" + 
               "<title>Our Mission</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

        // Add the topnav
        // This uses a Java v15+ Text Block
        html = html + """
            <div class='topnav'>
            <a href='/'><img src='logo.png' class='top-image' alt='RMIT logo' height='75'></a>
                <a href='/'>Landing Page</a>
                <a href='mission.html'>Our Mission</a>
                <a href='page2A.html'>Focused View of loss/waste change by country</a>
                <a href='page2B.html'>Focused View of loss/waste change by food group</a>
                <a href='page3A.html'>Identify locations with similar food waste/loss percentages</a>
                <a href='page3B.html'>Exploring food commodities and groups</a>
        </div>
        """;

        // Add header content block
        html = html + """
            <div class='header'>
                <h1>Our Mission</h1>
                <p> Our mission is to provided fast and accurate food loss and waste information from around the world. Wheter you are a farmer, student, grocer or anything inbetween, this website is a one stop shop for all things food waste and loss data. View people the different data options and visit a page of your liking today! </p>
            </div>
        """;

        // Add Div for page Content
        html = html + "<div class='content'>";

        // Add HTML for the page content

        // Describing each page
        html = html + """
        <div class = "topics">
            <div class = "topic-mission">
                <h2><a = href="page2A.html">Focused view of loss/waste change by Country</a></h2>
                <p>Allows you to filter the data so that you can discover information on Food loss by country. You can filter by food type, the year. You can also sort by ascending/descending</p>
            </div>

            <div class = "topic-mission">
                <h2><a href="page2B.html">Focused View of Food Loss/Waste by Food Group</a></h2>
                <p>Allows you to filter the data so that you can discover information on food loss by food group. You can chose the food group, filter in a year range. You also have the ability to only show specific information and sort by ascending/descending</p>
            </div>

            <div class = "topic-mission">
                <h2><a href = "page3A.html">Identify locations with similar food waste/loss percentages</a></h2>
                <p>Allows you to filter the data so that you can discover information on food loss with similar loss percentages. You can filter the data to sort in similarity over common food groups and/or overall loss percentage</p>
            </div>

            <div class = "topic-mission">
                <h2><a href = "page3B.html">Exploring food commodities and groups</a></h2>
                <p>Allows you to filter the data so that you can discover information on food commodities and groups. You can filter by the food, showing similarity based on food loss/waste ratio or the highest or the lowest loss/waste percentage</p>
            </div>
        </div>
        """;

        // Persona information

        html += """
                <h2 style = "align-text: center"> Personas </h2>

                """;
        final String PersonaQuery = "SELECT * FROM PERSONA";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            Statement statement = connection.createStatement();
            System.out.println("Now executing query: " + PersonaQuery);

            ResultSet results = statement.executeQuery(PersonaQuery);
            ArrayList<Persona> personas = new ArrayList<Persona>();

            while (results.next()) {
                int persona_id = results.getInt("persona_id");
                String name = results.getString("name");
                String image_path = results.getString("image");
                ArrayList<PersonaAttribute> personaAttributes = PersonaAttribute.getAttributesByID(persona_id);
                Persona currPersona = new Persona(persona_id, name, image_path, personaAttributes);

                personas.add(currPersona);
            }

            for (Persona current : personas) {
                System.out.println(PersonaQuery);
                html += """
                        <div class = "persona-section">
                            <h2> %s </h2>
                            <img src = "%s" alt = "could not load image"/>
                        """
                        .formatted(
                            current.getName(),
                            current.getImagePath()
                            );
                ArrayList<PersonaAttribute> DescriptionAttrs = current.filterByAttributeType("Description");
                ArrayList<PersonaAttribute> NeedsAttrs = current.filterByAttributeType("Needs and Goals");
                ArrayList<PersonaAttribute> SkillsAttrs = current.filterByAttributeType("Skills & Experience");
                
                html += """
                            <h2> Description/Attributes </h2>
                            <ul>
                                %s
                            </ul>

                            <h2> Needs and Goals </h2>
                            <ul>
                                %s
                            </ul>
                            
                            <h2> Skills & Experience </h2>
                            <ul>
                                %s
                            </ul>
                        </div>
                        """.formatted(
                            PersonaAttribute.attrsInList(DescriptionAttrs),
                            PersonaAttribute.attrsInList(NeedsAttrs),
                            PersonaAttribute.attrsInList(SkillsAttrs)
                        );
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Student information
        final String STUDENT_QUERY = "SELECT * FROM STUDENT";

        try {
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(STUDENT_QUERY);
            
            ArrayList<Student> students = new ArrayList<Student>();

            while (result.next()) {
                int studentID = result.getInt(Student.getIdFromDB());
                String studentName = result.getString(Student.getNameFromDB());
                students.add(new Student(studentID, studentName));
            }

            for (Student currStudent : students) {
                html += """
                        <div class = "student-section">
                            <h2 style = "display:inline">Student Name:</h2>
                                <p>%s</p>
                            <h2>Student ID: %d</h2>
                        </div>
                        """.formatted(
                            currStudent.getName(),
                            currStudent.getID()
                        );
            }


        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        


        // Close Content div
        html = html + "</div>";

        // Footer
        html = html + """
            <div class='footer'>
                <p>COSC2803 - Programming studio 1 project</p>
                <p>Completed by Benjamin Beattie s4007769 and Seth Tootell s4095464</p>
            </div>
        """;

        // Finish the HTML webpage
        html = html + "</body>" + "</html>";
        

        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }

}
