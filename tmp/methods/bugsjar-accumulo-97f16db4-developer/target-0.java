  public static FileSystemManager generateVfs() throws FileSystemException {
    DefaultFileSystemManager vfs = new DefaultFileSystemManager();
    vfs.addProvider("res", new org.apache.commons.vfs2.provider.res.ResourceFileProvider());
    vfs.addProvider("zip", new org.apache.commons.vfs2.provider.zip.ZipFileProvider());
    vfs.addProvider("gz", new org.apache.commons.vfs2.provider.gzip.GzipFileProvider());
    vfs.addProvider("ram", new org.apache.commons.vfs2.provider.ram.RamFileProvider());
    vfs.addProvider("file", new org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider());
    vfs.addProvider("jar", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("http", new org.apache.commons.vfs2.provider.http.HttpFileProvider());
    vfs.addProvider("https", new org.apache.commons.vfs2.provider.https.HttpsFileProvider());
    vfs.addProvider("ftp", new org.apache.commons.vfs2.provider.ftp.FtpFileProvider());
    vfs.addProvider("ftps", new org.apache.commons.vfs2.provider.ftps.FtpsFileProvider());
    vfs.addProvider("war", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("par", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("ear", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("sar", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("ejb3", new org.apache.commons.vfs2.provider.jar.JarFileProvider());
    vfs.addProvider("tmp", new org.apache.commons.vfs2.provider.temp.TemporaryFileProvider());
    vfs.addProvider("tar", new org.apache.commons.vfs2.provider.tar.TarFileProvider());
    vfs.addProvider("tbz2", new org.apache.commons.vfs2.provider.tar.TarFileProvider());
    vfs.addProvider("tgz", new org.apache.commons.vfs2.provider.tar.TarFileProvider());
    vfs.addProvider("bz2", new org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider());
    vfs.addProvider("hdfs", new HdfsFileProvider());
    vfs.addExtensionMap("jar", "jar");
    vfs.addExtensionMap("zip", "zip");
    vfs.addExtensionMap("gz", "gz");
    vfs.addExtensionMap("tar", "tar");
    vfs.addExtensionMap("tbz2", "tar");
    vfs.addExtensionMap("tgz", "tar");
    vfs.addExtensionMap("bz2", "bz2");
    vfs.addMimeTypeMap("application/x-tar", "tar");
    vfs.addMimeTypeMap("application/x-gzip", "gz");
    vfs.addMimeTypeMap("application/zip", "zip");
    vfs.setFileContentInfoFactory(new FileContentInfoFilenameFactory());
    vfs.setFilesCache(new SoftRefFilesCache());
    File cacheDir = computeTopCacheDir(); 
    vfs.setReplicator(new UniqueFileReplicator(cacheDir));
    vfs.setCacheStrategy(CacheStrategy.ON_RESOLVE);
    vfs.init();
    vfsInstances.add(new WeakReference<DefaultFileSystemManager>(vfs));
    return vfs;
  }
  private static File computeTopCacheDir() {
    String cacheDirPath = AccumuloClassLoader.getAccumuloString(VFS_CACHE_DIR, System.getProperty("java.io.tmpdir"));
    String procName = ManagementFactory.getRuntimeMXBean().getName();
    return new File(cacheDirPath, "accumulo-vfs-cache-" + procName + "-" + System.getProperty("user.name", "nouser"));
  }
