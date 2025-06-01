    protected void validateURI(String uri, String path, Map<String, Object> parameters) {
        // check for uri containing & but no ? marker
        if (uri.contains("&") && !uri.contains("?")) {
            throw new ResolveEndpointFailedException(uri, "Invalid uri syntax: no ? marker however the uri "
                + "has & parameter separators. Check the uri if its missing a ? marker.");
        }

        // check for uri containing double && markers without include by RAW
        if (uri.contains("&&")) {
            Pattern pattern = Pattern.compile("RAW(.*&&.*)");
            Matcher m = pattern.matcher(uri);
            // we should skip the RAW part
            if (!m.find()) {
                throw new ResolveEndpointFailedException(uri, "Invalid uri syntax: Double && marker found. "
                    + "Check the uri and remove the duplicate & marker.");
            }
        }

        // if we have a trailing & then that is invalid as well
        if (uri.endsWith("&")) {
            throw new ResolveEndpointFailedException(uri, "Invalid uri syntax: Trailing & marker found. "
                + "Check the uri and remove the trailing & marker.");
        }
    }
