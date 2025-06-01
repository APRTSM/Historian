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
    private void internalSetValue(@Nonnull final Value[] values)
            throws RepositoryException {
        if (values.length > MV_PROPERTY_WARN_THRESHOLD) {
            LOG.warn("Large multi valued property [{}] detected ({} values).",dlg.getPath(), values.length);
        }

        sessionDelegate.performVoid(new ItemWriteOperation("internalSetValue") {
            @Override
            public void checkPreconditions() throws RepositoryException {
                super.checkPreconditions();
                if (!getParent().isCheckedOut()) {
                    throw new VersionException(
                            "Cannot set property. Node is checked in.");
                }
            }

            @Override
            public void performVoid() throws RepositoryException {
                Type<?> type = dlg.getPropertyState().getType();
                if (!type.isArray()) {
                    throw new ValueFormatException(
                            "This is a single-valued property");
                }

                List<Value> converted = newArrayListWithCapacity(values.length);
                ValueFactory factory = getValueFactory();
                for (Value value : values) {
                    if (value != null) {
                        converted.add(ValueHelper.convert(
                                value, type.tag(), factory));
                    }
                }
                dlg.setState(createMultiState(dlg.getName(), converted, type));
            }

            @Override
            public String toString() {
                return String.format("Setting property [%s/%s]", dlg.getPath(), dlg.getName());
            }
        });
    }
    public void remove() throws RepositoryException {
        sessionDelegate.performVoid(new ItemWriteOperation("remove") {
            @Override
            public void checkPreconditions() throws RepositoryException {
                super.checkPreconditions();
                if (!getParent().isCheckedOut()) {
                    throw new VersionException(
                            "Cannot set property. Node is checked in.");
                }
            }

            @Override
            public void performVoid() {
                dlg.remove();
            }

            @Override
            public String toString() {
                return String.format("Removing property [%s/%s] ", dlg.getPath(), dlg.getName());
            }
        });
    }
    private void internalSetValue(@Nonnull final Value value)
            throws RepositoryException {
        sessionDelegate.performVoid(new ItemWriteOperation("internalSetValue") {
            @Override
            public void checkPreconditions() throws RepositoryException {
                super.checkPreconditions();
                if (!getParent().isCheckedOut()) {
                    throw new VersionException(
                            "Cannot set property. Node is checked in.");
                }
            }

            @Override
            public void performVoid() throws RepositoryException {
                Type<?> type = dlg.getPropertyState().getType();
                if (type.isArray()) {
                    throw new ValueFormatException(
                            "This is a multi-valued property");
                }

                Value converted = ValueHelper.convert(
                        value, type.tag(), getValueFactory());
                dlg.setState(createSingleState(dlg.getName(), converted, type));
            }

            @Override
            public String toString() {
                return String.format("Setting property [%s/%s]", dlg.getPath(), dlg.getName());
            }
        });
    }
