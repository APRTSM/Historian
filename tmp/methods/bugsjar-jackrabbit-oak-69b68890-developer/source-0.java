    private void updateSubject(@Nonnull TokenCredentials tc, @Nonnull AuthInfo authInfo,
                               @Nullable Set<? extends Principal> principals) {
        if (!subject.isReadOnly()) {
            subject.getPublicCredentials().add(tc);

            if (principals != null) {
                subject.getPrincipals().addAll(principals);
            }

            // replace all existing auth-info
            Set<AuthInfo> ais = subject.getPublicCredentials(AuthInfo.class);
            if (!ais.isEmpty()) {
                subject.getPublicCredentials().removeAll(ais);
            }
            subject.getPublicCredentials().add(authInfo);
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
                    updateSubject(tc, getAuthInfo(ti), null);
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
    private AuthInfo getAuthInfo(TokenInfo tokenInfo) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (tokenProvider != null && tokenInfo != null) {
            Map<String, String> publicAttributes = tokenInfo.getPublicAttributes();
            for (String attrName : publicAttributes.keySet()) {
                attributes.put(attrName, publicAttributes.get(attrName));
            }
        }
        return new AuthInfoImpl(userId, attributes, principals);
    }
