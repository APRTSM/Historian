    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

  if(getName().endsWith( "/ ")||  getName().endsWith( "prototype "))  {
            return true;
        }

        return false;
    }
