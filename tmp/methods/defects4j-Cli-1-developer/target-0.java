    public String[] getOptionValues(String opt)
    {
        Option key = resolveOption( opt );

        if (options.contains(key))
        {
            return key.getValues();
        }

        return null;
        }
    public Iterator iterator()
    {
        return options.iterator();
    }
    public Option[] getOptions()
    {
        Collection processed = options;

        // reinitialise array
        Option[] optionsArray = new Option[processed.size()];

        // return the array
        return (Option[]) processed.toArray(optionsArray);
    }
    public Object getOptionObject(String opt)
    {
        String res = getOptionValue(opt);

        Option option = resolveOption(opt);
        if (option == null)
        {
            return null;
        }

        Object type = option.getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }
    public boolean hasOption(String opt)
    {
        return options.contains( resolveOption(opt));
    }
    private Option resolveOption( String opt )
    {
        opt = Util.stripLeadingHyphens(opt);
        for ( Iterator it = options.iterator(); it.hasNext(); )
        {
            Option option = (Option) it.next();
            if (opt.equals(option.getOpt()))
            {
                return option;
            }
            if (opt.equals( option.getLongOpt()))
            {
                return option;
        }

        }
        return null;
    }
    void addOption(Option opt)
    {
        options.add(opt);
    }
