    public void changeFileName(String newName) {
        LOG.trace("Changing name to: {}", newName);

        // Make sure the names is normalized.
        String newFileName = FileUtil.normalizePath(newName);
        String newEndpointPath = FileUtil.normalizePath(endpointPath.endsWith("" + File.separatorChar) ? endpointPath : endpointPath + File.separatorChar);

        LOG.trace("Normalized endpointPath: {}", newEndpointPath);
        LOG.trace("Normalized newFileName: ()", newFileName);

        File file = new File(newFileName);
        if (!absolute) {
            // for relative then we should avoid having the endpoint path duplicated so clip it
            if (ObjectHelper.isNotEmpty(newEndpointPath) && newFileName.startsWith(newEndpointPath)) {
                // clip starting endpoint in case it was added
                // use File.separatorChar as the normalizePath uses this as path separator so we should use the same
                // in this logic here
                if (newEndpointPath.endsWith("" + File.separatorChar)) {
                    newFileName = ObjectHelper.after(newFileName, newEndpointPath);
                } else {
                    newFileName = ObjectHelper.after(newFileName, newEndpointPath + File.separatorChar);
                }

                // reconstruct file with clipped name
                file = new File(newFileName);
            }
        }

        // store the file name only
        setFileNameOnly(file.getName());
        setFileName(file.getName());

        // relative path
        if (file.getParent() != null) {
            setRelativeFilePath(file.getParent() + getFileSeparator() + file.getName());
        } else {
            setRelativeFilePath(file.getName());
        }

        // absolute path
        if (isAbsolute(newFileName)) {
            setAbsolute(true);
            setAbsoluteFilePath(newFileName);
        } else {
            setAbsolute(false);
            // construct a pseudo absolute filename that the file operations uses even for relative only
            String path = ObjectHelper.isEmpty(endpointPath) ? "" : endpointPath + getFileSeparator();
            setAbsoluteFilePath(path + getRelativeFilePath());
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("FileNameOnly: {}", getFileNameOnly());
            LOG.trace("FileName: {}", getFileName());
            LOG.trace("Absolute: {}", isAbsolute());
            LOG.trace("Relative path: {}", getRelativeFilePath());
            LOG.trace("Absolute path: {}", getAbsoluteFilePath());
            LOG.trace("Name changed to: {}", this);
        }
    }
