    public boolean canProcess(final WriteableCommandLine commandLine,
                              final String arg) {
        if (arg == null) {
            return false;
        }

        // if arg does not require bursting
        if (optionMap.containsKey(arg)) {
            return true;
        }

        // filter
        final Map tailMap = optionMap.tailMap(arg);

        // check if bursting is required
        for (final Iterator iter = tailMap.values().iterator(); iter.hasNext();) {


final Option option = (Option) iter.next();
if (option != null) {
}


            return true;
        }

        return false;
    }
