        public boolean isGranted(long permissions, @Nonnull PropertyState property) {
            EntryPredicate predicate = new EntryPredicate(tree, property, Permissions.respectParentPermissions(permissions));
            Iterator<PermissionEntry> it = concat(new LazyIterator(this, true, predicate), new LazyIterator(this, false, predicate));
            return hasPermissions(it, predicate, permissions, tree.getPath());
        }
        public boolean isGranted(long permissions) {
            EntryPredicate predicate = new EntryPredicate(tree, null, Permissions.respectParentPermissions(permissions));
            Iterator<PermissionEntry> it = concat(new LazyIterator(this, true, predicate), new LazyIterator(this, false, predicate));
            return hasPermissions(it, predicate, permissions, tree.getPath());
        }
    private boolean internalIsGranted(@Nonnull Tree tree, @Nullable PropertyState property, long permissions) {
        EntryPredicate predicate = new EntryPredicate(tree, property, Permissions.respectParentPermissions(permissions));
        return hasPermissions(getEntryIterator(predicate), predicate, permissions, tree.getPath());
    }
    public boolean isGranted(@Nonnull String path, long permissions) {
        EntryPredicate predicate = new EntryPredicate(path, Permissions.respectParentPermissions(permissions));
        return hasPermissions(getEntryIterator(predicate), predicate, permissions, path);
    }
    public RepositoryPermission getRepositoryPermission() {
        return new RepositoryPermission() {
            @Override
            public boolean isGranted(long repositoryPermissions) {
                EntryPredicate predicate = new EntryPredicate();
                return hasPermissions(getEntryIterator(predicate), predicate, repositoryPermissions, null);
            }
        };
    }
    private boolean hasPermissions(@Nonnull Iterator<PermissionEntry> entries,
                                   @Nonnull EntryPredicate predicate,
                                   long permissions, @Nullable String path) {
        // calculate readable paths if the given permissions includes any read permission.
        boolean isReadable = Permissions.diff(Permissions.READ, permissions) != Permissions.READ && readPolicy.isReadablePath(path, false);
        if (!entries.hasNext() && !isReadable) {
            return false;
        }

        boolean respectParent = (path != null) && Permissions.respectParentPermissions(permissions);

        long allows = (isReadable) ? Permissions.READ : Permissions.NO_PERMISSION;
        long denies = Permissions.NO_PERMISSION;

        PrivilegeBits allowBits = PrivilegeBits.getInstance();
        if (isReadable) {
            allowBits.add(bitsProvider.getBits(PrivilegeConstants.JCR_READ));
        }
        PrivilegeBits denyBits = PrivilegeBits.getInstance();
        PrivilegeBits parentAllowBits;
        PrivilegeBits parentDenyBits;
        String parentPath;

        if (respectParent) {
            parentAllowBits = PrivilegeBits.getInstance();
            parentDenyBits = PrivilegeBits.getInstance();
            parentPath = PermissionUtil.getParentPathOrNull(path);
        } else {
            parentAllowBits = PrivilegeBits.EMPTY;
            parentDenyBits = PrivilegeBits.EMPTY;
            parentPath = null;
        }

        while (entries.hasNext()) {
            PermissionEntry entry = entries.next();
            if (respectParent && (parentPath != null)) {
                boolean matchesParent = entry.matchesParent(parentPath);
                if (matchesParent) {
                    if (entry.isAllow) {
                        parentAllowBits.addDifference(entry.privilegeBits, parentDenyBits);
                    } else {
                        parentDenyBits.addDifference(entry.privilegeBits, parentAllowBits);
                    }
                }
            }

            if (entry.isAllow) {
                if (!respectParent || predicate.apply(entry, false)) {
                    allowBits.addDifference(entry.privilegeBits, denyBits);
                }
                long ap = PrivilegeBits.calculatePermissions(allowBits, parentAllowBits, true);
                allows |= Permissions.diff(ap, denies);
                if ((allows | ~permissions) == -1) {
                    return true;
                }
            } else {
                if (!respectParent || predicate.apply(entry, false)) {
                    denyBits.addDifference(entry.privilegeBits, allowBits);
                }
                long dp = PrivilegeBits.calculatePermissions(denyBits, parentDenyBits, false);
                denies |= Permissions.diff(dp, allows);
                if (Permissions.includes(denies, permissions)) {
                    return false;
                }
            }
        }

        return (allows | ~permissions) == -1;
    }
