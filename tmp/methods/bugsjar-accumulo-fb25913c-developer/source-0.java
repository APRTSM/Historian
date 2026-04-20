  private static ReloadingClassLoader createDynamicClassloader(final ClassLoader parent) throws FileSystemException, IOException {
    String dynamicCPath = AccumuloClassLoader.getAccumuloString(DYNAMIC_CLASSPATH_PROPERTY_NAME, DEFAULT_DYNAMIC_CLASSPATH_VALUE);

    String envJars = System.getenv("ACCUMULO_XTRAJARS");
    if (null != envJars && !envJars.equals(""))
      if (dynamicCPath != null && !dynamicCPath.equals(""))
        dynamicCPath = dynamicCPath + "," + envJars;
      else
        dynamicCPath = envJars;

    ReloadingClassLoader wrapper = new ReloadingClassLoader() {
      @Override
      public ClassLoader getClassLoader() {
        return parent;
      }
    };

    if (dynamicCPath == null || dynamicCPath.equals(""))
      return wrapper;

    // TODO monitor time for lib/ext was 1 sec... should this be configurable? - ACCUMULO-1301
    return new AccumuloReloadingVFSClassLoader(dynamicCPath, generateVfs(), wrapper, 1000, true);
  }
  static FileObject[] resolve(FileSystemManager vfs, String uris, ArrayList<FileObject> pathsToMonitor) throws FileSystemException {
    if (uris == null)
      return new FileObject[0];

    ArrayList<FileObject> classpath = new ArrayList<FileObject>();

    pathsToMonitor.clear();

    for (String path : uris.split(",")) {

      path = path.trim();

      if (path.equals(""))
        continue;

      path = AccumuloClassLoader.replaceEnvVars(path, System.getenv());

      FileObject fo = vfs.resolveFile(path);

      switch (fo.getType()) {
        case FILE:
        case FOLDER:
          classpath.add(fo);
          pathsToMonitor.add(fo);
          break;
        case IMAGINARY:
          // assume its a pattern
          String pattern = fo.getName().getBaseName();
          if (fo.getParent() != null && fo.getParent().getType() == FileType.FOLDER) {
            pathsToMonitor.add(fo.getParent());
            FileObject[] children = fo.getParent().getChildren();
            for (FileObject child : children) {
              if (child.getType() == FileType.FILE && child.getName().getBaseName().matches(pattern)) {
                classpath.add(child);
              }
            }
          } else {
            log.warn("ignoring classpath entry " + fo);
          }
          break;
        default:
          log.warn("ignoring classpath entry " + fo);
          break;
      }

    }

    return classpath.toArray(new FileObject[classpath.size()]);
  }
  public static ClassLoader getClassLoader() throws IOException {
    ReloadingClassLoader localLoader = loader;
    while (null == localLoader) {
      synchronized (lock) {
        if (null == loader) {

          FileSystemManager vfs = generateVfs();

          // Set up the 2nd tier class loader
          if (null == parent) {
            parent = AccumuloClassLoader.getClassLoader();
          }

          FileObject[] vfsCP = resolve(vfs, AccumuloClassLoader.getAccumuloString(VFS_CLASSLOADER_SYSTEM_CLASSPATH_PROPERTY, ""));

          if (vfsCP.length == 0) {
            localLoader = createDynamicClassloader(parent);
            loader = localLoader;
            return localLoader.getClassLoader();
          }

          // Create the Accumulo Context ClassLoader using the DEFAULT_CONTEXT
          localLoader = createDynamicClassloader(new VFSClassLoader(vfsCP, vfs, parent));
          loader = localLoader;
        }
      }
    }

    return localLoader.getClassLoader();
  }
  public static synchronized ContextManager getContextManager() throws IOException {
    if (contextManager == null) {
      getClassLoader();
      contextManager = new ContextManager(generateVfs(), new ReloadingClassLoader() {
        @Override
        public ClassLoader getClassLoader() {
          try {
            return AccumuloVFSClassLoader.getClassLoader();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }

    return contextManager;
  }
  public static void printClassPath(Printer out) {
    try {
      ClassLoader cl = getClassLoader();
      ArrayList<ClassLoader> classloaders = new ArrayList<ClassLoader>();

      while (cl != null) {
        classloaders.add(cl);
        cl = cl.getParent();
      }

      Collections.reverse(classloaders);

      int level = 0;

      for (ClassLoader classLoader : classloaders) {
        if (level > 0)
          out.print("");
        level++;

        String classLoaderDescription;

        switch (level) {
          case 1:
            classLoaderDescription = level + ": Java System Classloader (loads Java system resources)";
            break;
          case 2:
            classLoaderDescription = level + ": Java Classloader (loads everything defined by java classpath)";
            break;
          case 3:
            classLoaderDescription = level + ": Accumulo Classloader (loads everything defined by general.classpaths)";
            break;
          case 4:
            classLoaderDescription = level + ": Accumulo Dynamic Classloader (loads everything defined by general.dynamic.classpaths)";
            break;
          default:
            classLoaderDescription = level + ": Mystery Classloader (someone probably added a classloader and didn't update the switch statement in "
                + AccumuloVFSClassLoader.class.getName() + ")";
            break;
        }

        if (classLoader instanceof URLClassLoader) {
          // If VFS class loader enabled, but no contexts defined.
          URLClassLoader ucl = (URLClassLoader) classLoader;
          out.print("Level " + classLoaderDescription + " URL classpath items are:");

          for (URL u : ucl.getURLs()) {
            out.print("\t" + u.toExternalForm());
          }

        } else if (classLoader instanceof VFSClassLoader) {
          out.print("Level " + classLoaderDescription + " VFS classpaths items are:");
          VFSClassLoader vcl = (VFSClassLoader) classLoader;
          for (FileObject f : vcl.getFileObjects()) {
            out.print("\t" + f.getURL().toExternalForm());
          }
        } else {
          out.print("Unknown classloader configuration " + classLoader.getClass());
        }
      }

    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
