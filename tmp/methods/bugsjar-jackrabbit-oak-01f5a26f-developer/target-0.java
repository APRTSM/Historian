    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ExternalIdentityRef) {
            return string.equals(((ExternalIdentityRef) o).string);
        }
        return false;
    }
    private static void escape(@Nonnull StringBuilder builder, @Nonnull CharSequence str) {
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
    public ExternalIdentityRef(@Nonnull String id, @CheckForNull String providerName) {
        this.id = id;
        this.providerName = (providerName == null || providerName.isEmpty()) ? null : providerName;

        StringBuilder b = new StringBuilder();
        escape(b, id);
        if (this.providerName != null) {
            b.append(';');
            escape(b, this.providerName);
        }
        string =  b.toString();
    }
