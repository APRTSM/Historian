    public static GenericFile<File> asGenericFile(String endpointPath, File file) {
        GenericFile<File> answer = new GenericFile<File>();
        // use file specific binding
        answer.setBinding(new FileBinding());

        answer.setEndpointPath(endpointPath);
        answer.setFile(file);
        answer.setFileNameOnly(file.getName());
        answer.setFileLength(file.length());
        // must use FileUtil.isAbsolute to have consistent check for whether the file is
        // absolute or not. As windows do not consider \ paths as absolute where as all
        // other OS platforms will consider \ as absolute. The logic in Camel mandates
        // that we align this for all OS. That is why we must use FileUtil.isAbsolute
        // to return a consistent answer for all OS platforms.
        answer.setAbsolute(FileUtil.isAbsolute(file));
        answer.setAbsoluteFilePath(file.getAbsolutePath());
        answer.setLastModified(file.lastModified());

        // compute the file path as relative to the starting directory
        File path;
        String endpointNormalized = FileUtil.normalizePath(endpointPath);
        if (file.getPath().startsWith(endpointNormalized)) {
            // skip duplicate endpoint path
            path = new File(ObjectHelper.after(file.getPath(), endpointNormalized + File.separator));
        } else {
            path = new File(file.getPath());
        }

        if (path.getParent() != null) {
            answer.setRelativeFilePath(path.getParent() + File.separator + file.getName());
        } else {
            answer.setRelativeFilePath(path.getName());
        }

        // the file name should be the relative path
        answer.setFileName(answer.getRelativeFilePath());

        // use file as body as we have converters if needed as stream
        answer.setBody(file);
        return answer;
    }
