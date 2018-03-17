package ca.odell.glazedlists;

import java.io.IOException;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.runner.RunnerException;

public class BenchmarkMain {

    public static void main(String[] args) {
        try {
            Main.main(args);
        } catch (RunnerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
