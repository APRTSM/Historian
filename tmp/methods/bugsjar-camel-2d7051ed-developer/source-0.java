        XMLTokenIterator(String tagToken, String inheritNamespaceToken, InputStream in, String charset) {
            this.tagToken = tagToken;
            this.in = in;
            this.charset = charset;

            // remove any beginning < and ending > as we need to support ns prefixes and attributes, so we use a reg exp patterns
            this.tagTokenPattern = 
                Pattern.compile(MessageFormat.format(SCAN_BLOCK_TOKEN_REGEX_TEMPLATE, 
                                                     SCAN_TOKEN_NS_PREFIX_REGEX + tagToken.substring(1, tagToken.length() - 1)), 
                                                     Pattern.MULTILINE | Pattern.DOTALL);
            
            this.inheritNamespaceToken = inheritNamespaceToken;
            if (inheritNamespaceToken != null) {
                // the inherit namespace token may itself have a namespace prefix
                // the namespaces on the parent tag can be in multi line, so we need to instruct the dot to support multilines
                this.inheritNamespaceTokenPattern = 
                    Pattern.compile(MessageFormat.format(SCAN_PARENT_TOKEN_REGEX_TEMPLATE,
                                                         SCAN_TOKEN_NS_PREFIX_REGEX + inheritNamespaceToken.substring(1, inheritNamespaceToken.length() - 1)), 
                                                         Pattern.MULTILINE | Pattern.DOTALL);
            }
        }
