    private void escape(StringBuilder builder, CharSequence str) {
        final int len = str.length();
        for (int i=0; i<len; i++) {
            char c = str.charAt(i);
            if (c == '%') {
                builder.append("%25");
            } else if (c == ';') {
                builder.append("%3b");
            } else {
                builder.append(c);
            }
        }
    }
    public boolean equals(Object o) {
        try {
            // assuming that we never compare other types of classes
            return this == o || string.equals(((ExternalIdentityRef) o).string);
        } catch (Exception e) {
            return false;
        }
    }
    public ExternalIdentityRef(@Nonnull String id, @CheckForNull String providerName) {
        this.id = id;
        this.providerName = providerName;

        StringBuilder b = new StringBuilder();
        escape(b, id);
        if (providerName != null && providerName.length() > 0) {
            b.append(';');
            escape(b, providerName);
        }
        string =  b.toString();
    }
