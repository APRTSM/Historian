    private ExternalGroup createGroup(@Nonnull Entry entry, @CheckForNull String name)
            throws LdapInvalidAttributeValueException {
        ExternalIdentityRef ref = new ExternalIdentityRef(entry.getDn().getName(), this.getName());
        if (name == null) {
            name = entry.get(config.getGroupConfig().getIdAttribute()).getString();
        }
        String path = config.getGroupConfig().makeDnPath()
                ? createDNPath(entry.getDn())
                : null;
        LdapGroup group = new LdapGroup(this, ref, name, path);
        Map<String, Object> props = group.getProperties();
        applyAttributes(props, entry);
        return group;

    }
    private ExternalUser createUser(@Nonnull Entry entry, @CheckForNull String id)
            throws LdapInvalidAttributeValueException {
        ExternalIdentityRef ref = new ExternalIdentityRef(entry.getDn().getName(), this.getName());
        if (id == null) {
            id = entry.get(config.getUserConfig().getIdAttribute()).getString();
        }
        String path = config.getUserConfig().makeDnPath()
                ? createDNPath(entry.getDn())
                : null;
        LdapUser user = new LdapUser(this, ref, id, path);
        Map<String, Object> props = user.getProperties();
        applyAttributes(props, entry);
        return user;
    }
