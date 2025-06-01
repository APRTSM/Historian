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
