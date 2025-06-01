    UserManager getUserManager() throws UnsupportedRepositoryOperationException {
        if (userManager == null) {
            if (false) {
                userManager = securityProvider.getUserConfiguration().getUserManager(root, getNamePathMapper(), session);
            } else {
                throw new UnsupportedRepositoryOperationException("User management not supported.");
            }
        }
        return userManager;
    }
