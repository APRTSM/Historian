    public static Map<String, Object> parseQuery(String uri, boolean useRaw) throws URISyntaxException {
        // must check for trailing & as the uri.split("&") will ignore those
        if (uri != null && uri.endsWith("&")) {
            throw new URISyntaxException(uri, "Invalid uri syntax: Trailing & marker found. "
                    + "Check the uri and remove the trailing & marker.");
        }

        if (ObjectHelper.isEmpty(uri)) {
            // return an empty map
            return new LinkedHashMap<String, Object>(0);
        }

        // need to parse the uri query parameters manually as we cannot rely on splitting by &,
        // as & can be used in a parameter value as well.

        try {
            // use a linked map so the parameters is in the same order
            Map<String, Object> rc = new LinkedHashMap<String, Object>();

            boolean isKey = true;
            boolean isValue = false;
            boolean isRaw = false;
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();

            // parse the uri parameters char by char
            for (int i = 0; i < uri.length(); i++) {
                // current char
                char ch = uri.charAt(i);
                // look ahead of the next char
                char next;
                if (i <= uri.length() - 2) {
                    next = uri.charAt(i + 1);
                } else {
                    next = '\u0000';
                }

                // are we a raw value
                isRaw = value.toString().startsWith(RAW_TOKEN_START);

                // if we are in raw mode, then we keep adding until we hit the end marker
                if (isRaw) {
                    if (isKey) {
                        key.append(ch);
                    } else if (isValue) {
                        value.append(ch);
                    }

                    // we only end the raw marker if its )& or at the end of the value

                    boolean end = ch == RAW_TOKEN_END.charAt(0) && (next == '&' || next == '\u0000');
                    if (end) {
                        // raw value end, so add that as a parameter, and reset flags
                        addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
                        key.setLength(0);
                        value.setLength(0);
                        isKey = true;
                        isValue = false;
                        isRaw = false;
                        // skip to next as we are in raw mode and have already added the value
                        i++;
                    }
                    continue;
                }

                // if its a key and there is a = sign then the key ends and we are in value mode
                if (isKey && ch == '=') {
                    isKey = false;
                    isValue = true;
                    isRaw = false;
                    continue;
                }

                // the & denote parameter is ended
                if (ch == '&') {
                    // parameter is ended, as we hit & separator
                    addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
                    key.setLength(0);
                    value.setLength(0);
                    isKey = true;
                    isValue = false;
                    isRaw = false;
                    continue;
                }

                // regular char so add it to the key or value
                if (isKey) {
                    key.append(ch);
                } else if (isValue) {
                    value.append(ch);
                }
            }

            // any left over parameters, then add that
            if (key.length() > 0) {
                addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
            }

            return rc;

        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }
