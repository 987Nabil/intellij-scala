package org.jetbrains.plugins.scala.nailgun;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGServer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * used from {@link org.jetbrains.plugins.scala.compiler.CompileServerLauncher}
 * @author Pavel Fatin
 */
@SuppressWarnings("JavadocReference")
public class NailgunRunner {

  /** NOTE: set of commands should be equal to commands from {@link org.jetbrains.jps.incremental.scala.remote.CommandIds} */
  private static final String[] COMMANDS = {
          "compile",
          "compile-jps",
          "get-metrics",
          "start-metering",
          "end-metering"
  };
  private static final String SERVER_DESCRIPTION = "Scala compile server";

  private static final String STOP_ALIAS_START = "stop_";
  private static final String STOP_CLASS_NAME = "com.martiansoftware.nailgun.builtins.NGStop";

  public static void main(String[] args) throws Exception {
    if (args.length != 4)
      throw new IllegalArgumentException("Usage: NailgunRunner [port] [id] [classpath] [system-dir-path]");

    int port = Integer.parseInt(args[0]);
    String id = args[1];
    String classpath = args[2];
    Path buildSystemDirPath = Paths.get(args[3]);

    URLClassLoader classLoader = constructClassLoader(classpath);

    TokensGenerator.generateAndWriteTokenFor(buildSystemDirPath, port);

    InetAddress address = InetAddress.getByName(null);
    NGServer server = createServer(address, port, id, buildSystemDirPath, classLoader);

    Thread thread = new Thread(server);
    thread.setName("NGServer(" + address.toString() + ", " + port + "," + id + ")");
    thread.setContextClassLoader(classLoader);
    thread.start();

    Runtime.getRuntime().addShutdownHook(new ShutdownHook(server, buildSystemDirPath));
  }

  /**
   * Extra class loader is required due to several reasons:
   * <p>
   * 1. Dotty compiler interfaces (used inside Main during compilation)
   * casts classloader to a URLClassloader, and in JRE 11 AppClassLoader is not an instance of URLClassloader.<br>
   * <p>
   * 2. In order to run REPL instances (aka iLoopWrapper) (for Worksheet in REPL mode) in compiler server process.
   * REPL instances can use arbitrary scala versions in runtime. However, Compile Server uses fixed scala version.
   * In order to interact with REPL in compile server `repl-interface` is used. It's written in java to avoid any
   * scala binary incompatibility errors at runtime.
   * <p>
   * Final classloader hierarchy looks like this
   * <pre>
   *         [PlatformClassLoader]
   *                   |
   *            [AppClassLoader] (pure java)
   *                   |
   *            [repl-interface] (pure java)
   *            /      |       \
   *           /       |    [JPS & Compiler Server jars] (scala version A)
   *          /        |
   *         /  [REPL instance 1] (scala version B)
   *        /
   *   [REPL instance 2] (scala version C)
   * </pre>
   * Where:<br>
   * [PlatformClassLoader] - contains all jvm classes<br>
   * [AppClassLoader] - contains jars required to run Nailgun itself (scala-nailgun-runner, nailgun.jar)<br>
   * [repl-interface] - contains jars through which repl instances interact with main CompileServer<br>
   * [JPS & Compiler Server jars] - contains main compiler server jars with fixed scala version (2.13.2 at this moment)<br>
   * [REPL instance N] - classloaders created for each REPL instance with arbitrary scala version
   *
   * @see org.jetbrains.jps.incremental.scala.local.worksheet.ILoopWrapperFactoryHandler#createClassLoader(org.jetbrains.plugins.scala.compiler.data.CompilerJars)
   */
  public static URLClassLoader constructClassLoader(String classpath) {
    Function<String, URL> pathToUrl = path -> {
      try {
        return new File(path).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    };

    URL[] urls = Stream.of(classpath.split(File.pathSeparator))
            .map(pathToUrl)
            .toArray(URL[]::new);

    //noinspection Convert2MethodRef
    URL[] replInterfaceUrls = Arrays.stream(urls).filter(it -> isReplInterfaceJar(it)).toArray(URL[]::new);
    if (replInterfaceUrls.length == 0) {
      throw new IllegalStateException("repl interface jar not found");
    }
    URL[] otherUrls = Arrays.stream(urls).filter(it -> !isReplInterfaceJar(it)).toArray(URL[]::new);

    URLClassLoader replLoader = new URLClassLoader(replInterfaceUrls, NailgunRunner.class.getClassLoader());
    return new URLClassLoader(otherUrls, replLoader);
  }

  private static boolean isReplInterfaceJar(URL url) {
    String urlString = url.toString();
    return urlString.contains("repl-interface.jar");
  }

  private static NGServer createServer(InetAddress address, int port, String id, Path buildSystemDir, URLClassLoader classLoader)
          throws Exception {
    NGServer server = new NGServer(address, port);

    server.setAllowNailsByClassName(false);

    Class<?> mainNailClass = Utils.loadAndSetupMainClass(classLoader, buildSystemDir);
    for (String command : COMMANDS) {
      server.getAliasManager().addAlias(new Alias(command, SERVER_DESCRIPTION, mainNailClass));
    }
    initShutdownTimer(mainNailClass, server);

    // TODO: token should be checked
    Class<?> stopClass = classLoader.loadClass(STOP_CLASS_NAME);
    String stopAlias = STOP_ALIAS_START + id;
    server.getAliasManager().addAlias(new Alias(stopAlias, "", stopClass));

    return server;
  }

  // we need the server to shut down on timer even if no compilation was done (nailMain is not invoked)
  private static void initShutdownTimer(Class<?> mainNailClass, NGServer server) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = mainNailClass.getMethod("initShutdownTimer", NGServer.class);
    method.invoke(null, server);
  }

  private static class ShutdownHook extends Thread {
    static final int TIMEOUT = 30;

    private final NGServer myServer;
    private final Path buildSystemDir;

    ShutdownHook(NGServer server, Path buildSystemDir) {
      myServer = server;
      this.buildSystemDir = buildSystemDir;
    }

    public void run() {
      TokensGenerator.deleteTokenFor(buildSystemDir, myServer.getPort());

      myServer.shutdown(false);

      for (int i = 0; i < TIMEOUT; i++) {
        if (!myServer.isRunning()) break;

        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // do nothing
        }
      }
    }
  }

  private static class TokensGenerator {

    static void generateAndWriteTokenFor(Path buildSystemDir, int port) throws IOException {
      Path path = tokenPathFor(buildSystemDir, port);
      writeTokenTo(path, UUID.randomUUID());
    }

    /** duplicated in {@link org.jetbrains.plugins.scala.server.CompileServerToken} */
    static Path tokenPathFor(Path buildSystemDir, int port) {
      return buildSystemDir.resolve("tokens").resolve(Integer.toString(port));
    }

    static void writeTokenTo(Path path, UUID uuid) throws IOException {
      File directory = path.getParent().toFile();

      if (!directory.exists()) {
        if (!directory.mkdirs()) {
          throw new IOException("Cannot create directory: " + directory);
        }
      }

      Files.write(path, uuid.toString().getBytes());

      PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
      if (view != null) {
        try {
          view.setPermissions(new HashSet<>(asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
        } catch (IOException e) {
          System.err.println("Cannot set permissions: " + path);
        }
      }
    }

    public static void deleteTokenFor(Path buildSystemDir, int port) {
      File tokenFile = tokenPathFor(buildSystemDir, port).toFile();
      if (!tokenFile.delete()) {
        tokenFile.deleteOnExit();
      }
    }
  }
}
