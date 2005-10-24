package ca.odell.glazedlists.impl.java15;

import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.main.JavaCompiler;

import java.io.File;
import java.io.IOException;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Remove Java 5 language features from a source file.
 */
public class Declawer {
    public static void main(String[] args) throws Throwable {
        if(args.length < 3) {
            System.out.println("Usage: Declawer <inpath> <classpath> <outdir>");
        }
        String inpath = args[0];
        String classpath = args[1];
        String outdir = args[2];
        System.out.println("Using inpath: " + inpath);
        System.out.println("Using classpath: " + classpath);
        System.out.println("Using outdir: " + outdir);

        Context context = new Context();
        Options options = Options.instance(context);
        options.put("-s", "");
        options.put("-d", outdir);
        options.put("-classpath", classpath);
        JavaCompiler javaCompiler = JavaCompiler.instance(context);

        List<String> files = List.of(null);
        files.remove(null);
        files.addAll(listFilesAsStringsFromPath(inpath));
        javaCompiler.compile(files);
    }

    /**
     * Adds files (but not directories) in the specified file or directory.
     */
    private static void addFileRecursively(File file, ArrayList<File> target) {
        if(file.isDirectory()) {
            for(File child: file.listFiles(JAVA_FILE_OR_DIRECTORY)) {
                addFileRecursively(child, target);
            }
        } else {
            target.add(file);
        }
    }
    private static Collection<String> listFilesAsStringsFromPath(String path) {
        try {
            // populate an arraylist of files
            final ArrayList<File> files = new ArrayList<File>();
            final String[] pathElements = path.split("[:;]");
            for (int i = 0; i < pathElements.length; i++) {
                addFileRecursively(new File(pathElements[i]), files);
            }

            // trade that for an array of strings
            String[] result = new String[files.size()];
            for(int f = 0; f < files.size(); f++) {
                result[f] = files.get(f).getCanonicalPath();
            }
            return Arrays.asList(result);
        } catch(IOException e) {
            throw new RuntimeException("Failed to fetch full path from file");
        }
    }
    private static final FileFilter JAVA_FILE_OR_DIRECTORY = new FileFilter() {
        public boolean accept(File pathname) {
            try {
                return pathname.isDirectory() || pathname.getName().endsWith(".java")
                   && pathname.getCanonicalPath().indexOf("swt") == -1
                   && pathname.getCanonicalPath().indexOf("quickr") == -1
                   && pathname.getCanonicalPath().indexOf("java15") == -1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
}