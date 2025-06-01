    private Property internalRemoveProperty(final String jcrName)
            throws RepositoryException {
        final String oakName = getOakName(checkNotNull(jcrName));
        return perform(new ItemWriteOperation<Property>("internalRemoveProperty") {
            @Override
            public void checkPreconditions() throws RepositoryException {
                super.checkPreconditions();
                if (!isCheckedOut()) {
                    throw new VersionException(
                            "Cannot remove property. Node is checked in.");
                }
            }
            @Nonnull
            @Override
            public Property perform() throws RepositoryException {
                PropertyDelegate property = dlg.getPropertyOrNull(oakName);
                if (property != null) {
                    property.remove();
                } else {
                    // Return an instance which throws on access; see OAK-395
                    property = dlg.getProperty(oakName);
                }
                return new PropertyImpl(property, sessionContext);
            }

            @Override
            public String toString() {
                return String.format("Removing property [%s]", jcrName);
            }
        });
    }
