        XMLTokenPairIterator(String startToken, String endToken, String inheritNamespaceToken, InputStream in, String charset) {
            super(startToken, endToken, true, in, charset);

            // remove any beginning < and ending > as we need to support ns prefixes and attributes, so we use a reg exp patterns
            StringBuilder tokenSb = new StringBuilder("<").append(SCAN_TOKEN_NS_PREFIX_REGEX).
                                append(startToken.substring(1, startToken.length() - 1)).append(SCAN_TOKEN_REGEX);
            this.startTokenPattern = Pattern.compile(tokenSb.toString());

            tokenSb = new StringBuilder("</").append(SCAN_TOKEN_NS_PREFIX_REGEX).
                                append(endToken.substring(2, endToken.length() - 1)).append(SCAN_TOKEN_REGEX);
            this.scanEndToken = tokenSb.toString();

            this.inheritNamespaceToken = inheritNamespaceToken;
            if (inheritNamespaceToken != null) {
                // the inherit namespace token may itself have a namespace prefix
                tokenSb = new StringBuilder("<").append(SCAN_TOKEN_NS_PREFIX_REGEX).
                                append(inheritNamespaceToken.substring(1, inheritNamespaceToken.length() - 1)).append(SCAN_TOKEN_REGEX);
                // the namespaces on the parent tag can be in multi line, so we need to instruct the dot to support multilines
                this.inheritNamespaceTokenPattern = Pattern.compile(tokenSb.toString(), Pattern.MULTILINE | Pattern.DOTALL);
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

            // make sure the end tag matches the begin tag if the tag has a namespace prefix
            String tag = ObjectHelper.before(next, ">");
            StringBuilder endTagSb = new StringBuilder("</");
            int firstSpaceIndex = tag.indexOf(" ");
            if (firstSpaceIndex > 0) {
                endTagSb.append(tag.substring(1, firstSpaceIndex)).append(">");
            } else {
                endTagSb.append(tag.substring(1, tag.length())).append(">");
            }

            // build answer accordingly to whether namespaces should be inherited or not
            StringBuilder sb = new StringBuilder();
            if (inheritNamespaceToken != null && rootTokenNamespaces != null) {
                // append root namespaces to local start token
                // grab the text
                String text = ObjectHelper.after(next, ">");
                // build result with inherited namespaces
                next = sb.append(tag).append(rootTokenNamespaces).append(">").append(text).append(endTagSb.toString()).toString();
            } else {
                next = sb.append(next).append(endTagSb.toString()).toString();
            }

            return next;
        }
