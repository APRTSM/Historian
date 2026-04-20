  public MiniAccumuloCluster(MiniAccumuloConfig config) throws IOException {
    
    if (config.getDir().exists() && !config.getDir().isDirectory())
      throw new IllegalArgumentException("Must pass in directory, " + config.getDir() + " is a file");
    
    if (config.getDir().exists() && config.getDir().list().length != 0)
      throw new IllegalArgumentException("Directory " + config.getDir() + " is not empty");
    
    this.config = config;
    
    libDir = new File(config.getDir(), "lib");
    libExtDir = new File(libDir, "ext");
    confDir = new File(config.getDir(), "conf");
    accumuloDir = new File(config.getDir(), "accumulo");
    zooKeeperDir = new File(config.getDir(), "zookeeper");
    logDir = new File(config.getDir(), "logs");
    walogDir = new File(config.getDir(), "walogs");
    
    confDir.mkdirs();
    accumuloDir.mkdirs();
    zooKeeperDir.mkdirs();
    logDir.mkdirs();
    walogDir.mkdirs();
    libDir.mkdirs();
    
    // Avoid the classloader yelling that the general.dynamic.classpaths value is invalid because
    // $ACCUMULO_HOME/lib/ext isn't defined.
    libExtDir.mkdirs();
    
    zooKeeperPort = PortUtils.getRandomFreePort();
    
    File siteFile = new File(confDir, "accumulo-site.xml");
    
    OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(siteFile), Constants.UTF8);
    fileWriter.append("<configuration>\n");
    
    HashMap<String,String> siteConfig = new HashMap<String,String>(config.getSiteConfig());
    
    appendProp(fileWriter, Property.INSTANCE_DFS_URI, "file:///", siteConfig);
    appendProp(fileWriter, Property.INSTANCE_DFS_DIR, accumuloDir.getAbsolutePath(), siteConfig);
    appendProp(fileWriter, Property.INSTANCE_ZK_HOST, "localhost:" + zooKeeperPort, siteConfig);
    appendProp(fileWriter, Property.INSTANCE_SECRET, INSTANCE_SECRET, siteConfig);
    appendProp(fileWriter, Property.TSERV_PORTSEARCH, "true", siteConfig);
    appendProp(fileWriter, Property.LOGGER_DIR, walogDir.getAbsolutePath(), siteConfig);
    appendProp(fileWriter, Property.TSERV_DATACACHE_SIZE, "10M", siteConfig);
    appendProp(fileWriter, Property.TSERV_INDEXCACHE_SIZE, "10M", siteConfig);
    appendProp(fileWriter, Property.TSERV_MAXMEM, "50M", siteConfig);
    appendProp(fileWriter, Property.TSERV_WALOG_MAX_SIZE, "100M", siteConfig);
    appendProp(fileWriter, Property.TSERV_NATIVEMAP_ENABLED, "false", siteConfig);
    appendProp(fileWriter, Property.TRACE_TOKEN_PROPERTY_PREFIX + ".password", config.getRootPassword(), siteConfig);
    appendProp(fileWriter, Property.GC_CYCLE_DELAY, "4s", siteConfig);
    appendProp(fileWriter, Property.GC_CYCLE_START, "0s", siteConfig);
    mergePropWithRandomPort(siteConfig, Property.MASTER_CLIENTPORT.getKey());
    mergePropWithRandomPort(siteConfig, Property.TRACE_PORT.getKey());
    mergePropWithRandomPort(siteConfig, Property.TSERV_CLIENTPORT.getKey());
    mergePropWithRandomPort(siteConfig, Property.MONITOR_PORT.getKey());
    mergePropWithRandomPort(siteConfig, Property.GC_PORT.getKey());
    mergePropWithRandomPort(siteConfig, Property.MONITOR_LOG4J_PORT.getKey());
    
    // since there is a small amount of memory, check more frequently for majc... setting may not be needed in 1.5
    appendProp(fileWriter, Property.TSERV_MAJC_DELAY, "3", siteConfig);
    
    // ACCUMULO-1472 -- Use the classpath, not what might be installed on the system.
    // We have to set *something* here, otherwise the AccumuloClassLoader will default to pulling from
    // environment variables (e.g. ACCUMULO_HOME, HADOOP_HOME/PREFIX) which will result in multiple copies
    // of artifacts on the classpath as they'll be provided by the invoking application
    appendProp(fileWriter, Property.GENERAL_CLASSPATHS, libDir.getAbsolutePath() + "/[^.].*.jar", siteConfig);
    appendProp(fileWriter, Property.GENERAL_DYNAMIC_CLASSPATHS, libExtDir.getAbsolutePath() + "/[^.].*.jar", siteConfig);
    
    for (Entry<String,String> entry : siteConfig.entrySet())
      fileWriter.append("<property><name>" + entry.getKey() + "</name><value>" + entry.getValue() + "</value></property>\n");
    fileWriter.append("</configuration>\n");
    fileWriter.close();
    
    zooCfgFile = new File(confDir, "zoo.cfg");
    fileWriter = new OutputStreamWriter(new FileOutputStream(zooCfgFile), Constants.UTF8);
    
    // zookeeper uses Properties to read its config, so use that to write in order to properly escape things like Windows paths
    Properties zooCfg = new Properties();
    zooCfg.setProperty("tickTime", "1000");
    zooCfg.setProperty("initLimit", "10");
    zooCfg.setProperty("syncLimit", "5");
    zooCfg.setProperty("clientPort", zooKeeperPort + "");
    zooCfg.setProperty("maxClientCnxns", "100");
    zooCfg.setProperty("dataDir", zooKeeperDir.getAbsolutePath());
    zooCfg.store(fileWriter, null);
    
    fileWriter.close();
  }
