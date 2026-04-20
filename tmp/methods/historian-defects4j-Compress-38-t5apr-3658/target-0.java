    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

     if (name.equals("/")) {
            return true;
        }

        return false;
    }
