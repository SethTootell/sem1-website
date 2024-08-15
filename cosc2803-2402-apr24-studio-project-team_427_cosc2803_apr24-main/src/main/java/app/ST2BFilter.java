package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import helper.DBHelper;

public class ST2BFilter {
    public static String getFoodGroupsInDropdown() {
        String str = new String();

        String query = "SELECT descriptor FROM CommodityGroup";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                String desc = result.getString("descriptor");
                str += "<option value = \"%s\">%s</option>%n".formatted(desc, desc);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return str;
    }

    public static String generateTable(String foodGroup, String yearStart, String yearEnd, boolean isActivityActive,
            boolean isFoodSupplyStageActive, boolean isCauseOfLossActive, String sort) {
        String str = "<table class='table-section'>\n";
        // aggregate table
        String minYearQuery = """
                WITH MinYear AS (
                    SELECT MIN(cl.year) as min_year
                    FROM CountryLossEvent as cl
                    JOIN CPC as cp ON cp.cpc_code = cl.cpc_code
                    JOIN CommodityGroup cg ON cg.groupID = cp.groupID
                    WHERE cl.year >= ?
                        AND cg.DESCRIPTOR = ?
                )
                SELECT my.min_year,
                    (SELECT AVG(cl.percentage)
                        FROM CountryLossEvent as cl
                        JOIN CPC as cp ON cp.cpc_code = cl.cpc_code
                        JOIN CommodityGroup as cg ON cg.groupID = cp.groupID
                        WHERE cl.year = my.min_year
                            AND cg.DESCRIPTOR = ?) AS average_percentage
                FROM MinYear as my
                        """;

        String maxYearQuery = """
WITH maxYear AS (
    SELECT max(cl.year) as max_year
    FROM CountryLossEvent as cl
    JOIN CPC as cp ON cp.cpc_code = cl.cpc_code
    JOIN CommodityGroup cg ON cg.groupID = cp.groupID
    WHERE cl.year <= ?
        AND cg.DESCRIPTOR = ?
)
SELECT my.max_year,
    (SELECT AVG(cl.percentage)
        FROM CountryLossEvent as cl
        JOIN CPC as cp ON cp.cpc_code = cl.cpc_code
        JOIN CommodityGroup as cg ON cg.groupID = cp.groupID
        WHERE cl.year = my.max_year
            AND cg.DESCRIPTOR = ?) AS average_percentage
FROM maxYear as my
                        """;

        try {
            Connection connection = null;
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            PreparedStatement preparedMinYear = connection.prepareStatement(minYearQuery);
            PreparedStatement preparedMaxYear = connection.prepareStatement(maxYearQuery);

            System.out.println("DEBUG: food group: " + foodGroup + " yearStart: " + yearStart + " yearEnd: " + yearEnd);

            preparedMinYear.setString(1, yearStart);
            preparedMinYear.setString(2, foodGroup);
            preparedMinYear.setString(3, foodGroup);

            preparedMaxYear.setString(1, yearEnd);
            preparedMaxYear.setString(2, foodGroup);
            preparedMaxYear.setString(3, foodGroup);

            ResultSet minYearSet = preparedMinYear.executeQuery();
            ResultSet maxYearSet = preparedMaxYear.executeQuery();

            int minYear = minYearSet.getInt("min_year");
            double averagePercentageMin = minYearSet.getFloat("average_percentage");

            int maxYear = maxYearSet.getInt("max_year");
            double averagePercentageMax = maxYearSet.getFloat("average_percentage");

            double percentageDifference = averagePercentageMin - averagePercentageMax;

            // aggregate information
            str += """
                    <tr>
                        <th>Food Group</th>
                        <th>First Year</th>
                        <th>First Year Avg %</th>
                        <th>Max Year</th>
                        <th>Max Year Avg %</th>
                        <th>Percentage Change</th>
                    </tr>
                        """;

            str += """
                    <tr>
                        <td>%s</td>
                        <td>%d</td>
                        <td>%.2f</td>
                        <td>%d</td>
                        <td>%.2f</td>
                        <td>%.2f</td>
                    </tr>
                        """.formatted(foodGroup, minYear, averagePercentageMin, maxYear, averagePercentageMax,
                    percentageDifference);
            str += "</table>\n";

            // show i"SELECT cg.DESCRIPTOR, year, percentage";
            String dataQuery = """
                        SELECT cg.DESCRIPTOR, year, percentage, activity, cause, supply_stage
                            FROM CountryLossEvent as co
                                JOIN CPC as cp ON cp.cpc_code = co.cpc_code
                                JOIN CommodityGroup as cg ON cg.groupID = cp.groupID
                                WHERE year BETWEEN ? AND ?
                                AND cg.DESCRIPTOR = ?
                                ORDER BY PERCENTAGE %s
                    """.formatted(sort);
            System.out.println(dataQuery);

            dataQuery += """
                    """; // 1 = minYear, 2 = maxYear, 3 = foodGroup

            PreparedStatement statement = connection.prepareStatement(dataQuery);

            statement.setString(1, yearStart);
            statement.setString(2, yearEnd);
            statement.setString(3, foodGroup);

            ResultSet dataResult = statement.executeQuery();

            str += "<table class='table-section'>\n";

            str += """
                    <tr>
                        <th>Food Group</th>
                        <th>Year</th>
                        <th>Percentage Loss/Waste</th>
                    """;
            str += isActivityActive ? "<th>Activity</th>" : "";
            str += isCauseOfLossActive ? "<th>Cause of Loss</th>" : "";
            str += isFoodSupplyStageActive ? "<th>Supply Stage</th>" : "";
            str += "</tr>";

            while (dataResult.next()) {
                str += "<tr>";
                String descriptor = dataResult.getString("DESCRIPTOR");
                int year = dataResult.getInt("year");
                double percentage = dataResult.getDouble("percentage");
                String activity = isActivityActive ? dataResult.getString("activity") : null;
                String cause = isCauseOfLossActive ? dataResult.getString("cause") : null;
                String supplyStage = isFoodSupplyStageActive ? dataResult.getString("supply_stage") : null;

                str += "<td>%s</td>".formatted(descriptor);
                str += "<td>%s</td>".formatted(year);
                // If percentage < 0.01, state so
                str += percentage > 0.01 ? "<td>%.2f</td>".formatted(percentage) : "<td>< 0.01%</td>";

                // If information exists, show the information in the appropiate column, else,
                // state the information isn't available(is waste);
                str += isActivityActive
                        ? "<td>%s</td>".formatted(!activity.isEmpty() ? activity : "<i>No available information</i>")
                        : "";
                str += isCauseOfLossActive
                        ? "<td>%s</td>".formatted(!cause.isEmpty() ? cause : "<i>No available information</i>")
                        : "";
                str += isFoodSupplyStageActive
                        ? "<td>%s</td>"
                                .formatted(!supplyStage.isEmpty() ? supplyStage : "<i>No available information</i>")
                        : "";

                str += "</tr>";
            }
            str += "</table>";

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str;
    }

}
