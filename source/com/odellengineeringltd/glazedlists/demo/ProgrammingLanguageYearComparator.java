import java.util.Comparator;

public class ProgrammingLanguageYearComparator implements Comparator {

    public int compare(Object a, Object b) {
        ProgrammingLanguage languageA = (ProgrammingLanguage)a;
        ProgrammingLanguage languageB = (ProgrammingLanguage)b;
        
        return languageA.getYear().compareTo(languageB.getYear());
    }
}
