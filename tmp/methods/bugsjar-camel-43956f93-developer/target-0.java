        private Property(int beginIndex, int endIndex, String key, String value) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.key = key;
            this.value = value;
        }
        public String parse(String input) {
            return doParse(input, new HashSet<String>());
        }
    public String parseUri(String text, Properties properties, String prefixToken, String suffixToken, String propertyPrefix, String propertySuffix,
                           boolean fallbackToUnaugmentedProperty) throws IllegalArgumentException {
        ParsingContext context = new ParsingContext(properties, prefixToken, suffixToken, propertyPrefix, propertySuffix, fallbackToUnaugmentedProperty);
        return context.parse(text);
    }
        private int getSuffixIndex(String input) {
            int index = -1;
            do {
                index = input.indexOf(suffixToken, index + 1);
            } while (index != -1 && isQuoted(input, index, suffixToken));
            return index;
        }
        private String getPropertyValue(String key, String input) {
            String augmentedKey = getAugmentedKey(key);
            boolean shouldFallback = fallbackToUnaugmentedProperty && !key.equals(augmentedKey);

            String value = doGetPropertyValue(augmentedKey);
            if (value == null && shouldFallback) {
                log.debug("Property with key [{}] not found, attempting with unaugmented key: {}", augmentedKey, key);
                value = doGetPropertyValue(key);
            }

            if (value == null) {
                StringBuilder esb = new StringBuilder();
                esb.append("Property with key [").append(augmentedKey).append("] ");
                if (shouldFallback) {
                    esb.append("(and original key [").append(key).append("]) ");
                }
                esb.append("not found in properties from text: ").append(input);
                throw new IllegalArgumentException(esb.toString());
            }

            return value;
        }
        private String doGetPropertyValue(String key) {
            String value = System.getProperty(key);
            if (value != null) {
                log.debug("Found a JVM system property: {} with value: {} to be used.", key, value);
            } else if (properties != null) {
                value = properties.getProperty(key);
            }
            return value;
        }
        private Property readProperty(String input) {
            // Find the index of the first valid suffix token
            int suffix = getSuffixIndex(input);

            // If not found, ensure that there is no valid prefix token in the string
            if (suffix == -1) {
                if (getMatchingPrefixIndex(input, input.length()) != -1) {
                    throw new IllegalArgumentException(format("Missing %s from the text: %s", suffixToken, input));
                }
                return null;
            }

            // Find the index of the prefix token that matches the suffix token
            int prefix = getMatchingPrefixIndex(input, suffix);
            if (prefix == -1) {
                throw new IllegalArgumentException(format("Missing %s from the text: %s", prefixToken, input));
            }

            String key = input.substring(prefix + prefixToken.length(), suffix);
            String value = getPropertyValue(key, input);
            return new Property(prefix, suffix + suffixToken.length(), key, value);
        }
        public ParsingContext(Properties properties, String prefixToken, String suffixToken, String propertyPrefix, String propertySuffix,
                              boolean fallbackToUnaugmentedProperty) {
            this.properties = properties;
            this.prefixToken = prefixToken;
            this.suffixToken = suffixToken;
            this.propertyPrefix = propertyPrefix;
            this.propertySuffix = propertySuffix;
            this.fallbackToUnaugmentedProperty = fallbackToUnaugmentedProperty;
        }
        private int getMatchingPrefixIndex(String input, int suffixIndex) {
            int index = suffixIndex;
            do {
                index = input.lastIndexOf(prefixToken, index - 1);
            } while (index != -1 && isQuoted(input, index, prefixToken));
            return index;
        }
        private String doParse(String input, Set<String> replacedPropertyKeys) {
            String answer = input;
            Property property;
            while ((property = readProperty(answer)) != null) {
                // Check for circular references
                if (replacedPropertyKeys.contains(property.getKey())) {
                    throw new IllegalArgumentException("Circular reference detected with key [" + property.getKey() + "] from text: " + input);
                }

                Set<String> newReplaced = new HashSet<String>(replacedPropertyKeys);
                newReplaced.add(property.getKey());

                String before = answer.substring(0, property.getBeginIndex());
                String after = answer.substring(property.getEndIndex());
                answer = before + doParse(property.getValue(), newReplaced) + after;
            }
            return answer;
        }
        private boolean isQuoted(String input, int index, String token) {
            int beforeIndex = index - 1;
            int afterIndex = index + token.length();
            if (beforeIndex >= 0 && afterIndex < input.length()) {
                char before = input.charAt(beforeIndex);
                char after = input.charAt(afterIndex);
                return (before == after) && (before == '\'' || before == '"');
            }
            return false;
        }
        private String getAugmentedKey(String key) {
            String augmentedKey = key;
            if (propertyPrefix != null) {
                log.debug("Augmenting property key [{}] with prefix: {}", key, propertyPrefix);
                augmentedKey = propertyPrefix + augmentedKey;
            }
            if (propertySuffix != null) {
                log.debug("Augmenting property key [{}] with suffix: {}", key, propertySuffix);
                augmentedKey = augmentedKey + propertySuffix;
            }
            return augmentedKey;
        }
        public int getBeginIndex() {
            return beginIndex;
        }
        public int getEndIndex() {
            return endIndex;
        }
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
