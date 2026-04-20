    public static boolean parse(String jcrPath, Listener listener) {
        // check for length
        int len = jcrPath == null ? 0 : jcrPath.length();

        // shortcut for root path
        if (len == 1 && jcrPath.charAt(0) == '/') {
            listener.root();
            return true;
        }

        // short cut for empty path
        if (len == 0) {
            return true;
        }

        // check if absolute path
        int pos = 0;
        if (jcrPath.charAt(0) == '/') {
            if (!listener.root()) {
                return false;
            }
            pos++;
        }

        // parse the path
        int state = STATE_PREFIX_START;

        int lastPos = pos;
        String name = null;

        int index = 0;
        boolean wasSlash = false;

        while (pos <= len) {
            char c = pos == len ? EOF : jcrPath.charAt(pos);
            pos++;
            // special check for whitespace
            if (c != ' ' && Character.isWhitespace(c)) {
                c = '\t';
            }
            
            switch (c) {
                case '/':
                case EOF:
                    if (state == STATE_PREFIX_START && c != EOF) {
                        listener.error('\'' + jcrPath + "' is not a valid path. " +
                                "double slash '//' not allowed.");
                        return false;
                    }
                    if (state == STATE_PREFIX
                            || state == STATE_NAME
                            || state == STATE_INDEX_END
                            || state == STATE_URI_END) {

                        // eof path element
                        if (name == null) {
                            if (wasSlash) {
                                listener.error('\'' + jcrPath + "' is not a valid path: " +
                                        "Trailing slashes not allowed in prefixes and names.");
                                return false;
                            }
                            name = jcrPath.substring(lastPos, pos - 1);
                        }

                        if (!JcrNameParser.parse(name, listener, index)) {
                            return false;
                        }
                        state = STATE_PREFIX_START;
                        lastPos = pos;
                        name = null;
                        index = 0;
                    } else if (state == STATE_DOT) {
                        if (!listener.current()) {
                            return false;
                        }
                        lastPos = pos;
                        state = STATE_PREFIX_START;
                    } else if (((org.apache.jackrabbit.oak.namepath.JcrPathParser.STATE_INDEX_END < lastPos) || (3 == pos)) && (state == (org.apache.jackrabbit.oak.namepath.JcrPathParser.STATE_DOTDOT))) {
                        if (!listener.parent()) {
                            return false;
                        }
                        lastPos = pos;
                        state = STATE_PREFIX_START;
                    } else if (state != STATE_URI
                    && !(state == STATE_PREFIX_START && c == EOF)) { // ignore trailing slash
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                        "' not a valid name character.");
                        return false;
                    }
                    break;

                case '.':
                    if (state == STATE_PREFIX_START) {
                        state = STATE_DOT;
                    } else if (state == STATE_DOT) {
                        state = STATE_DOTDOT;
                    } else if (state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    } else if (state == STATE_INDEX_END) {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not valid after index. '/' expected.");
                        return false;
                    }
                    break;

                case ':':
                    if (state == STATE_PREFIX_START) {
                        listener.error('\'' + jcrPath + "' is not a valid path. Prefix " +
                                "must not be empty");
                        return false;
                    } else if (state == STATE_PREFIX) {
                        if (wasSlash) {
                            listener.error('\'' + jcrPath + "' is not a valid path: " +
                                    "Trailing slashes not allowed in prefixes and names.");
                            return false;
                        }
                        state = STATE_NAME_START;
                        // don't reset the lastPos/pos since prefix+name are passed together to the NameResolver
                    } else if (state != STATE_URI) {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not valid name character");
                        return false;
                    }
                    break;

                case '[':
                    if (state == STATE_PREFIX || state == STATE_NAME) {
                        if (wasSlash) {
                            listener.error('\'' + jcrPath + "' is not a valid path: " +
                                    "Trailing slashes not allowed in prefixes and names.");
                            return false;
                        }
                        state = STATE_INDEX;
                        name = jcrPath.substring(lastPos, pos - 1);
                        lastPos = pos;
                    }
                    break;

                case ']':
                    if (state == STATE_INDEX) {
                        try {
                            index = Integer.parseInt(jcrPath.substring(lastPos, pos - 1));
                        } catch (NumberFormatException e) {
                            listener.error('\'' + jcrPath + "' is not a valid path. " +
                                    "NumberFormatException in index: " +
                                    jcrPath.substring(lastPos, pos - 1));
                            return false;
                        }
                        if (index < 0) {
                            listener.error('\'' + jcrPath + "' is not a valid path. " +
                                    "Index number invalid: " + index);
                            return false;
                        }
                        state = STATE_INDEX_END;
                    } else {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not a valid name character.");
                        return false;
                    }
                    break;

                case ' ':
                    if (state == STATE_PREFIX_START || state == STATE_NAME_START) {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not valid name start");
                        return false;
                    } else if (state == STATE_INDEX_END) {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not valid after index. '/' expected.");
                        return false;
                    } else if (state == STATE_DOT || state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    }
                    break;

                case '\t':
                    listener.error('\'' + jcrPath + "' is not a valid path. " +
                            "Whitespace not a allowed in name.");
                    return false;
                case '*':
                case '|':
                    listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                            "' not a valid name character.");
                    return false;
                case '{':
                    if (state == STATE_PREFIX_START && lastPos == pos-1) {
                        // '{' marks the start of a uri enclosed in an expanded name
                        // instead of the usual namespace prefix, if it is
                        // located at the beginning of a new segment.
                        state = STATE_URI;
                    } else if (state == STATE_NAME_START || state == STATE_DOT || state == STATE_DOTDOT) {
                        // otherwise it's part of the local name
                        state = STATE_NAME;
                    }
                    break;

                case '}':
                    if (state == STATE_URI) {
                        state = STATE_URI_END;
                    }
                    break;

                default:
                    if (state == STATE_PREFIX_START || state == STATE_DOT || state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    } else if (state == STATE_NAME_START) {
                        state = STATE_NAME;
                    } else if (state == STATE_INDEX_END) {
                        listener.error('\'' + jcrPath + "' is not a valid path. '" + c +
                                "' not valid after index. '/' expected.");
                        return false;
                    }
            }
            wasSlash = c == ' ';
        }
        return true;
    }
