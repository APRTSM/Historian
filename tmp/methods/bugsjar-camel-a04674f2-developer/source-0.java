        XMLTokenPairIterator(String startToken, String endToken, String inheritNamespaceToken, InputStream in, String charset) {
            super(startToken, endToken, true, in, charset);

            // remove any ending > as we need to support attributes on the tags, so we need to use a reg exp pattern
            String token = startToken.substring(0, startToken.length() - 1) + SCAN_TOKEN_REGEX;
            this.startTokenPattern = Pattern.compile(token);
            this.scanEndToken = endToken.substring(0, endToken.length() - 1) + SCAN_TOKEN_REGEX;
            this.inheritNamespaceToken = inheritNamespaceToken;
            if (inheritNamespaceToken != null) {
                token = inheritNamespaceToken.substring(0, inheritNamespaceToken.length() - 1) + SCAN_TOKEN_REGEX;
                // the namespaces on the parent tag can be in multi line, so we need to instruct the dot to support multilines
                this.inheritNamespaceTokenPattern = Pattern.compile(token, Pattern.MULTILINE | Pattern.DOTALL);
            }
        }
        String getNext(boolean first) {
            String next = scanner.next();
            if (next == null) {
                return null;
            }

            // initialize inherited namespaces on first
            if (first && inheritNamespaceToken != null) {
                rootTokenNamespaces = getNamespacesFromNamespaceToken(next);
            }

            // make sure next is positioned at start token as we can have leading data
            // or we reached EOL and there is no more start tags
            Matcher matcher = startTokenPattern.matcher(next);
            if (!matcher.find()) {
                return null;
            } else {
                int index = matcher.start();
                next = next.substring(index);
            }

            // build answer accordingly to whether namespaces should be inherited or not
            StringBuilder sb = new StringBuilder();
            if (inheritNamespaceToken != null && rootTokenNamespaces != null) {
                // append root namespaces to local start token
                String tag = ObjectHelper.before(next, ">");
                // grab the text
                String text = ObjectHelper.after(next, ">");
                // build result with inherited namespaces
                next = sb.append(tag).append(rootTokenNamespaces).append(">").append(text).append(endToken).toString();
            } else {
                next = sb.append(next).append(endToken).toString();
            }

            return next;
        }
