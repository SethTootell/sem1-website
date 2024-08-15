package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * Class for Managing the JDBC Connection to a SQLLite Database.
 * Allows SQL queries to be used with the SQLLite Databse in Java.
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @author Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class JDBCConnection {

    // Name of database file (contained in database folder)
    public static final String DATABASE = "jdbc:sqlite:database/food_loss.db";

    /**
     * This creates a JDBC Object so we can keep talking to the database
     */
    public JDBCConnection() {
        System.out.println("Created JDBC Connection Object");
    }

    /**
     * Get all of the Countries in the database.
     * @return
     *    Returns an ArrayList of Country objects
     */
    public ArrayList<Country> getAllCountries() {
        // Create the ArrayList of Country objects to return
        ArrayList<Country> countries = new ArrayList<Country>();

        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "SELECT * FROM Country";
            
            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                // Lookup the columns we need
                String m49Code     = results.getString("m49_code");
                String name  = results.getString("country_name");

                // Create a Country Object
                Country country = new Country(m49Code, name);

                // Add the Country object to the array
                countries.add(country);
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just pring the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        // Finally we return all of the countries
        return countries;
    }

    public static String getCountriesforDropdown() {
        String str = "";
    
        Connection connection = null;
        String query = "SELECT country_name FROM Country ORDER BY country_name ASC";
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                String country = result.getString("country_name");
                str += String.format("<option value='%s'>%s</option>%n", country, country);
            }
            statement.close();
        } catch (SQLException e) { 
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return str;
    }
  
    public static String get2AResults(String country, String yrStart, String yrEnd, boolean comd, boolean act, boolean sup, boolean los, String sort) {
        StringBuilder resultHtml = new StringBuilder();
        String query = "";
        String additionalColumns = "";
    
        // Construct base query
        query += "SELECT c.country_name, MIN(cle.year) AS first_year, cle.percentage";
    
        // Add additional columns based on selected filters
        if (comd) {
            query += ", cmd.commodityName";
            additionalColumns += ", cmd.commodityName";
        }
        if (act) {
            query += ", cle.activity";
            additionalColumns += ", cle.activity";
        }
        if (sup) {
            query += ", cle.supply_stage";
            additionalColumns += ", cle.supply_stage";
        }
        if (los) {
            query += ", cle.cause";
            additionalColumns += ", cle.cause";
        }
    
        // Construct the rest of the query
        query += " FROM Country c ";
        query += " JOIN CountryLossEvent cle ON c.m49_code = cle.m49_code ";
        if (comd) {
            query += " JOIN Commodity cmd ON cmd.cpc_code = cle.cpc_code ";
        }
        query += " WHERE c.country_name = ? ";
        query += " AND cle.year BETWEEN ? AND ? ";
        query += " GROUP BY c.country_name, cle.year, cle.percentage ";
        query += " ORDER BY cle.year " + sort;
    
        Connection connection = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Use PreparedStatement for parameterized query
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, yrStart);
            statement.setString(3, yrEnd);
    
            ResultSet resultSet = statement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            // Center the table within a div
            resultHtml.append("<div style='margin: 0 auto; width: 80%;'>");
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Year</th>");
            resultHtml.append("<th>Percentage</th>");
    
            // Add additional headers based on selected columns
            if (comd) {
                resultHtml.append("<th>Commodity</th>");
            }
            if (act) {
                resultHtml.append("<th>Activity</th>");
            }
            if (sup) {
                resultHtml.append("<th>Supply Stage</th>");
            }
            if (los) {
                resultHtml.append("<th>Cause</th>");
            }
    
            resultHtml.append("</tr>");
    
            // Iterate over result set and populate table rows
            while (resultSet.next()) {
                String countryName = resultSet.getString("country_name");
                int firstYear = resultSet.getInt("first_year");
                double percentage = resultSet.getDouble("percentage");
    
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(countryName).append("</td>");
                resultHtml.append("<td>").append(firstYear).append("</td>");
                resultHtml.append("<td>").append(String.format("%.2f%%", percentage)).append("</td>");
    
                // Add additional columns based on selected filters
                if (comd) {
                    String commodity = resultSet.getString("commodityName");
                    resultHtml.append("<td>").append(commodity).append("</td>");
                }
                if (act) {
                    String activity = resultSet.getString("activity");
                    resultHtml.append("<td>").append(activity).append("</td>");
                }
                if (sup) {
                    String supplyStage = resultSet.getString("supply_stage");
                    resultHtml.append("<td>").append(supplyStage).append("</td>");
                }
                if (los) {
                    String cause = resultSet.getString("cause");
                    resultHtml.append("<td>").append(cause).append("</td>");
                }
    
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
            resultHtml.append("</div>"); // Closing centered div
    
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
           

    public static String get2ADifference(String country, String yrStart, String yrEnd) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
    
        // Query to find closest year with data for yrStart
        String queryFirstYear = "SELECT ? AS country_name, " +
                                "MIN(cle.year) AS first_year, " +
                                "AVG(cle.percentage) AS first_year_avg_percentage " +
                                "FROM CountryLossEvent cle " +
                                "JOIN Country c ON cle.m49_code = c.m49_code " +
                                "WHERE c.country_name = ? " +
                                "AND cle.year IN ( " +
                                "  SELECT cle.year " +
                                "  FROM CountryLossEvent cle " +
                                "  JOIN Country c ON cle.m49_code = c.m49_code " +
                                "  WHERE c.country_name = ? " +
                                "  GROUP BY cle.year " +
                                "  ORDER BY ABS(cle.year - ?) " +
                                "  LIMIT 1 " +
                                ") " +
                                "GROUP BY c.country_name " +
                                "HAVING first_year IS NOT NULL " +
                                "ORDER BY first_year ASC " +
                                "LIMIT 1";
    
        // Query to find closest year with data for yrEnd
        String queryClosestYear = "SELECT ? AS country_name, " +
                                  "MAX(cle.year) AS closest_year, " +
                                  "AVG(cle.percentage) AS closest_year_avg_percentage " +
                                  "FROM CountryLossEvent cle " +
                                  "JOIN Country c ON cle.m49_code = c.m49_code " +
                                  "WHERE c.country_name = ? " +
                                  "AND cle.year IN ( " +
                                  "  SELECT cle.year " +
                                  "  FROM CountryLossEvent cle " +
                                  "  JOIN Country c ON cle.m49_code = c.m49_code " +
                                  "  WHERE c.country_name = ? " +
                                  "  GROUP BY cle.year " +
                                  "  ORDER BY ABS(cle.year - ?) " +
                                  "  LIMIT 1 " +
                                  ") " +
                                  "GROUP BY c.country_name " +
                                  "HAVING closest_year IS NOT NULL " +
                                  "ORDER BY closest_year ASC " +
                                  "LIMIT 1";
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Prepare the statement for yrStart
            PreparedStatement statementFirstYear = connection.prepareStatement(queryFirstYear);
            statementFirstYear.setString(1, country); // Set country name for the first parameter
            statementFirstYear.setString(2, country); // Set country name for the second parameter
            statementFirstYear.setString(3, country); // Set country name for the third parameter
            statementFirstYear.setString(4, yrStart); // Set year start for the closest year with data calculation
    
            ResultSet resultSetFirstYear = statementFirstYear.executeQuery();
    
            // Prepare the statement for yrEnd
            PreparedStatement statementClosestYear = connection.prepareStatement(queryClosestYear);
            statementClosestYear.setString(1, country); // Set country name for the first parameter
            statementClosestYear.setString(2, country); // Set country name for the second parameter
            statementClosestYear.setString(3, country); // Set country name for the third parameter
            statementClosestYear.setString(4, yrEnd);   // Set year end for the closest year with data calculation
    
            ResultSet resultSetClosestYear = statementClosestYear.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            // Center the table within a div
            resultHtml.append("<div style='margin: 0 auto; width: 80%;'>");
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>First Year</th>");
            resultHtml.append("<th>First Year Avg %</th>");
            resultHtml.append("<th>Last Year</th>");
            resultHtml.append("<th>Last Year Avg %</th>");
            resultHtml.append("<th>Percentage Change</th>");
            resultHtml.append("</tr>");
    
            // Process yrStart result
            double firstYearAvgPercentage = 0.0;
            String firstYear = "";
            if (resultSetFirstYear.next()) {
                firstYear = resultSetFirstYear.getString("first_year");
                firstYearAvgPercentage = resultSetFirstYear.getDouble("first_year_avg_percentage");
            }
    
            // Process yrEnd result
            double closestYearAvgPercentage = 0.0;
            String closestYear = "";
            if (resultSetClosestYear.next()) {
                closestYear = resultSetClosestYear.getString("closest_year");
                closestYearAvgPercentage = resultSetClosestYear.getDouble("closest_year_avg_percentage");
            }
    
            // Calculate percentage change
            double percentageChange = closestYearAvgPercentage - firstYearAvgPercentage;
    
            // Append each row of data to the table
            resultHtml.append("<tr>");
            resultHtml.append("<td>").append(country).append("</td>");
            resultHtml.append("<td>").append(firstYear).append("</td>");
            resultHtml.append("<td>").append(String.format("%.2f%%", firstYearAvgPercentage)).append("</td>");
            resultHtml.append("<td>").append(closestYear).append("</td>");
            resultHtml.append("<td>").append(String.format("%.2f%%", closestYearAvgPercentage)).append("</td>");
            resultHtml.append("<td>").append(String.format("%.2f%%", percentageChange)).append("</td>");
            resultHtml.append("</tr>");
    
            resultHtml.append("</table>");
            resultHtml.append("</div>"); // Closing centered div
    
            statementFirstYear.close();
            statementClosestYear.close();
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
    
    



public static String get2AYear(String country, String yr) {
    StringBuilder resultHtml = new StringBuilder();
    String query = "SELECT ? AS country_name, " +
                   "MIN(cle.year) AS closest_year, " +
                   "AVG(cle.percentage) AS avg_percentage " +
                   "FROM CountryLossEvent cle " +
                   "JOIN Country c ON cle.m49_code = c.m49_code " +
                   "WHERE c.country_name = ? " +
                   "AND cle.year IN ( " +
                   "  SELECT cle.year " +
                   "  FROM CountryLossEvent cle " +
                   "  JOIN Country c ON cle.m49_code = c.m49_code " +
                   "  WHERE c.country_name = ? " +
                   "  GROUP BY cle.year " +
                   "  ORDER BY ABS(cle.year - ?) " +
                   "  LIMIT 1 " +
                   ") " +
                   "GROUP BY c.country_name " +
                   "HAVING closest_year IS NOT NULL " +
                   "ORDER BY ABS(avg_percentage - ( " +
                   "  SELECT AVG(cle2.percentage) " +
                   "  FROM CountryLossEvent cle2 " +
                   "  JOIN Country c2 ON cle2.m49_code = c2.m49_code " +
                   "  WHERE c2.country_name = ? " +
                   "    AND cle2.year = ( " +
                   "      SELECT cle.year " +
                   "      FROM CountryLossEvent cle " +
                   "      JOIN Country c ON cle.m49_code = c.m49_code " +
                   "      WHERE c.country_name = ? " +
                   "      ORDER BY ABS(cle.year - ?) " +
                   "      LIMIT 1 " +
                   "    ) " +
                   ")) ASC " +
                   "LIMIT 1";

    Connection connection = null;

    try {
        connection = DriverManager.getConnection(DATABASE);

        // Use PreparedStatement for parameterized query
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, country); // Set country_name in SELECT
        statement.setString(2, country); // Set country_name in WHERE and GROUP BY
        statement.setString(3, country); // Set country_name in subquery WHERE and GROUP BY
        statement.setString(4, yr);      // Set year in subquery ORDER BY ABS
        statement.setString(5, country); // Set country_name in outer HAVING and subquery WHERE
        statement.setString(6, country); // Set country_name in outer subquery WHERE and ORDER BY ABS
        statement.setString(7, yr);      // Set year in outer subquery ORDER BY ABS

        ResultSet resultSet = statement.executeQuery();

        // Start building the HTML table
        resultHtml.append("<style>");
        resultHtml.append("table { width: 100%; border-collapse: collapse; }");
        resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
        resultHtml.append("th { background-color: #f2f2f2; }");
        resultHtml.append("</style>");

        // Center the table within a div
        resultHtml.append("<div style='margin: 0 auto; width: 80%;'>");
        resultHtml.append("<table>");
        resultHtml.append("<tr>");
        resultHtml.append("<th>Country</th>");
        resultHtml.append("<th>Year</th>");
        resultHtml.append("<th>Average Percentage</th>");
        resultHtml.append("</tr>");

        // Iterate over result set and populate table rows
        while (resultSet.next()) {
            String countryName = resultSet.getString("country_name");
            int closestYear = resultSet.getInt("closest_year");
            double avgPercentage = resultSet.getDouble("avg_percentage");

            resultHtml.append("<tr>");
            resultHtml.append("<td>").append(countryName).append("</td>");
            resultHtml.append("<td>").append(closestYear).append("</td>");
            resultHtml.append("<td>").append(String.format("%.2f%%", avgPercentage)).append("</td>");
            resultHtml.append("</tr>");
        }

        resultHtml.append("</table>");
        resultHtml.append("</div>"); // Closing centered div

        statement.close();
    } catch (SQLException e) {
        System.err.println("Error executing SQL query: " + e.getMessage());
    } finally {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    return resultHtml.toString();
}





    public static String getRegionsforDropdown() {
        String str = "";
    
        Connection connection = null;
        String query = "SELECT region_name\n" + //
                        "FROM Region\n" + //
                        "WHERE region_name <> '' AND region_name <> '-'\n" + //
                        "AND region_name <> 'none'\n" + //
                        "ORDER BY region_name ASC;";
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                String region = result.getString("region_name");
                str += String.format("<option value='%s'>%s</option>%n", region, region);
            }
            statement.close();
        } catch (SQLException e) { 
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return str;
    }






    //3A
    public static String get3APercentageCountry(String name, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        String query = "";
    
        try {
            connection = DriverManager.getConnection(DATABASE);
            
            // Query to get closest year for the specified country
            query = "SELECT c.country_name, MIN(cle.year) AS closest_year, AVG(cle.percentage) AS avg_percentage " +
                    "FROM CountryLossEvent cle " +
                    "JOIN Country c ON cle.m49_code = c.m49_code " +
                    "WHERE cle.year IN ( " +
                    "   SELECT cle.year " +
                    "   FROM CountryLossEvent cle " +
                    "   JOIN Country c ON cle.m49_code = c.m49_code " +
                    "   WHERE c.country_name = ? " +
                    "   GROUP BY cle.year " +
                    "   ORDER BY ABS(cle.year - ?) " +
                    "   LIMIT 1 " +
                    ") " +
                    "GROUP BY c.country_name " +
                    "HAVING closest_year IS NOT NULL " +
                    "ORDER BY ABS(avg_percentage - ( " +
                    "   SELECT AVG(cle2.percentage) " +
                    "   FROM CountryLossEvent cle2 " +
                    "   JOIN Country c2 ON cle2.m49_code = c2.m49_code " +
                    "   WHERE c2.country_name = ? " +
                    "     AND cle2.year = ( " +
                    "         SELECT cle.year " +
                    "         FROM CountryLossEvent cle " +
                    "         JOIN Country c ON cle.m49_code = c.m49_code " +
                    "         WHERE c.country_name = ? " +
                    "         ORDER BY ABS(cle.year - ?) " +
                    "         LIMIT 1 " +
                    "     ) " +
                    ")) ASC " +
                    "LIMIT (? + 1)";
    
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, year);
            preparedStatement.setString(6, (numGroups));
    
            ResultSet result = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Last Year</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("</tr>");
    
            while (result.next()) {
                String itemName = result.getString("country_name");
                int closestYear = result.getInt("closest_year");
                double avgPercentage = result.getDouble("avg_percentage");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(closestYear).append("</td>");
                resultHtml.append("<td>").append(String.format("%.2f%%", avgPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
    

    public static String get3APercentageRegion(String name, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Prepare the query to retrieve data
            String query = "SELECT r.region_name, MIN(rle.year) AS closest_year, AVG(rle.percentage) AS avg_percentage " +
                           "FROM RegionLossEvent rle " +
                           "JOIN Region r ON rle.m49_code = r.m49_code " +
                           "WHERE rle.year IN ( " +
                           "   SELECT rle.year " +
                           "   FROM RegionLossEvent rle " +
                           "   JOIN Region r ON rle.m49_code = r.m49_code " +
                           "   WHERE r.region_name = ? " +
                           "   GROUP BY rle.year " +
                           "   ORDER BY ABS(rle.year - ?) " +
                           "   LIMIT 1 " +
                           ") " +
                           "GROUP BY r.region_name " +
                           "HAVING closest_year IS NOT NULL " +
                           "ORDER BY ABS(avg_percentage - ( " +
                           "   SELECT AVG(rle2.percentage) " +
                           "   FROM RegionLossEvent rle2 " +
                           "   JOIN Region r2 ON rle2.m49_code = r2.m49_code " +
                           "   WHERE r2.region_name = ? " +
                           "     AND rle2.year = ( " +
                           "         SELECT rle.year " +
                           "         FROM RegionLossEvent rle " +
                           "         JOIN Region r ON rle.m49_code = r.m49_code " +
                           "         WHERE r.region_name = ? " +
                           "         ORDER BY ABS(rle.year - ?) " +
                           "         LIMIT 1 " +
                           "     ) " +
                           ")) ASC " +
                           "LIMIT (?)";
    
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, year);
            preparedStatement.setString(6, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Region</th>");
            resultHtml.append("<th>Last Year</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String regionName = resultSet.getString("region_name");
                int closestYear = resultSet.getInt("closest_year");
                double avgPercentage = resultSet.getDouble("avg_percentage");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(regionName).append("</td>");
                resultHtml.append("<td>").append(closestYear).append("</td>");
                resultHtml.append("<td>").append(String.format("%.2f%%", avgPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    

    public static String get3AABS(String name, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Query using the provided SQL with adjustments
            String query = "WITH AustraliaYear AS (" +
                           "   SELECT COALESCE(" +
                           "       (SELECT MIN(year) FROM CountryLossEvent cle " +
                           "        JOIN country c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.country_name = ? AND year >= ?), " +
                           "       (SELECT MAX(year) FROM CountryLossEvent cle " +
                           "        JOIN country c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.country_name = ?), " +
                           "       ? " +
                           "   ) AS closest_year " +
                           ") " +
                           "SELECT c.country_name, COUNT(DISTINCT cle.cpc_code) AS common_cpc_codes " +
                           "FROM CountryLossEvent cle " +
                           "JOIN country c ON cle.m49_code = c.m49_code " +
                           "JOIN AustraliaYear ay ON cle.year = ay.closest_year " +
                           "WHERE c.country_name <> ? " +
                           "AND EXISTS ( " +
                           "   SELECT 1 " +
                           "   FROM CountryLossEvent " +
                           "   WHERE year = cle.year " +
                           "     AND cpc_code = cle.cpc_code " +
                           "     AND m49_code = (SELECT m49_code FROM country WHERE country_name = ?) " +
                           ") " +
                           "GROUP BY c.country_name " +
                           "ORDER BY common_cpc_codes DESC " +
                           "LIMIT (?)";
    
            System.out.println("Generated SQL query: " + query); // Debug statement
    
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, year); // Assuming 'year' is a string for flexibility
            preparedStatement.setString(5, name);
            preparedStatement.setString(6, name);
            preparedStatement.setString(7, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Common food products</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String itemName = resultSet.getString("country_name");
                int commonCPCCodes = resultSet.getInt("common_cpc_codes");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(commonCPCCodes).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
 
    public static String get3AABSR(String regionName, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Query using the provided SQL with adjustments
            String query = "WITH AustraliaYear AS (" +
                           "   SELECT COALESCE(" +
                           "       (SELECT MIN(year) FROM RegionLossEvent cle " +
                           "        JOIN Region c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.region_name = ? AND year >= ?), " +
                           "       (SELECT MAX(year) FROM RegionLossEvent cle " +
                           "        JOIN Region c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.region_name = ?), " +
                           "       ? " +
                           "   ) AS closest_year " +
                           ") " +
                           "SELECT c.region_name, COUNT(DISTINCT cle.cpc_code) AS common_cpc_codes " +
                           "FROM RegionLossEvent cle " +
                           "JOIN Region c ON cle.m49_code = c.m49_code " +
                           "JOIN AustraliaYear ay ON cle.year = ay.closest_year " +
                           "WHERE c.region_name <> ? " +
                           "AND EXISTS ( " +
                           "   SELECT 1 " +
                           "   FROM RegionLossEvent " +
                           "   WHERE year = cle.year " +
                           "     AND cpc_code = cle.cpc_code " +
                           "     AND m49_code = (SELECT m49_code FROM Region WHERE region_name = ?) " +
                           ") " +
                           "GROUP BY c.region_name " +
                           "ORDER BY common_cpc_codes DESC " +
                           "LIMIT (?)";
    
            System.out.println("Generated SQL query: " + query); // Debug statement
    
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, regionName);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, regionName);
            preparedStatement.setString(4, year); // Assuming 'year' is a string for flexibility
            preparedStatement.setString(5, regionName);
            preparedStatement.setString(6, regionName);
            preparedStatement.setString(7, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Region</th>");
            resultHtml.append("<th>Common food products</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String itemName = resultSet.getString("region_name");
                int commonCPCCodes = resultSet.getInt("common_cpc_codes");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(commonCPCCodes).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
    
    public static String get3AOVC(String countryName, String defaultYear, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);
    
            // Query using the provided SQL with adjustments
            String query = "WITH AustraliaYear AS (" +
                           "   SELECT COALESCE(" +
                           "       (SELECT MIN(year) FROM CountryLossEvent cle " +
                           "        JOIN country c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.country_name = ? AND year >= ?), " +
                           "       (SELECT MAX(year) FROM CountryLossEvent cle " +
                           "        JOIN country c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.country_name = ?), " +
                           "       ? " +
                           "   ) AS closest_year " +
                           ") " +
                           "SELECT c.country_name, " +
                           "       ROUND(COUNT(DISTINCT cle.cpc_code) * 100.0 / ( " +
                           "           SELECT COUNT(DISTINCT cle2.cpc_code) " +
                           "           FROM CountryLossEvent cle2 " +
                           "           WHERE cle2.year = ay.closest_year " +
                           "       ), 3) AS common_cpc_code_percentage " +
                           "FROM CountryLossEvent cle " +
                           "JOIN country c ON cle.m49_code = c.m49_code " +
                           "JOIN AustraliaYear ay ON cle.year = ay.closest_year " +
                           "WHERE c.country_name <> ? " +
                           "AND EXISTS ( " +
                           "   SELECT 1 " +
                           "   FROM CountryLossEvent " +
                           "   WHERE year = cle.year " +
                           "     AND cpc_code = cle.cpc_code " +
                           "     AND m49_code = (SELECT m49_code FROM country WHERE country_name = ?) " +
                           ") " +
                           "GROUP BY c.country_name " +
                           "ORDER BY common_cpc_code_percentage DESC " +
                           "LIMIT (?)";
    
            // Debug statement to print the generated SQL query
            System.out.println("Generated SQL query: " + query);
    
            // Prepare the statement
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, countryName);
            preparedStatement.setString(2, defaultYear);
            preparedStatement.setString(3, countryName);
            preparedStatement.setString(4, defaultYear);
            preparedStatement.setString(5, countryName);
            preparedStatement.setString(6, countryName);
            preparedStatement.setString(7, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table structure
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Common food product Percentage</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String itemName = resultSet.getString("country_name");
                double commonCPCPercentage = resultSet.getDouble("common_cpc_code_percentage");
    
                // Append table row with data
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(String.format("%.3f%%", commonCPCPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            // Close resources in the finally block to ensure they're always closed
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        // Return the formatted HTML result
        return resultHtml.toString();
    }
    
    
    

    public static String get3AOVCR(String regionName, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);
    
            // Query using the provided SQL with adjustments for regions
            String query = "WITH RegionYear AS (" +
                           "   SELECT COALESCE(" +
                           "       (SELECT MIN(year) FROM RegionLossEvent cle " +
                           "        JOIN Region c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.region_name = ? AND year >= ?), " +
                           "       (SELECT MAX(year) FROM RegionLossEvent cle " +
                           "        JOIN Region c ON cle.m49_code = c.m49_code " +
                           "        WHERE c.region_name = ?), " +
                           "       ? " +
                           "   ) AS closest_year " +
                           ") " +
                           "SELECT c.region_name, " +
                           "       ROUND(COUNT(DISTINCT cle.cpc_code) * 100.0 / ( " +
                           "           SELECT COUNT(DISTINCT cle2.cpc_code) " +
                           "           FROM RegionLossEvent cle2 " +
                           "           WHERE cle2.year = ay.closest_year " +
                           "       ), 3) AS common_cpc_code_percentage " +
                           "FROM RegionLossEvent cle " +
                           "JOIN Region c ON cle.m49_code = c.m49_code " +
                           "JOIN RegionYear ay ON cle.year = ay.closest_year " +
                           "WHERE c.region_name <> ? " +
                           "AND EXISTS ( " +
                           "   SELECT 1 " +
                           "   FROM RegionLossEvent " +
                           "   WHERE year = cle.year " +
                           "     AND cpc_code = cle.cpc_code " +
                           "     AND m49_code = (SELECT m49_code FROM Region WHERE region_name = ?) " +
                           ") " +
                           "GROUP BY c.region_name " +
                           "ORDER BY common_cpc_code_percentage DESC " +
                           "LIMIT (?)";
    
            // Debug statement to print the generated SQL query
            System.out.println("Generated SQL query: " + query);
    
            // Prepare the statement
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, regionName);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, regionName);
            preparedStatement.setString(4, year); // Assuming 'year' is a string for flexibility
            preparedStatement.setString(5, regionName);
            preparedStatement.setString(6, regionName);
            preparedStatement.setString(7, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table structure
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Region</th>");
            resultHtml.append("<th>Common Food Product Percentage</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String itemName = resultSet.getString("region_name");
                double commonCPCPercentage = resultSet.getDouble("common_cpc_code_percentage");
    
                // Append table row with data
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(String.format("%.3f%%", commonCPCPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            // Close resources in the finally block to ensure they're always closed
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        // Return the formatted HTML result
        return resultHtml.toString();
    }
    


    public static String getbotha(String countryName, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);
    
            // Query using the provided SQL with adjustments for countries and dynamic year selection
            String query = "WITH SelectedCountry AS ( " +
                           "    SELECT c.m49_code " +
                           "    FROM Country c " +
                           "    WHERE c.country_name = ? " +
                           "), " +
                           "CountryYear AS ( " +
                           "    SELECT COALESCE( " +
                           "        (SELECT MIN(year) FROM CountryLossEvent cle " +
                           "         JOIN SelectedCountry sc ON cle.m49_code = sc.m49_code " +
                           "         WHERE cle.year >= ? ), " +
                           "        (SELECT MAX(year) FROM CountryLossEvent cle " +
                           "         JOIN SelectedCountry sc ON cle.m49_code = sc.m49_code ), " +
                           "        ? " +
                           "    ) AS closest_year " +
                           "), " +
                           "CountryData AS ( " +
                           "    SELECT AVG(cle.percentage) AS avg_percentage " +
                           "    FROM CountryLossEvent cle " +
                           "    JOIN SelectedCountry sc ON cle.m49_code = sc.m49_code " +
                           "    JOIN CountryYear cy ON cle.year = cy.closest_year " +
                           "), " +
                           "CountryComparison AS ( " +
                           "    SELECT c.country_name, " +
                           "           AVG(cle.percentage) AS avg_percentage, " +
                           "           COUNT(DISTINCT cle.cpc_code) AS common_cpc_codes " +
                           "    FROM CountryLossEvent cle " +
                           "    JOIN Country c ON cle.m49_code = c.m49_code " +
                           "    JOIN CountryYear cy ON cle.year = cy.closest_year " +
                           "    WHERE c.country_name <> ? " +
                           "    AND EXISTS ( " +
                           "        SELECT 1 " +
                           "        FROM CountryLossEvent cle2 " +
                           "        WHERE cle2.year = cle.year " +
                           "        AND cle2.cpc_code = cle.cpc_code " +
                           "        AND cle2.m49_code = (SELECT m49_code FROM Country WHERE country_name = ?) " +
                           "    ) " +
                           "    GROUP BY c.country_name " +
                           "), " +
                           "RankedCountries AS ( " +
                           "    SELECT cc.country_name, " +
                           "           cc.avg_percentage, " +
                           "           cc.common_cpc_codes, " +
                           "           ABS(cc.avg_percentage - (SELECT avg_percentage FROM CountryData)) AS percentage_diff " +
                           "    FROM CountryComparison cc " +
                           ") " +
                           "SELECT rc.country_name, " +
                           "       rc.avg_percentage, " +
                           "       rc.common_cpc_codes " +
                           "FROM RankedCountries rc " +
                           "ORDER BY percentage_diff ASC, common_cpc_codes DESC " +
                           "LIMIT ?";
    
            // Debug statement to print the generated SQL query
            System.out.println("Generated SQL query: " + query);
    
            // Prepare the statement
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, countryName);
            preparedStatement.setString(2, year); // Year parameter for finding closest year
            preparedStatement.setString(3, year); // Year parameter also for fallback in COALESCE
            preparedStatement.setString(4, countryName);
            preparedStatement.setString(5, countryName);
            preparedStatement.setString(6, numGroups);
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table structure
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("<th>Common Food Products</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String country = resultSet.getString("country_name");
                double avgPercentage = resultSet.getDouble("avg_percentage");
                int commonCpcCodes = resultSet.getInt("common_cpc_codes");
    
                // Append table row with data
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(country).append("</td>");
                resultHtml.append("<td>").append(String.format("%.3f%%", avgPercentage)).append("</td>");
                resultHtml.append("<td>").append(commonCpcCodes).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            // Close resources in the finally block to ensure they're always closed
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        // Return the formatted HTML result
        return resultHtml.toString();
    }
    

    public static String getbothp(String countryName, String year, String numgroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);
    
            // Prepare the statement
            String sql = "WITH CountryYear AS (" +
                         "    SELECT COALESCE(" +
                         "        (SELECT MIN(year) " +
                         "         FROM CountryLossEvent cle " +
                         "         JOIN Country c ON cle.m49_code = c.m49_code " +
                         "         WHERE c.country_name = ? AND year >= ?), " +
                         "        (SELECT MAX(year) " +
                         "         FROM CountryLossEvent cle " +
                         "         JOIN Country c ON cle.m49_code = c.m49_code " +
                         "         WHERE c.country_name = ?), " +
                         "        ? " +
                         "    ) AS closest_year " +
                         "), " +
                         "CountryAvgPercentage AS (" +
                         "    SELECT AVG(cle.percentage) AS avg_percentage " +
                         "    FROM CountryLossEvent cle " +
                         "    JOIN Country c ON cle.m49_code = c.m49_code " +
                         "    WHERE c.country_name = ? " +
                         "      AND cle.year = (SELECT closest_year FROM CountryYear) " +
                         "), " +
                         "CommonCPCCodePercentage AS (" +
                         "    SELECT c.country_name, " +
                         "           ROUND(COUNT(DISTINCT cle.cpc_code) * 100.0 / ( " +
                         "               SELECT COUNT(DISTINCT cle2.cpc_code) " +
                         "               FROM CountryLossEvent cle2 " +
                         "               WHERE cle2.year = (SELECT closest_year FROM CountryYear) " +
                         "           ), 3) AS common_cpc_code_percentage " +
                         "    FROM CountryLossEvent cle " +
                         "    JOIN Country c ON cle.m49_code = c.m49_code " +
                         "    JOIN CountryYear cy ON cle.year = cy.closest_year " +
                         "    WHERE c.country_name <> ? " +
                         "      AND EXISTS ( " +
                         "          SELECT 1 " +
                         "          FROM CountryLossEvent " +
                         "          WHERE year = cle.year " +
                         "            AND cpc_code = cle.cpc_code " +
                         "            AND m49_code = (SELECT m49_code FROM Country WHERE country_name = ?) " +
                         "      ) " +
                         "    GROUP BY c.country_name " +
                         ") " +
                         "SELECT c.country_name, " +
                         "       MIN(cle.year) AS closest_year, " +
                         "       ROUND(AVG(cle.percentage), 3) AS avg_percentage, " +
                         "       cpp.common_cpc_code_percentage " +
                         "FROM CountryLossEvent cle " +
                         "JOIN Country c ON cle.m49_code = c.m49_code " +
                         "JOIN CountryYear cy ON cle.year = cy.closest_year " +
                         "JOIN CommonCPCCodePercentage cpp ON c.country_name = cpp.country_name " +
                         "WHERE cle.year = cy.closest_year " +
                         "GROUP BY c.country_name, cpp.common_cpc_code_percentage " +
                         "HAVING closest_year IS NOT NULL " + // Ensure there is data for the closest year
                         "ORDER BY " +
                         "   ABS(ROUND(AVG(cle.percentage), 3) - (SELECT ROUND(avg_percentage, 3) FROM CountryAvgPercentage)) ASC, " +
                         "   cpp.common_cpc_code_percentage DESC " +
                         "LIMIT ?;";
    
            preparedStatement = connection.prepareStatement(sql);
    
            // Set parameters
            preparedStatement.setString(1, countryName);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, countryName);
            preparedStatement.setString(4, year);
            preparedStatement.setString(5, countryName);
            preparedStatement.setString(6, countryName);
            preparedStatement.setString(7, countryName);
            preparedStatement.setInt(8, Integer.parseInt(numgroups)); // Convert numgroups string to integer
    
            // Execute the query
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table structure
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("<th>Common Food Products</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String country = resultSet.getString("country_name");
                double avgPercentage = resultSet.getDouble("avg_percentage");
                double commonCpcCodePercentage = resultSet.getDouble("common_cpc_code_percentage");
    
                // Append table row with data
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(country).append("</td>");
                resultHtml.append("<td>").append(String.format("%.3f%%", avgPercentage)).append("</td>");
                resultHtml.append("<td>").append(commonCpcCodePercentage).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            // Close resources in the finally block to ensure they're always closed
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        // Return the formatted HTML result
        return resultHtml.toString();
    }
    
 
 


    //3B highest
    public static String get3BHighest(String name, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Execute your SQL query
            String query = "WITH SelectedCommodityGroup AS (" +
                           "    SELECT" +
                           "        cg.groupID," +
                           "        cg.DESCRIPTOR AS group_name" +
                           "    FROM" +
                           "        Commodity c" +
                           "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                           "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                           "    WHERE" +
                           "        c.commodityName = 'Tomatoes'" +
                           ")," +
                           "" +
                           "SelectedGroupMaxLossCommodity AS (" +
                           "    SELECT" +
                           "        c.commodityName," +
                           "        MAX(cle.percentage) AS max_loss_percentage" +
                           "    FROM" +
                           "        Commodity c" +
                           "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                           "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                           "    WHERE" +
                           "        cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                           "    GROUP BY" +
                           "        c.commodityName" +
                           "    ORDER BY" +
                           "        max_loss_percentage DESC" +
                           "    LIMIT 1" +
                           ")," +
                           "" +
                           "GroupMaxLoss AS (" +
                           "    SELECT" +
                           "        cg.groupID," +
                           "        cg.DESCRIPTOR AS group_name," +
                           "        MAX(cle.percentage) AS max_loss_percentage" +
                           "    FROM" +
                           "        Commodity c" +
                           "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                           "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                           "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                           "    GROUP BY" +
                           "        cg.groupID, cg.DESCRIPTOR" +
                           ")," +
                           "" +
                           "SimilarityScore AS (" +
                           "    SELECT" +
                           "        gml.groupID," +
                           "        gml.group_name," +
                           "        gml.max_loss_percentage," +
                           "        100 - ABS(gml.max_loss_percentage - (SELECT max_loss_percentage FROM SelectedGroupMaxLossCommodity)) AS similarity_score" +
                           "    FROM" +
                           "        GroupMaxLoss gml" +
                           "    WHERE" +
                           "        gml.groupID <> (SELECT groupID FROM SelectedCommodityGroup)" +
                           ")," +
                           "" +
                           "TopSimilarGroups AS (" +
                           "    SELECT" +
                           "        ss.groupID," +
                           "        ss.group_name," +
                           "        ss.max_loss_percentage," +
                           "        ss.similarity_score" +
                           "    FROM" +
                           "        SimilarityScore ss" +
                           "    ORDER BY" +
                           "        ss.similarity_score DESC" +
                           "    LIMIT 5" +
                           ")" +
                           "" +
                           "SELECT" +
                           "    ts.groupID," +
                           "    ts.group_name," +
                           "    ts.max_loss_percentage," +
                           "    ts.similarity_score" +
                           " FROM" +
                           "    TopSimilarGroups ts;";
    
            // Execute the query and process results
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            // Build HTML table header
            resultHtml.append("<table border=\"1\">");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group ID</th>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Maximum Loss Percentage</th>");
            resultHtml.append("<th>Similarity Score</th>");
            resultHtml.append("</tr>");
    
            // Build HTML table rows from query results
            while (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("groupID")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("max_loss_percentage")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("similarity_score")).append("</td>");
                resultHtml.append("</tr>");
            }
    
            // Close HTML table
            resultHtml.append("</table>");
    
            // Close statement and connection
            resultSet.close();
            statement.close();
            connection.close();
    
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        }
    
        return resultHtml.toString();
    }

    
    
    public static String test(String country, String total) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // SQL query with placeholders for parameters
            String sqlString =
                "WITH SelectedCommodityGroup AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "    WHERE" +
                "        c.commodityName = ?" +
                ")," +
                "" +
                "SelectedGroupMaxLossCommodity AS (" +
                "    SELECT" +
                "        c.commodityName," +
                "        MAX(cle.percentage) AS max_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    WHERE" +
                "        cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                "    GROUP BY" +
                "        c.commodityName" +
                "    ORDER BY" +
                "        max_loss_percentage DESC" +
                "    LIMIT 1" +
                ")," +
                "" +
                "GroupMaxLoss AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name," +
                "        MAX(cle.percentage) AS max_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    GROUP BY" +
                "        cg.groupID, cg.DESCRIPTOR" +
                ")," +
                "" +
                "SimilarityScore AS (" +
                "    SELECT" +
                "        gml.groupID," +
                "        gml.group_name," +
                "        gml.max_loss_percentage," +
                "        100 - ABS(gml.max_loss_percentage - (SELECT max_loss_percentage FROM SelectedGroupMaxLossCommodity)) AS similarity_score" +
                "    FROM" +
                "        GroupMaxLoss gml" +
                "    WHERE" +
                "        gml.groupID <> (SELECT groupID FROM SelectedCommodityGroup)" +
                ")," +
                "" +
                "TopSimilarGroups AS (" +
                "    SELECT" +
                "        ss.groupID," +
                "        ss.group_name," +
                "        ss.max_loss_percentage," +
                "        ss.similarity_score" +
                "    FROM" +
                "        SimilarityScore ss" +
                "    ORDER BY" +
                "        ss.similarity_score DESC" +
                "    LIMIT ?" +
                ")" +
                "" +
                "SELECT" +
                "    ts.groupID," +
                "    ts.group_name," +
                "    ts.max_loss_percentage," +
                "    ts.similarity_score" +
                " FROM" +
                "    TopSimilarGroups ts;";
    
            // Log the query for debugging
            System.out.println("Executing query: " + sqlString);
    
            // Create PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setString(1, country); // Set the country parameter
            preparedStatement.setInt(2, Integer.parseInt(total)); // Set the total parameter
    
            // Execute query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
    
            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group ID</th>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Maximum Loss Percentage</th>");
            resultHtml.append("<th>Similarity Score</th>");
            resultHtml.append("</tr>");
    
            // Build HTML table rows from query results
            while (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("groupID")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("max_loss_percentage")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("similarity_score")).append("</td>");
                resultHtml.append("</tr>");
            }
    
            // Close HTML table
            resultHtml.append("</table>");
    
            // Close resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if total is not a valid integer
            e.printStackTrace();
            resultHtml.append("<p>Error: Total must be a valid integer.</p>");
        }
    
        return resultHtml.toString();
    }
    
    
    public static String test1(String country, String total) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(DATABASE);
        
            // SQL query with placeholders for parameters
            String sqlString =
                "WITH SelectedCommodityGroup AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "    WHERE" +
                "        c.commodityName = ?" + // Use placeholder for country parameter
                ")," +
                "SelectedGroupMinLossCommodity AS (" +
                "    SELECT" +
                "        c.commodityName," +
                "        MIN(cle.percentage) AS min_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    WHERE" +
                "        cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                "    GROUP BY" +
                "        c.commodityName" +
                "    ORDER BY" +
                "        min_loss_percentage ASC" +
                "    LIMIT 1" +
                ")," +
                "GroupMinLoss AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name," +
                "        MIN(cle.percentage) AS min_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    GROUP BY" +
                "        cg.groupID, cg.DESCRIPTOR" +
                ")," +
                "SimilarityScore AS (" +
                "    SELECT" +
                "        gml.groupID," +
                "        gml.group_name," +
                "        gml.min_loss_percentage," +
                "        ROUND(100 - ABS(gml.min_loss_percentage - (SELECT min_loss_percentage FROM SelectedGroupMinLossCommodity)), 3) AS similarity_score" +
                "    FROM" +
                "        GroupMinLoss gml" +
                "    WHERE" +
                "        gml.groupID <> (SELECT groupID FROM SelectedCommodityGroup)" +
                ")" +
                "SELECT" +
                "    ss.groupID," +
                "    ss.group_name," +
                "    ss.min_loss_percentage," +
                "    ss.similarity_score" +
                " FROM" +
                "    SimilarityScore ss" +
                " ORDER BY" +
                "    ss.similarity_score DESC" +
                " LIMIT ?"; // Use placeholder for total parameter
        
            // Log the query for debugging
            System.out.println("Executing query: " + sqlString);
        
            // Create PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setString(1, country); // Set the country parameter
            preparedStatement.setInt(2, Integer.parseInt(total)); // Set the total parameter
        
            // Execute query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
        
            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
        
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group ID</th>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Minimum Loss Percentage</th>");
            resultHtml.append("<th>Similarity Score</th>");
            resultHtml.append("</tr>");
        
            // Build HTML table rows from query results
            while (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("groupID")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("min_loss_percentage")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("similarity_score")).append("</td>");
                resultHtml.append("</tr>");
            }
        
            // Close HTML table
            resultHtml.append("</table>");
        
            // Close resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
        
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if total is not a valid integer
            e.printStackTrace();
            resultHtml.append("<p>Error: Total must be a valid integer.</p>");
        }
        
        return resultHtml.toString();
    }
    


    public static String compareGroups(String commodityName, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
    
        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);
    
            // Prepare the SQL query with placeholders for commodity name and limit
            String query = "WITH SelectedCommodityGroup AS (\n" +
                    "    SELECT cg.groupID, cg.DESCRIPTOR AS commodity_group\n" +
                    "    FROM Commodity AS c\n" +
                    "    JOIN CPC AS cp ON c.cpc_code = cp.cpc_code\n" +
                    "    JOIN CommodityGroup AS cg ON cp.groupID = cg.GroupID\n" +
                    "    WHERE c.commodityName = ?\n" +
                    "),\n" +
                    "WasteLossCounts AS (\n" +
                    "    SELECT\n" +
                    "        cg.DESCRIPTOR AS group_name,\n" +
                    "        SUM(CASE WHEN cl.supply_stage IN ('Retail', 'Households', 'Food Services', 'Market') THEN 1 ELSE 0 END) AS waste_count,\n" +
                    "        SUM(CASE WHEN cl.supply_stage NOT IN ('Retail', 'Households', 'Food Services', 'Market') THEN 1 ELSE 0 END) AS loss_count\n" +
                    "    FROM Commodity AS c\n" +
                    "    JOIN CPC AS cp ON c.cpc_code = cp.cpc_code\n" +
                    "    JOIN CommodityGroup AS cg ON cp.groupID = cg.GroupID\n" +
                    "    JOIN CountryLossEvent AS cl ON cl.cpc_code = cp.cpc_code\n" +
                    "    GROUP BY cg.DESCRIPTOR\n" +
                    "),\n" +
                    "TomatoesCounts AS (\n" +
                    "    SELECT\n" +
                    "        group_name,\n" +
                    "        waste_count AS tomatoes_waste_count,\n" +
                    "        loss_count AS tomatoes_loss_count,\n" +
                    "        CASE\n" +
                    "            WHEN loss_count > 0 THEN waste_count * 1.0 / loss_count\n" +
                    "            ELSE NULL\n" +
                    "        END AS tomatoes_waste_to_loss_ratio\n" +
                    "    FROM WasteLossCounts\n" +
                    "    WHERE group_name = (SELECT commodity_group FROM SelectedCommodityGroup)\n" +
                    "),\n" +
                    "Comparison AS (\n" +
                    "    SELECT\n" +
                    "        wc.group_name AS similar_group,\n" +
                    "        wc.waste_count,\n" +
                    "        wc.loss_count,\n" +
                    "        CASE\n" +
                    "            WHEN tc.tomatoes_loss_count > 0 THEN wc.waste_count * 1.0 / tc.tomatoes_loss_count\n" +
                    "            ELSE NULL\n" +
                    "        END AS waste_to_tomatoes_loss_ratio\n" +
                    "    FROM WasteLossCounts wc\n" +
                    "    CROSS JOIN TomatoesCounts tc\n" +
                    "    WHERE wc.group_name <> tc.group_name\n" +
                    ")\n" +
                    "SELECT\n" +
                    "    similar_group,\n" +
                    "    waste_count,\n" +
                    "    loss_count,\n" +
                    "    waste_to_tomatoes_loss_ratio\n" +
                    "FROM Comparison\n" +
                    "ORDER BY ABS(waste_to_tomatoes_loss_ratio - 1) ASC\n" +
                    "LIMIT ?;";
    
            // Create a PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, commodityName);
            preparedStatement.setInt(2, Integer.parseInt(numGroups));
    
            // Execute the query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
    
            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Similar Group</th>");
            resultHtml.append("<th>Waste Count</th>");
            resultHtml.append("<th>Loss Count</th>");
            resultHtml.append("<th>Waste to Loss Ratio</th>");
            resultHtml.append("</tr>");
    
            // Build HTML table rows from query results
            while (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("similar_group")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getInt("waste_count")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getInt("loss_count")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("waste_to_tomatoes_loss_ratio")).append("</td>");
                resultHtml.append("</tr>");
            }
    
            // Close HTML table
            resultHtml.append("</table>");
    
            // Close statement and connection
            resultSet.close();
            preparedStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if numGroups is not a valid integer
            e.printStackTrace();
            resultHtml.append("<p>Error: Number of groups must be a valid integer.</p>");
        }
    
        return resultHtml.toString();
    }
    

    public static String getRatio(String commodityName) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;

        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DATABASE);

            // Prepare the SQL query with placeholders for commodity name
            String query = "WITH SelectedCommodityGroup AS (\n" +
                    "    SELECT cg.groupID, cg.DESCRIPTOR AS commodity_group\n" +
                    "    FROM Commodity AS c\n" +
                    "    JOIN CPC AS cp ON c.cpc_code = cp.cpc_code\n" +
                    "    JOIN CommodityGroup AS cg ON cp.groupID = cg.GroupID\n" +
                    "    WHERE c.commodityName = ?\n" +
                    "),\n" +
                    "WasteLossCounts AS (\n" +
                    "    SELECT\n" +
                    "        cg.DESCRIPTOR AS group_name,\n" +
                    "        SUM(CASE WHEN cl.supply_stage IN ('Retail', 'Households', 'Food Services', 'Market') THEN 1 ELSE 0 END) AS waste_count,\n" +
                    "        SUM(CASE WHEN cl.supply_stage NOT IN ('Retail', 'Households', 'Food Services', 'Market') THEN 1 ELSE 0 END) AS loss_count\n" +
                    "    FROM Commodity AS c\n" +
                    "    JOIN CPC AS cp ON c.cpc_code = cp.cpc_code\n" +
                    "    JOIN CommodityGroup AS cg ON cp.groupID = cg.GroupID\n" +
                    "    JOIN CountryLossEvent AS cl ON cl.cpc_code = cp.cpc_code\n" +
                    "    WHERE cg.DESCRIPTOR = (SELECT commodity_group FROM SelectedCommodityGroup)\n" +
                    "    GROUP BY cg.DESCRIPTOR\n" +
                    ")\n" +
                    "SELECT\n" +
                    "    group_name,\n" +
                    "    waste_count,\n" +
                    "    loss_count,\n" +
                    "    CASE\n" +
                    "        WHEN loss_count > 0 THEN waste_count * 1.0 / loss_count\n" +
                    "        ELSE NULL\n" +
                    "    END AS waste_to_loss_ratio\n" +
                    "FROM WasteLossCounts;";

            // Create a PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, commodityName);

            // Execute the query and process results
            ResultSet resultSet = preparedStatement.executeQuery();

            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");

            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Waste Count</th>");
            resultHtml.append("<th>Loss Count</th>");
            resultHtml.append("<th>Waste to Loss Ratio</th>");
            resultHtml.append("</tr>");

            // Build HTML table rows from query results
            if (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getInt("waste_count")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getInt("loss_count")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("waste_to_loss_ratio")).append("</td>");
                resultHtml.append("</tr>");
            } else {
                resultHtml.append("<tr>");
                resultHtml.append("<td colspan='4'>No data found for the specified commodity.</td>");
                resultHtml.append("</tr>");
            }

            // Close HTML table
            resultHtml.append("</table>");

            // Close statement and connection
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        }

        return resultHtml.toString();
    }

    public static String getA1(String name, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        String query = "";

        try {
            connection = DriverManager.getConnection(DATABASE);

            // Query to get closest year for the specified country
            query = "SELECT c.country_name, MIN(cle.year) AS closest_year, AVG(cle.percentage) AS avg_percentage " +
                    "FROM CountryLossEvent cle " +
                    "JOIN Country c ON cle.m49_code = c.m49_code " +
                    "WHERE cle.year IN ( " +
                    "   SELECT cle.year " +
                    "   FROM CountryLossEvent cle " +
                    "   JOIN Country c ON cle.m49_code = c.m49_code " +
                    "   WHERE c.country_name = ? " +
                    "   GROUP BY cle.year " +
                    "   ORDER BY ABS(cle.year - ?) " +
                    "   LIMIT 1 " +
                    ") " +
                    "GROUP BY c.country_name " +
                    "HAVING closest_year IS NOT NULL " +
                    "ORDER BY ABS(avg_percentage - ( " +
                    "   SELECT AVG(cle2.percentage) " +
                    "   FROM CountryLossEvent cle2 " +
                    "   JOIN Country c2 ON cle2.m49_code = c2.m49_code " +
                    "   WHERE c2.country_name = ? " +
                    "     AND cle2.year = ( " +
                    "         SELECT cle.year " +
                    "         FROM CountryLossEvent cle " +
                    "         JOIN Country c ON cle.m49_code = c.m49_code " +
                    "         WHERE c.country_name = ? " +
                    "         ORDER BY ABS(cle.year - ?) " +
                    "         LIMIT 1 " +
                    "     ) " +
                    ")) ASC " +
                    "LIMIT 1";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, year);
           
            ResultSet result = preparedStatement.executeQuery();

            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");

            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Country</th>");
            resultHtml.append("<th>Year</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("</tr>");

            while (result.next()) {
                String itemName = result.getString("country_name");
                int closestYear = result.getInt("closest_year");
                double avgPercentage = result.getDouble("avg_percentage");

                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(itemName).append("</td>");
                resultHtml.append("<td>").append(closestYear).append("</td>");
                resultHtml.append("<td>").append(String.format("%.2f%%", avgPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }

            resultHtml.append("</table>");

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return resultHtml.toString();
    }


    public static String get2A(String name, String year) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

            // Query to get all CPC codes and descriptions for the specified country and year
            String query = "WITH CountryCPC AS (" +
                           "    SELECT DISTINCT cle.cpc_code, cpc.descriptor " +
                           "    FROM CountryLossEvent cle " +
                           "    JOIN country c ON cle.m49_code = c.m49_code " +
                           "    JOIN CPC cpc ON cle.cpc_code = cpc.cpc_code " +
                           "    WHERE c.country_name = ? " +
                           "), " +
                           "CountryYear AS (" +
                           "    SELECT COALESCE(" +
                           "        (SELECT MIN(year) FROM CountryLossEvent cle " +
                           "         JOIN country c ON cle.m49_code = c.m49_code " +
                           "         WHERE c.country_name = ? AND year >= ?), " +
                           "        (SELECT MAX(year) FROM CountryLossEvent cle " +
                           "         JOIN country c ON cle.m49_code = c.m49_code " +
                           "         WHERE c.country_name = ?), " +
                           "        ? " +
                           "    ) AS closest_year " +
                           ") " +
                           "SELECT cc.cpc_code, cc.descriptor " +
                           "FROM CountryCPC cc " +
                           "JOIN CountryYear cy ON 1=1 " +
                           "ORDER BY cc.cpc_code";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, year);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, year);

            resultSet = preparedStatement.executeQuery();

            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");

            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>CPC Code</th>");
            resultHtml.append("<th>Description</th>");
            resultHtml.append("</tr>");

            // Process the results
            while (resultSet.next()) {
                String cpcCode = resultSet.getString("cpc_code");
                String descriptor = resultSet.getString("descriptor");

                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(cpcCode).append("</td>");
                resultHtml.append("<td>").append(descriptor).append("</td>");
                resultHtml.append("</tr>");
            }

            resultHtml.append("</table>");

        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }

        return resultHtml.toString();
    }

    public static String getA11(String regionName, String year, String numGroups) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Query to get closest year for the specified region
            String query = "SELECT c.region_name, MIN(cle.year) AS closest_year, AVG(cle.percentage) AS avg_percentage " +
                           "FROM RegionLossEvent cle " +
                           "JOIN Region c ON cle.m49_code = c.m49_code " +
                           "WHERE cle.year IN ( " +
                           "   SELECT cle.year " +
                           "   FROM RegionLossEvent cle " +
                           "   JOIN Region c ON cle.m49_code = c.m49_code " +
                           "   WHERE c.region_name = ? " +
                           "   GROUP BY cle.year " +
                           "   ORDER BY ABS(cle.year - ?) " +
                           "   LIMIT 1 " +
                           ") " +
                           "GROUP BY c.region_name " +
                           "HAVING closest_year IS NOT NULL " +
                           "ORDER BY ABS(avg_percentage - ( " +
                           "   SELECT AVG(cle2.percentage) " +
                           "   FROM RegionLossEvent cle2 " +
                           "   JOIN Region c2 ON cle2.m49_code = c2.m49_code " +
                           "   WHERE c2.region_name = ? " +
                           "     AND cle2.year = ( " +
                           "         SELECT cle.year " +
                           "         FROM RegionLossEvent cle " +
                           "         JOIN Region c ON cle.m49_code = c.m49_code " +
                           "         WHERE c.region_name = ? " +
                           "         ORDER BY ABS(cle.year - ?) " +
                           "         LIMIT 1 " +
                           "     ) " +
                           ")) ASC " +
                           "LIMIT 1";
    
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, regionName);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, regionName);
            preparedStatement.setString(4, regionName);
            preparedStatement.setString(5, year);
    
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Region</th>");
            resultHtml.append("<th>Year</th>");
            resultHtml.append("<th>Average Percentage</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String region = resultSet.getString("region_name");
                int closestYear = resultSet.getInt("closest_year");
                double avgPercentage = resultSet.getDouble("avg_percentage");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(region).append("</td>");
                resultHtml.append("<td>").append(closestYear).append("</td>");
                resultHtml.append("<td>").append(String.format("%.2f%%", avgPercentage)).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }
    
    public static String get2A1(String regionName, String year) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // Query to get all CPC codes and descriptions for the specified region and year
            String query = "WITH RegionCPC AS (" +
                           "    SELECT DISTINCT cle.cpc_code, cpc.descriptor " +
                           "    FROM RegionLossEvent cle " +
                           "    JOIN Region c ON cle.m49_code = c.m49_code " +
                           "    JOIN CPC cpc ON cle.cpc_code = cpc.cpc_code " +
                           "    WHERE c.region_name = ? " +
                           "), " +
                           "RegionYear AS (" +
                           "    SELECT COALESCE(" +
                           "        (SELECT MIN(year) FROM RegionLossEvent cle " +
                           "         JOIN Region c ON cle.m49_code = c.m49_code " +
                           "         WHERE c.region_name = ? AND year >= ?), " +
                           "        (SELECT MAX(year) FROM RegionLossEvent cle " +
                           "         JOIN Region c ON cle.m49_code = c.m49_code " +
                           "         WHERE c.region_name = ?), " +
                           "        ? " +
                           "    ) AS closest_year " +
                           ") " +
                           "SELECT rc.cpc_code, rc.descriptor " +
                           "FROM RegionCPC rc " +
                           "JOIN RegionYear ry ON 1=1 " +
                           "ORDER BY rc.cpc_code";
    
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, regionName);
            preparedStatement.setString(2, regionName);
            preparedStatement.setString(3, year);
            preparedStatement.setString(4, regionName);
            preparedStatement.setString(5, year);
    
            resultSet = preparedStatement.executeQuery();
    
            // Start building the HTML table
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>CPC Code</th>");
            resultHtml.append("<th>Description</th>");
            resultHtml.append("</tr>");
    
            // Process the results
            while (resultSet.next()) {
                String cpcCode = resultSet.getString("cpc_code");
                String descriptor = resultSet.getString("descriptor");
    
                // Append each row of data to the table
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(cpcCode).append("</td>");
                resultHtml.append("<td>").append(descriptor).append("</td>");
                resultHtml.append("</tr>");
            }
    
            resultHtml.append("</table>");
    
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    
        return resultHtml.toString();
    }


    public static String testA(String country, String total) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // SQL query with placeholders for parameters
            String sqlString =
                "WITH SelectedCommodityGroup AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "    WHERE" +
                "        c.commodityName = ?" +
                ")," +
                "" +
                "GroupMaxLoss AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name," +
                "        MAX(cle.percentage) AS max_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    WHERE" +
                "        cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                "    GROUP BY" +
                "        cg.groupID, cg.DESCRIPTOR" +
                "    ORDER BY" +
                "        max_loss_percentage DESC" +
                "    LIMIT 1" +
                ")" +
                "" +
                "SELECT" +
                "    gm.groupID," +
                "    gm.group_name," +
                "    gm.max_loss_percentage" +
                " FROM" +
                "    GroupMaxLoss gm";
    
            // Log the query for debugging
            System.out.println("Executing query: " + sqlString);
    
            // Create PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setString(1, country); // Set the country parameter
    
            // Execute query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
    
            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group ID</th>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Maximum Loss Percentage</th>");
            resultHtml.append("</tr>");
    
            // Process the single result (since we limit to 1)
            if (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("groupID")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("max_loss_percentage")).append("</td>");
                resultHtml.append("</tr>");
            }
    
            // Close HTML table
            resultHtml.append("</table>");
    
            // Close resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if total is not a valid integer
            e.printStackTrace();
            resultHtml.append("<p>Error: Total must be a valid integer.</p>");
        }
    
        return resultHtml.toString();
    }
    
    public static String test1A(String country, String total) {
        StringBuilder resultHtml = new StringBuilder();
        Connection connection = null;
    
        try {
            connection = DriverManager.getConnection(DATABASE);
    
            // SQL query with placeholders for parameters
            String sqlString =
                "WITH SelectedCommodityGroup AS (" +
                "    SELECT" +
                "        cg.groupID," +
                "        cg.DESCRIPTOR AS group_name" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "    WHERE" +
                "        c.commodityName = ?" + // Use placeholder for country parameter
                ")," +
                "SelectedGroupMinLossCommodity AS (" +
                "    SELECT" +
                "        c.commodityName," +
                "        MIN(cle.percentage) AS min_loss_percentage" +
                "    FROM" +
                "        Commodity c" +
                "        JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "        JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                "    WHERE" +
                "        cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                "    GROUP BY" +
                "        c.commodityName" +
                "    ORDER BY" +
                "        min_loss_percentage ASC" +
                "    LIMIT 1" +
                ")" +
                "SELECT" +
                "    cg.groupID," +
                "    cg.DESCRIPTOR AS group_name," +
                "    MIN(cle.percentage) AS min_loss_percentage" +
                " FROM" +
                "    Commodity c" +
                "    JOIN Cpc cp ON c.cpc_code = cp.cpc_code" +
                "    JOIN CommodityGroup cg ON cp.groupID = cg.groupID" +
                "    JOIN CountryLossEvent cle ON cp.cpc_code = cle.cpc_code" +
                " WHERE" +
                "    cp.groupID = (SELECT groupID FROM SelectedCommodityGroup)" +
                " GROUP BY" +
                "    cg.groupID, cg.DESCRIPTOR";
    
            // Log the query for debugging
            System.out.println("Executing query: " + sqlString);
    
            // Create PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setString(1, country); // Set the country parameter
    
            // Execute query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
    
            // Build HTML table header
            resultHtml.append("<style>");
            resultHtml.append("table { width: 100%; border-collapse: collapse; }");
            resultHtml.append("th, td { padding: 10px; text-align: left; border: 1px solid #dddddd; }");
            resultHtml.append("th { background-color: #f2f2f2; }");
            resultHtml.append("</style>");
    
            resultHtml.append("<table>");
            resultHtml.append("<tr>");
            resultHtml.append("<th>Group ID</th>");
            resultHtml.append("<th>Group Name</th>");
            resultHtml.append("<th>Minimum Loss Percentage</th>");
            resultHtml.append("</tr>");
    
            // Build HTML table row from query result (should be only one result due to LIMIT 1)
            if (resultSet.next()) {
                resultHtml.append("<tr>");
                resultHtml.append("<td>").append(resultSet.getString("groupID")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getString("group_name")).append("</td>");
                resultHtml.append("<td>").append(resultSet.getDouble("min_loss_percentage")).append("</td>");
                resultHtml.append("</tr>");
            }
    
            // Close HTML table
            resultHtml.append("</table>");
    
            // Close resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            resultHtml.append("<p>Error retrieving data from the database.</p>");
        }
    
        return resultHtml.toString();
    }
    

}    