    protected void syncMembership(@Nonnull ExternalIdentity external, @Nonnull Authorizable auth, long depth)
            throws RepositoryException {
        if (depth <= 0) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Syncing membership '{}' -> '{}'", external.getExternalId().getString(), auth.getID());
        }

        final DebugTimer timer = new DebugTimer();
        Iterable<ExternalIdentityRef> externalGroups;
        try {
            externalGroups = external.getDeclaredGroups();
        } catch (ExternalIdentityException e) {
            log.error("Error while retrieving external declared groups for '{}'", external.getId(), e);
            return;
        }
        timer.mark("fetching");

        // first get the set of the existing groups that are synced ones
        Map<String, Group> declaredExternalGroups = new HashMap<String, Group>();
        Iterator<Group> grpIter = auth.declaredMemberOf();
        while (grpIter.hasNext()) {
            Group grp = grpIter.next();
            if (isSameIDP(grp)) {
                declaredExternalGroups.put(grp.getID(), grp);
            }
        }
        timer.mark("reading");

        for (ExternalIdentityRef ref : externalGroups) {
            log.debug("- processing membership {}", ref.getId());
            // get group
            ExternalGroup extGroup;
            try {
                ExternalIdentity extId = idp.getIdentity(ref);
                if (extId instanceof ExternalGroup) {
                    extGroup = (ExternalGroup) extId;
                } else {
                    log.warn("No external group found for ref '{}'.", ref.getString());
                    continue;
                }
            } catch (ExternalIdentityException e) {
                log.warn("Unable to retrieve external group '{}' from provider.", ref.getString(), e);
                continue;
            }
            log.debug("- idp returned '{}'", extGroup.getId());

            Group grp;
            Authorizable a = userManager.getAuthorizable(extGroup.getId());
            if (a == null) {
                grp = createGroup(extGroup);
                log.debug("- created new group");
            } else if (a.isGroup()) {
                grp = (Group) a;
            } else {
                log.warn("Authorizable '{}' is not a group, but should be one.", extGroup.getId());
                continue;
            }
            log.debug("- user manager returned '{}'", grp);

            syncGroup(extGroup, grp);

            // ensure membership
            grp.addMember(auth);
            log.debug("- added '{}' as member to '{}'", auth, grp);

            // remember the declared group
            declaredExternalGroups.remove(grp.getID());

            // recursively apply further membership
            if (depth > 1) {
                log.debug("- recursively sync group membership of '{}' (depth = {}).", grp.getID(), depth);
                syncMembership(extGroup, grp, depth - 1);
            } else {
                log.debug("- group nesting level for '{}' reached", grp.getID());
            }
        }
        timer.mark("adding");
        // remove us from the lost membership groups
        for (Group grp : declaredExternalGroups.values()) {
            grp.removeMember(auth);
            log.debug("- removing member '{}' for group '{}'", auth.getID(), grp.getID());
        }
        if (log.isDebugEnabled()) {
            timer.mark("removing");
            log.debug("syncMembership({}) {}", external.getId(), timer.getString());
        }
    }
