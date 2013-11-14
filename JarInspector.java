// Seth Hoenig 2013
// I herby declare this piece of crap to be in the public domain.

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.lang.reflect.Method;

public final class JarInspector {

    public static void main(String[] args) throws IOException, InterruptedException, MalformedURLException {
        if (args.length != 1) {
            System.err.println("Usage: java JarInspector [jar file]");
            return;
        }

        System.out.println("Going to inspect jar file: " + args[0]);

        File jf = getJarFile(args[0]);
        System.out.println("jf path: " + jf.getCanonicalPath());

        String rawlist = execute(new String[]{"jar", "-tf", jf.getCanonicalPath()});

        List<String> classes = listOfClassFiles(rawlist);

        for (String s : classes) {
            String className = path2pkg(s);
            System.out.println("className: " + className);

            Class<?> c = extractClassFromJar(jf, className);
            if (c != null) {
                System.out.println("  EXTRACTED: " + c);
                processClass(c);
            }
            else
                System.out.println("  FAILED");
        }        
    }

    private static File getJarFile(String jarname) {
        File f = new File(jarname);
        if (!f.exists())
            throw new IllegalArgumentException(jarname + " does not exist");
        return f;
    }

    private static Class<?> extractClassFromJar(File jar, String klass) throws MalformedURLException {
        URL jurl = jar.toURI().toURL();
        URL[] jurls = new URL[]{jurl};
        ClassLoader loader = new URLClassLoader(jurls);
        try {
            Class<?> c = loader.loadClass(klass);
            return c;
        } catch(Throwable t) { // screw you too, Java
            return null;
        }
    }

    private static void processClass(Class<?> klass) {
        Method[] methods = null;
        try {
            methods = klass.getDeclaredMethods();
        } catch(Throwable t) {
            System.out.println("  FAILED getDeclaredMethods()");
            return;
        }
        for(Method m : methods)
            System.out.println("    " + m);
    }

    private static String execute(String[] cmd) throws IOException, InterruptedException {
        StringBuffer out = new StringBuffer();
        Process proc = Runtime.getRuntime().exec(cmd);
        proc.waitFor();

        InputStream is = proc.getInputStream();
        int c;
        while ((c = is.read()) != -1)
            out.append((char)c);

        is.close();

        return out.toString();
    }

    private static List<String> listOfClassFiles(String rawOutput) {
        List<String> result = new ArrayList<>();
        String[] toks = rawOutput.split("\\s+");
        for (String line : toks)
            if (line.endsWith(".class"))
                result.add(line);
        return result;
    }

    private static String path2pkg(String path) {
        return path.replaceAll("/", ".").replaceAll(".class", "");
    }
}
