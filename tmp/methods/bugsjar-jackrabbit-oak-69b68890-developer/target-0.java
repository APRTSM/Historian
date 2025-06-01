    private void updateSubject(@Nonnull TokenCredentials tc, @Nullable AuthInfo authInfo,
                               @Nullable Set<? extends Principal> principals) {
        if (!subject.isReadOnly()) {
            subject.getPublicCredentials().add(tc);

            if (principals != null) {
                subject.getPrincipals().addAll(principals);
            }

            if (authInfo != null) {
                setAuthInfo(authInfo, subject);
            }
        }
    }
    public boolean commit() throws LoginException {
        if (tokenCredentials != null) {
            updateSubject(tokenCredentials, getAuthInfo(tokenInfo), principals);
            return true;
        }

        if (tokenProvider != null && sharedState.containsKey(SHARED_KEY_CREDENTIALS)) {
            Credentials shared = getSharedCredentials();
            if (shared != null && tokenProvider.doCreateToken(shared)) {
                TokenInfo ti = tokenProvider.createToken(shared);
                if (ti != null) {
                    TokenCredentials tc = new TokenCredentials(ti.getToken());
                    Map<String, String> attributes = ti.getPrivateAttributes();
                    for (String name : attributes.keySet()) {
                        tc.setAttribute(name, attributes.get(name));
                    }
                    attributes = ti.getPublicAttributes();
                    for (String name : attributes.keySet()) {
                        tc.setAttribute(name, attributes.get(name));
                    }
                    sharedState.put(SHARED_KEY_ATTRIBUTES, attributes);
                    updateSubject(tc, null, null);
                } else {
                    // failed to create token -> fail commit()
                    log.debug("TokenProvider failed to create a login token for user " + userId);
                    throw new LoginException("Failed to create login token for user " + userId);
                }
            }
        }
        // the login attempt on this module did not succeed: clear state
        clearState();

        return false;
    }
    private AuthInfo getAuthInfo(@Nullable TokenInfo tokenInfo) {
        if (tokenInfo != null) {
            Map<String, Object> attributes = new HashMap<String, Object>();
            Map<String, String> publicAttributes = tokenInfo.getPublicAttributes();
            for (String attrName : publicAttributes.keySet()) {
                attributes.put(attrName, publicAttributes.get(attrName));
            }
            return new AuthInfoImpl(tokenInfo.getUserId(), attributes, principals);
        } else {
            return null;
        }
    }
