package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import helper.DBHelper;

/*
 * Namespace for the ST3B page filters
 */
public class ST3BFilter {
    
    // returns a string of all foods in a <option> HTML tag in alphabetical order
    public static String getFoodforDropdown() {
        String str = new String();

        Connection connection = null;
        String query = "SELECT commodityName FROM Commodity ORDER BY commodityName ASC";
        try {
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                String food = result.getString("commodityName");
                 str += "<option value = \"%s_value\">%s</option>%n".formatted(food, food);
            }
        } catch (SQLException e) { 
            System.err.println(e.getMessage());
        }
        System.out.println(str);
        return str;
    }

}
