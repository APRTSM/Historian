    public TokenInfo createToken(String userId, Map<String, ?> attributes) {
        String error = "Failed to create login token. ";
        NodeUtil tokenParent = getTokenParent(userId);
        if (tokenParent != null) {
            try {
                long creationTime = new Date().getTime();
                Calendar creation = GregorianCalendar.getInstance();
                creation.setTimeInMillis(creationTime);
                String tokenName = Text.replace(ISO8601.format(creation), ":", ".");

                NodeUtil tokenNode = tokenParent.addChild(tokenName, TOKEN_NT_NAME);
                tokenNode.setString(JcrConstants.JCR_UUID, IdentifierManager.generateUUID());

                String key = generateKey(options.getConfigValue(PARAM_TOKEN_LENGTH, DEFAULT_KEY_SIZE));
                String nodeId = getIdentifier(tokenNode.getTree());
                String token = new StringBuilder(nodeId).append(DELIM).append(key).toString();

                String keyHash = PasswordUtil.buildPasswordHash(getKeyValue(key, userId));
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
