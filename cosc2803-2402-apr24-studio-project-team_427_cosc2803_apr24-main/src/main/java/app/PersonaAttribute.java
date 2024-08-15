package app;

import java.sql.SQLException;

import helper.DBHelper;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PersonaAttribute {
    String PersonaAttributeType;
    String Description;

    public PersonaAttribute(String PersonaAttributeType, String Description) {
        this.PersonaAttributeType = PersonaAttributeType;
        this.Description = Description;
    }

    public String getType() {
        return this.PersonaAttributeType;
    }

    public String getDesc() {
        return this.Description;
    }

    public static ArrayList<PersonaAttribute> getAttributesByID(int persona_id) throws SQLException {
        String query = "SELECT persona_attr_type, descriptor FROM PersonaAttribute WHERE persona_id = " + persona_id;
        ArrayList<PersonaAttribute> attributes = new ArrayList<PersonaAttribute>();

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DBHelper.DATABASE);

            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(query);

            while (result.next())
                attributes.add(new PersonaAttribute(result.getString("persona_attr_type"),
                    result.getString("descriptor")));
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return attributes;
    }


    public static String attrsInList(ArrayList<PersonaAttribute> attributes) {
        String str = new String();
        
        for (PersonaAttribute attr : attributes) str += "<li>%s</li>%n".formatted(attr.getDesc());

        return str;
    }
}
