    public static boolean validate(String jcrPath) {
        Listener listener = new Listener() {
            boolean hasRoot;
            @Override
            public boolean root() {
                if (hasRoot) {
                    return false;
                }
                else {
                    hasRoot = true;
                    return true;
                }
            }

            @Override
            public boolean current() {
                return true;
            }

            @Override
            public boolean parent() {
                return true;
            }

            @Override
            public void error(String message) {
            }

            @Override
            public boolean name(String name, int index) {
                return true;
            }

        };
        return parse(jcrPath, listener);
    }
    public String getJcrPath(String oakPath) {
        final List<String> elements = new ArrayList<String>();

        if ("/".equals(oakPath)) {
            // avoid the need to special case the root path later on
            return "/";
        }

        JcrPathParser.Listener listener = new JcrPathParser.Listener() {
            @Override
            public boolean root() {
                if (!elements.isEmpty()) {
                    throw new IllegalArgumentException("/ on non-empty path");
                }
                elements.add("");
                return true;
            }

            @Override
            public boolean current() {
                // nothing to do here
                return false;
            }

            @Override
            public boolean parent() {
                if (elements.isEmpty() || "..".equals(elements.get(elements.size() - 1))) {
                    elements.add("..");
                    return true;
                }
                elements.remove(elements.size() - 1);
                return true;
            }

            @Override
            public void error(String message) {
                throw new IllegalArgumentException(message);
            }

            @Override
            public boolean name(String name, int index) {
                if (index > 1) {
                    throw new IllegalArgumentException("index > 1");
                }
                String p = nameMapper.getJcrName(name);
                elements.add(p);
                return true;
            }
        };

        JcrPathParser.parse(oakPath, listener);

        // empty path: map to "."
        if (elements.isEmpty()) {
            return ".";
        }

        StringBuilder jcrPath = new StringBuilder();
        for (String element : elements) {
            if (element.isEmpty()) {
                // root
                jcrPath.append('/');
            }
            else {
                jcrPath.append(element);
                jcrPath.append('/');
            }
        }

        jcrPath.deleteCharAt(jcrPath.length() - 1);
        return jcrPath.toString();
    }
    private String getOakPath(String jcrPath, final boolean keepIndex) {
        if ("/".equals(jcrPath)) {
            // avoid the need to special case the root path later on
            return "/";
        }

        int length = jcrPath.length();

        // identifier path?
        if (length > 0 && jcrPath.charAt(0) == '[') {
            if (jcrPath.charAt(length - 1) != ']') {
                // TODO error handling?
                log.debug("Could not parse path " + jcrPath + ": unterminated identifier");
                return null;
            }
            if (this.idManager == null) {
                // TODO error handling?
                log.debug("Could not parse path " + jcrPath + ": could not resolve identifier");
                return null;
            }
            return this.idManager.getPath(jcrPath.substring(1, length - 1));
        }

        boolean hasClarkBrackets = false;
        boolean hasIndexBrackets = false;
        boolean hasColon = false;
        boolean hasNameStartingWithDot = false;
        boolean hasTrailingSlash = false;

        char prev = 0;
        for (int i = 0; i < length; i++) {
            char c = jcrPath.charAt(i);

            if (c == '{' || c == '}') {
                hasClarkBrackets = true;
            } else if (c == '[' || c == ']') {
                hasIndexBrackets = true;
            } else if (c == ':') {
                hasColon = true;
            } else if (c == '.' && (prev == 0 || prev == '/')) {
                hasNameStartingWithDot = true;
            } else if(c == '/' && i == (length - 1)){
                hasTrailingSlash = true;
            }

            prev = c;
        }

        // try a shortcut
        if (!hasNameStartingWithDot && !hasClarkBrackets && !hasIndexBrackets) {
            if (!hasColon || !hasSessionLocalMappings()) {
                if (JcrPathParser.validate(jcrPath)) {
                    if(hasTrailingSlash){
                        return jcrPath.substring(0, length - 1);
                    }
                    return jcrPath;
                }
                else {
                    log.debug("Invalid path: {}", jcrPath);
                    return null;
                }
            }
        }

        final List<String> elements = new ArrayList<String>();
        final StringBuilder parseErrors = new StringBuilder();

        JcrPathParser.Listener listener = new JcrPathParser.Listener() {

            @Override
            public boolean root() {
                if (!elements.isEmpty()) {
                    parseErrors.append("/ on non-empty path");
                    return false;
                }
                elements.add("");
                return true;
            }

            @Override
            public boolean current() {
                // nothing to do here
                return true;
            }

            @Override
            public boolean parent() {
                if (elements.isEmpty() || "..".equals(elements.get(elements.size() - 1))) {
                    elements.add("..");
                    return true;
                }
                elements.remove(elements.size() - 1);
                return true;
            }

            @Override
            public void error(String message) {
                parseErrors.append(message);
            }

            @Override
            public boolean name(String name, int index) {
                if (!keepIndex && index > 1) {
                    parseErrors.append("index > 1");
                    return false;
                }
                String p = nameMapper.getOakName(name);
                if (p == null) {
                    parseErrors.append("Invalid name: ").append(name);
                    return false;
                }
                if (keepIndex && index > 0) {
                    p += "[" + index + ']';
                }
                elements.add(p);
                return true;
            }
        };

        JcrPathParser.parse(jcrPath, listener);
        if (parseErrors.length() != 0) {
            log.debug("Could not parse path " + jcrPath + ": " + parseErrors.toString());
            return null;
        }

        // Empty path maps to ""
        if (elements.isEmpty()) {
            return "";
        }

        StringBuilder oakPath = new StringBuilder();
        for (String element : elements) {
            if (element.isEmpty()) {
                // root
                oakPath.append('/');
            }
            else {
                oakPath.append(element);
                oakPath.append('/');
            }
        }

        // root path is special-cased early on so it does not need to
        // be considered here
        oakPath.deleteCharAt(oakPath.length() - 1);
        return oakPath.toString();
    }
