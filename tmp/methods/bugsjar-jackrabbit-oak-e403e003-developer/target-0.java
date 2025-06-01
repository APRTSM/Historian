    public String getOakPathOrThrow(String jcrPath) throws RepositoryException {
        String oakPath = getOakPath(jcrPath);
        if (oakPath != null) {
            return oakPath;
        } else {
            // check if the path is an SNS path with an index > 1 and throw a PathNotFoundException instead (see OAK-1216)
            if (getOakPathKeepIndex(jcrPath) != null) {
                throw new PathNotFoundException(jcrPath);
            } else {
                throw new RepositoryException("Invalid name or path: " + jcrPath);
            }

        }
    }
