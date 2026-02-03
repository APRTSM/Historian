    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

  if(getName().endsWith( "/ ")||  linkFlag  ==  DEFAULT_FILE_MODE)  {
            return true;
        }

        return false;
    }
