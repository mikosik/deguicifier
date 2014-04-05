package com.perunlabs.deguicifier.testing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class SimpleJavaCompiler {
  private static final Charset CHARSET = Charset.forName("UTF-8");

  public static Provider<?> compileProvider(String sourceCode) {
    check(sourceCode != null);
    try {
      File tempDir = Files.createTempDirectory("deguicifier-test").toFile();
      String canonicalName = canonicalName(sourceCode);
      String path = canonicalName.replace('.', '/') + ".java";
      Path sourceFilePath = tempDir.toPath().resolve(path);
      writeToFile(sourceFilePath, sourceCode);

      String classPath = createClassPath(tempDir);
      compileAndFailOnErrors(tempDir, classPath, sourceFilePath.toFile());
      return (Provider<?>) createInstanceLoadedFromFile(tempDir, canonicalName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String canonicalName(String sourceCode) {
    String[] lines = sourceCode.split("\\r?\\n");
    String packageName = packageName(lines);
    String className = className(lines);
    if (packageName.isEmpty()) {
      return className;
    } else {
      return packageName + "." + className;
    }
  }

  private static String packageName(String[] lines) {
    String packageString = "package ";
    for (String line : lines) {
      if (line.startsWith(packageString)) {
        return line.substring(packageString.length(), line.length() - 1);
      }
    }
    return "";
  }

  private static String className(String[] lines) {
    String prefixesString = "public class ";
    for (String line : lines) {
      if (line.startsWith(prefixesString)) {
        String withouthPrefixes = line.substring(prefixesString.length());
        return withouthPrefixes.substring(0, withouthPrefixes.indexOf(' '));
      }
    }
    return "";
  }

  private static String createClassPath(File tempDir) {
    URLClassLoader classLoader = (URLClassLoader) SimpleJavaCompiler.class.getClassLoader();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(tempDir.toString());
    for (URL url : classLoader.getURLs()) {
      stringBuilder.append(File.pathSeparatorChar);
      stringBuilder.append(url.getFile());
    }
    return stringBuilder.toString();
  }

  private static void check(boolean condition) {
    if (!condition) {
      throw new RuntimeException();
    }
  }

  private static void writeToFile(Path sourceFile, String sourceCode) throws IOException {
    Files.createDirectories(sourceFile.getParent());
    Files.write(sourceFile, Arrays.asList(sourceCode), CHARSET);
  }

  public static void compileAndFailOnErrors(File destinationBaseDir, String classpath,
      File... files) throws IOException {
    DiagnosticCollector<JavaFileObject> diagnosticCollector =
        compileFiles(destinationBaseDir, classpath, files);
    if (problemsFound(diagnosticCollector)) {
      throw new RuntimeException("Compiling failed with:\n" + diagnosticCollector.getDiagnostics());
    }
  }

  private static boolean problemsFound(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR
          || diagnostic.getKind() == Diagnostic.Kind.MANDATORY_WARNING
          || diagnostic.getKind() == Diagnostic.Kind.WARNING) {
        return true;
      }
    }
    return false;
  }

  private static DiagnosticCollector<JavaFileObject> compileFiles(File destinationBaseDir,
      String classpath, File... files) throws IOException {
    DiagnosticCollector<JavaFileObject> diagnosticCollector =
        new DiagnosticCollector<JavaFileObject>();
    compileFiles(diagnosticCollector, destinationBaseDir, classpath, files);
    return diagnosticCollector;
  }

  private static void compileFiles(DiagnosticListener<? super JavaFileObject> diagnosticListener,
      File destinationBaseDir, String classpath, File... files) throws IOException {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, CHARSET);
    Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(files);
    List<String> options = Arrays.asList("-d", destinationBaseDir.toString(), "-cp", classpath);
    javaCompiler.getTask(null, null, diagnosticListener, options, null, fileObjects).call();
    fileManager.close();
  }

  public static Object createInstanceLoadedFromFile(File classesRootDir, String classToLoad)
      throws MalformedURLException, ClassNotFoundException, AssertionError {
    Class<?> klass = loadClassFromFile(classesRootDir, classToLoad);
    try {
      return klass.getConstructors()[0].newInstance();
    } catch (InstantiationException e) {
      throw new AssertionError(e);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (IllegalArgumentException e) {
      throw new AssertionError(e);
    } catch (SecurityException e) {
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e);
    }
  }

  private static Class<?> loadClassFromFile(File classesRootDir, String classToLoad)
      throws MalformedURLException, ClassNotFoundException {
    URL[] urls = new URL[] { new URL("file://" + classesRootDir + "/") };
    @SuppressWarnings("resource")
    URLClassLoader urlClassLoader =
        new URLClassLoader(urls, SimpleJavaCompiler.class.getClassLoader());
    return urlClassLoader.loadClass(classToLoad);
  }
}
