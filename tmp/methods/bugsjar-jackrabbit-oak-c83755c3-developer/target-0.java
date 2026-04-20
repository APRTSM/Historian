    private ExternalUser createUser(@Nonnull Entry entry, @CheckForNull String id)
            throws LdapInvalidAttributeValueException {
        ExternalIdentityRef ref = new ExternalIdentityRef(entry.getDn().getName(), this.getName());
        if (id == null) {
            String idAttribute = config.getUserConfig().getIdAttribute();
            Attribute attr = entry.get(idAttribute);
            if (attr == null) {
                throw new LdapInvalidAttributeValueException(ResultCodeEnum.CONSTRAINT_VIOLATION,
                        "no value found for attribute '" + idAttribute + "' for entry " + entry);
            }
            id = attr.getString();
        }
        String path = config.getUserConfig().makeDnPath()
                ? createDNPath(entry.getDn())
                : null;
        LdapUser user = new LdapUser(this, ref, id, path);
        Map<String, Object> props = user.getProperties();
        applyAttributes(props, entry);
        return user;
    }
    private ExternalGroup createGroup(@Nonnull Entry entry, @CheckForNull String name)
            throws LdapInvalidAttributeValueException {
        ExternalIdentityRef ref = new ExternalIdentityRef(entry.getDn().getName(), this.getName());
        if (name == null) {
            String idAttribute = config.getGroupConfig().getIdAttribute();
            Attribute attr = entry.get(idAttribute);
            if (attr == null) {
                throw new LdapInvalidAttributeValueException(ResultCodeEnum.CONSTRAINT_VIOLATION,
                        "no value found for attribute '" + idAttribute + "' for entry " + entry);
            }
            name = attr.getString();
        }
        String path = config.getGroupConfig().makeDnPath()
                ? createDNPath(entry.getDn())
                : null;
        LdapGroup group = new LdapGroup(this, ref, name, path);
        Map<String, Object> props = group.getProperties();
        applyAttributes(props, entry);
        return group;

    }
