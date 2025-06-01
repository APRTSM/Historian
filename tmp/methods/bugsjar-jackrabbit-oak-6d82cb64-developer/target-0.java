    public static int getDepth(String path) {
        assert isValid(path);

        if (path.isEmpty()) {
            return 0;
        }
        int count = 1, i = 0;
        if (isAbsolutePath(path)) {
            if (denotesRootPath(path)) {
                return 0;
            }
            i++;
        }
        while (true) {
            i = path.indexOf('/', i) + 1;
            if (i == 0) {
                return count;
            }
            count++;
        }
    }
