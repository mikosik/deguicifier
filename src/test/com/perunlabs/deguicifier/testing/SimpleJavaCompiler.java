package com.perunlabs.deguicifier.testing;

import static com.perunlabs.deguicifier.Deguicifier.FACTORY_CLASS_NAME;

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

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class SimpleJavaCompiler {
  private static final Charset CHARSET = Charset.forName("UTF-8");

  public static Object compiledInstance(String sourceCode) {
    check(sourceCode != null);
    try {
      File tempDir = Files.createTempDirectory("deguicifier-test").toFile();
      Path sourceFilePath = tempDir.toPath().resolve(FACTORY_CLASS_NAME + ".java");
      writeToFile(sourceFilePath, sourceCode);

      compileAndFailOnErrors(tempDir, "", sourceFilePath.toFile());
      return createInstanceLoadedFromFile(tempDir, FACTORY_CLASS_NAME);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void check(boolean condition) {
    if (!condition) {
      throw new RuntimeException();
    }
  }

  private static void writeToFile(Path sourceFile, String sourceCode) throws IOException {
    Files.write(sourceFile, Arrays.asList(sourceCode), CHARSET);
  }

  public static void compileAndFailOnErrors(File destinationBaseDir, String classpath,
      File... files) throws IOException {
    DiagnosticCollector<JavaFileObject> diagnosticCollector =
        compileFiles(destinationBaseDir, classpath, files);
    if (!diagnosticCollector.getDiagnostics().isEmpty()) {
      throw new RuntimeException("Compiling failed with:\n" + diagnosticCollector.getDiagnostics());
    }
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