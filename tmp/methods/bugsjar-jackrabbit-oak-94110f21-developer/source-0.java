    private boolean changePassword(User user, SimpleCredentials credentials) {
        try {
            Object newPasswordObject = credentials.getAttribute(CREDENTIALS_ATTRIBUTE_NEWPASSWORD);
            if (newPasswordObject != null) {
                if (newPasswordObject instanceof String) {
                    user.changePassword((String) newPasswordObject);
                    root.commit();
                    log.debug("User " + userId + ": changed user password");
                    return true;
                } else {
                    log.warn("Aborted password change for user " + userId
                            + ": provided new password is of incompatible type "
                            + newPasswordObject.getClass().getName());
                }
            }
        } catch (PasswordHistoryException e) {
            credentials.setAttribute(e.getClass().getName(), e.getMessage());
            log.error("Failed to change password for user " + userId, e.getMessage());
        } catch (RepositoryException e) {
            log.error("Failed to change password for user " + userId, e.getMessage());
        } catch (CommitFailedException e) {
            root.refresh();
            log.error("Failed to change password for user " + userId, e.getMessage());
        }
        return false;
    }
