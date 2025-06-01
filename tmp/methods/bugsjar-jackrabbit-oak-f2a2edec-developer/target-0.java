    public static boolean validate(String jcrPath) {
        Listener listener = new Listener() {
            int depth;
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
                depth--;
                return !hasRoot || depth >= 0;
            }

            @Override
            public void error(String message) {
            }

            @Override
            public boolean name(String name, int index) {
                depth++;
                return true;
            }

        };
        return parse(jcrPath, listener);
    }
