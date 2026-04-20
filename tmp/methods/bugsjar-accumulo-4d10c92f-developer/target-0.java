  private static String setUpOptions(ClassLoader classloader, final ConsoleReader reader, final String className, final Map<String,String> options)
      throws IOException,
      ShellCommandException {
    String input;
    OptionDescriber skvi;
    Class<? extends OptionDescriber> clazz;
    try {
      clazz = classloader.loadClass(className).asSubclass(OptionDescriber.class);
      skvi = clazz.newInstance();
    } catch (ClassNotFoundException e) {
      StringBuilder msg = new StringBuilder("Unable to load ").append(className);
      if (className.indexOf('.') < 0) {
        msg.append("; did you use a fully qualified package name?");
      } else {
        msg.append("; class not found.");
      }
      throw new ShellCommandException(ErrorCode.INITIALIZATION_FAILURE, msg.toString());
    } catch (InstantiationException e) {
      throw new IllegalArgumentException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e.getMessage());
    } catch (ClassCastException e) {
      StringBuilder msg = new StringBuilder("Loaded ");
      msg.append(className).append(" but it does not implement ");
      msg.append(OptionDescriber.class.getSimpleName());
      msg.append("; use 'config -s' instead.");
      throw new ShellCommandException(ErrorCode.INITIALIZATION_FAILURE, msg.toString());
    }
    
    final IteratorOptions itopts = skvi.describeOptions();
    if (itopts.getName() == null) {
      throw new IllegalArgumentException(className + " described its default distinguishing name as null");
    }
    String shortClassName = className;
    if (className.contains(".")) {
      shortClassName = className.substring(className.lastIndexOf('.') + 1);
    }
    final Map<String,String> localOptions = new HashMap<String,String>();
    do {
      // clean up the overall options that caused things to fail
      for (String key : localOptions.keySet()) {
        options.remove(key);
      }
      localOptions.clear();
      
      reader.printString(itopts.getDescription());
      reader.printNewline();
      
      String prompt;
      if (itopts.getNamedOptions() != null) {
        for (Entry<String,String> e : itopts.getNamedOptions().entrySet()) {
          prompt = Shell.repeat("-", 10) + "> set " + shortClassName + " parameter " + e.getKey() + ", " + e.getValue() + ": ";
          reader.flushConsole();
          input = reader.readLine(prompt);
          if (input == null) {
            reader.printNewline();
            throw new IOException("Input stream closed");
          } else {
            input = new String(input);
          }
          // Places all Parameters and Values into the LocalOptions, even if the value is "".
          // This allows us to check for "" values when setting the iterators and allows us to remove
          // the parameter and value from the table property.
          localOptions.put(e.getKey(), input);
        }
      }
      
      if (itopts.getUnnamedOptionDescriptions() != null) {
        for (String desc : itopts.getUnnamedOptionDescriptions()) {
          reader.printString(Shell.repeat("-", 10) + "> entering options: " + desc + "\n");
          input = "start";
          while (true) {
            prompt = Shell.repeat("-", 10) + "> set " + shortClassName + " option (<name> <value>, hit enter to skip): ";
            
            reader.flushConsole();
            input = reader.readLine(prompt);
            if (input == null) {
              reader.printNewline();
              throw new IOException("Input stream closed");
            } else {
              input = new String(input);
            }
            
            if (input.length() == 0)
              break;
            
            String[] sa = input.split(" ", 2);
            localOptions.put(sa[0], sa[1]);
          }
        }
      }
      
      options.putAll(localOptions);
      if (!skvi.validateOptions(options))
        reader.printString("invalid options for " + clazz.getName() + "\n");
      
    } while (!skvi.validateOptions(options));
    return itopts.getName();
  }
