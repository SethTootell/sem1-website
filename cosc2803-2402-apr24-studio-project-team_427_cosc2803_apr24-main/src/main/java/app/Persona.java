package app;

import java.util.ArrayList;

public class Persona {
    private int persona_id;
    private String name;
    private String image_path;
    private ArrayList<PersonaAttribute> attributes;

    public Persona(int persona_id, String name, String image_path, ArrayList<PersonaAttribute> attributes) {
        this.persona_id = persona_id;
        this.name = name;
        this.image_path = image_path;
        this.attributes = attributes;
    }

    public int getId() {
        return this.persona_id;
    }

    public String getName() {
        return this.name;
    }

    public String getImagePath() {
        return this.image_path;
    }

    public ArrayList<PersonaAttribute> getAttributes() {
        return this.attributes;
    }

    public ArrayList<PersonaAttribute> filterByAttributeType(String attrType) {
        ArrayList<PersonaAttribute> filteredAttributes = new ArrayList<>();
        for (PersonaAttribute attr : this.attributes) {
            if (attr.getType().equals(attrType)) {
                filteredAttributes.add(attr);
            }
        }
        return filteredAttributes;
    }

    /* 
        Returns a string with attribute descriptions in HTML list tags
        i.e. <li>My Attribute</li> 
    */
}
