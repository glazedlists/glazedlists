import com.odellengineeringltd.glazedlists.*;

class ProgrammingLanguage implements Filterable {
    private String name;
    private String year;
    private String description;
    private boolean objectOriented;
    private boolean virtualMachine;
    
    public ProgrammingLanguage(String name, String year, String description, boolean objectOriented, boolean virtualMachine) {
        this.name = name;
        this.year = year;
        this.description = description;
        this.objectOriented = objectOriented;
        this.virtualMachine = virtualMachine;
    }

    public String[] getFilterStrings() {
        return new String[] { name, year, description };
    }
    
    public String getName() {
        return name;
    }
    
    public String getYear() {
        return year;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isObjectOriented() {
        return objectOriented;
    }
    
    public boolean isVirtualMachine() {
        return virtualMachine;
    }
}

