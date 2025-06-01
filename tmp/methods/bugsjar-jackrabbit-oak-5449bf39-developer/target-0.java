    public static boolean isAncestor(String ancestor, String path) {
        assert isValid(ancestor);
        assert isValid(path);
        if (ancestor.isEmpty() || path.isEmpty()) {
            return false;
        }
        if (denotesRoot(ancestor)) {
            if (denotesRoot(path)) {
                return false;
            }
        }
        else {
            ancestor += "/";
        }
        return path.startsWith(ancestor);
    }
