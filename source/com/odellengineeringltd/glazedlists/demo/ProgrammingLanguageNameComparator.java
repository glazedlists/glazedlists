import java.util.Comparator;

public class ProgrammingLanguageNameComparator implements Comparator {

    public int compare(Object a, Object b) {
        ProgrammingLanguage languageA = (ProgrammingLanguage)a;
        ProgrammingLanguage languageB = (ProgrammingLanguage)b;
        
        return languageA.getName().compareToIgnoreCase(languageB.getName());
    }
}
