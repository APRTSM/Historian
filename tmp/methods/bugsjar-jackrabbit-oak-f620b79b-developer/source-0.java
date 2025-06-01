    private static String getKeyValue(String key, String userId) {
        return key + userId;
    }
    public TokenInfo createToken(String userId, Map<String, ?> attributes) {
        String error = "Failed to create login token. ";
        NodeUtil tokenParent = getTokenParent(userId);
        if (tokenParent != null) {
            try {
                long creationTime = new Date().getTime();
                NodeUtil tokenNode = createTokenNode(tokenParent, creationTime);
                tokenNode.setString(JcrConstants.JCR_UUID, IdentifierManager.generateUUID());

                String key = generateKey(options.getConfigValue(PARAM_TOKEN_LENGTH, DEFAULT_KEY_SIZE));
                String nodeId = getIdentifier(tokenNode.getTree());
                String token = new StringBuilder(nodeId).append(DELIM).append(key).toString();

                String keyHash = PasswordUtil.buildPasswordHash(getKeyValue(key, userId), options);
                tokenNode.setString(TOKEN_ATTRIBUTE_KEY, keyHash);

                long exp;
                if (attributes.containsKey(PARAM_TOKEN_EXPIRATION)) {
                    exp = Long.parseLong(attributes.get(PARAM_TOKEN_EXPIRATION).toString());
                } else {
                    exp = tokenExpiration;
                }
                long expTime = createExpirationTime(creationTime, exp);
                tokenNode.setDate(TOKEN_ATTRIBUTE_EXPIRY, expTime);

                for (String name : attributes.keySet()) {
                    if (!RESERVED_ATTRIBUTES.contains(name)) {
                        String attr = attributes.get(name).toString();
                        tokenNode.setString(name, attr);
                    }
                }
                root.commit();
                return new TokenInfoImpl(tokenNode, token, userId);
            } catch (NoSuchAlgorithmException e) {
                // error while generating login token
                log.error(error, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                // error while generating login token
                log.error(error, e.getMessage());
            } catch (CommitFailedException e) {
                // conflict while committing changes
                log.warn(error, e.getMessage());
            } catch (AccessDeniedException e) {
                log.warn(error, e.getMessage());
            }
        } else {
            log.warn("Unable to get/create token store for user " + userId);
        }
        return null;
    }
    private NodeUtil getTokenParent(String userId) {
        NodeUtil tokenParent = null;
        String parentPath = null;
        try {
            Authorizable user = userManager.getAuthorizable(userId);
            if (user != null && !user.isGroup()) {
                String userPath = user.getPath();
                NodeUtil userNode = new NodeUtil(root.getTree(userPath));
                tokenParent = userNode.getChild(TOKENS_NODE_NAME);
                if (tokenParent == null) {
                    tokenParent = userNode.addChild(TOKENS_NODE_NAME, TOKENS_NT_NAME);
                    parentPath = userPath + '/' + TOKENS_NODE_NAME;
                    root.commit();
                }
            } else {
                log.debug("Cannot create login token: No corresponding node for User " + userId + '.');
            }
        } catch (RepositoryException e) {
            // error while accessing user.
            log.debug("Error while accessing user " + userId + '.', e);
        } catch (CommitFailedException e) {
            // conflict while creating token store for this user -> refresh and
            // try to get the tree from the updated root.
            log.debug("Conflict while creating token store -> retrying", e.getMessage());
            root.refresh();
            if (parentPath != null) {
                Tree parentTree = root.getTree(parentPath);
                if (parentTree.exists()) {
                    tokenParent = new NodeUtil(parentTree);
                }
            }
        }
        return tokenParent;
    }
