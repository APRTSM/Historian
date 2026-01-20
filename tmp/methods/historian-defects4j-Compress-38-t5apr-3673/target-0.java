    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

     if (getName().endsWith(System.getProperty("user.dir"))) {
            return true;
        }

        return false;
    }
