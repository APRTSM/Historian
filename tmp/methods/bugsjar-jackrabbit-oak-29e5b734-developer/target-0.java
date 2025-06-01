    public AccessControlPolicy[] getEffectivePolicies(@Nonnull Set<Principal> principals) throws RepositoryException {
        Util.checkValidPrincipals(principals, principalManager);
        Root r = getLatestRoot();

        Result aceResult = searchAces(principals, r);
        Set<JackrabbitAccessControlList> effective = Sets.newTreeSet(new Comparator<JackrabbitAccessControlList>() {
            @Override
            public int compare(JackrabbitAccessControlList list1, JackrabbitAccessControlList list2) {
                if (list1.equals(list2)) {
                    return 0;
                } else {
                    String p1 = list1.getPath();
                    String p2 = list2.getPath();

                    if (p1 == null) {
                        return -1;
                    } else if (p2 == null) {
                        return 1;
                    } else {
                        int depth1 = PathUtils.getDepth(p1);
                        int depth2 = PathUtils.getDepth(p2);
                        return (depth1 == depth2) ? p1.compareTo(p2) : Ints.compare(depth1, depth2);
                    }

                }
            }
        });

        Set<String> paths = Sets.newHashSet();
        for (ResultRow row : aceResult.getRows()) {
            String acePath = row.getPath();
            String aclName = Text.getName(Text.getRelativeParent(acePath, 1));

            Tree accessControlledTree = r.getTree(Text.getRelativeParent(acePath, 2));
            if (aclName.isEmpty() || !accessControlledTree.exists()) {
                log.debug("Isolated access control entry -> ignore query result at " + acePath);
                continue;
            }

            String path = (REP_REPO_POLICY.equals(aclName)) ? null : accessControlledTree.getPath();
            if (paths.contains(path)) {
                continue;
            }
            JackrabbitAccessControlList policy = createACL(path, accessControlledTree, true);
            if (policy != null) {
                effective.add(policy);
                paths.add(path);
            }
        }
        return effective.toArray(new AccessControlPolicy[effective.size()]);
    }
