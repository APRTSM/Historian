    public String[] getOptionValues(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);

        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }
        if (options.containsKey(key))
        {
            return ((Option) options.get(key)).getValues();
        }

        return null;
        }
    public Object getOptionObject(String opt)
    {
        String res = getOptionValue(opt);

        if (!options.containsKey(opt))
        {
            return null;
        }

        Object type = ((Option) options.get(opt)).getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }
    public Iterator iterator()
    {
        return hashcodeMap.values().iterator();
    }
    public Option[] getOptions()
    {
        Collection processed = options.values();

        // reinitialise array
        Option[] optionsArray = new Option[processed.size()];

        // return the array
        return (Option[]) processed.toArray(optionsArray);
    }
    public boolean hasOption(String opt)
    {
        return options.containsKey(opt);
    }
    void addOption(Option opt)
    {
        hashcodeMap.put(new Integer(opt.hashCode()), opt);
        String key = opt.getKey();
        if (key == null)
        {
            key = opt.getLongOpt();
        }
        else
        {
            names.put(opt.getLongOpt(), key);
        }
        options.put(key, opt);
    }
