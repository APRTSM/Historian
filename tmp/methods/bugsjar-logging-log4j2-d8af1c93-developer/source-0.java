    public String lookup(LogEvent event, String var) {
        if (var == null) {
            return null;
        }

        int prefixPos = var.indexOf(PREFIX_SEPARATOR);
        if (prefixPos >= 0) {
            String prefix = var.substring(0, prefixPos);
            String name = var.substring(prefixPos + 1);
            StrLookup lookup = lookups.get(prefix);
            String value = null;
            if (lookup != null) {
                value = event == null ? lookup.lookup(name) : lookup.lookup(event, name);
            }

            if (value != null) {
                return value;
            }
            var = var.substring(prefixPos);
        }
        if (defaultLookup != null) {
            return event == null ? defaultLookup.lookup(var) : defaultLookup.lookup(event, var);
        }
        return null;
    }
