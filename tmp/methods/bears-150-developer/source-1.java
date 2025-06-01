    private Object readResolve() {
        String s = this.value;

        if (s == null)
            return null;

        int len = s.length();
        char ch = ' ';

        int i = 0;
        for (;
             i < len && ('a' <= (ch = s.charAt(i)) && ch <= 'z'
                     || 'A' <= ch && ch <= 'Z'
                     || '0' <= ch && ch <= '9');
             i++) {
        }

        String language = s.substring(0, i);
        String country = null;
        String var = null;

        if (ch == '-' || ch == '_') {
            int head = ++i;

            for (;
                 i < len && ('a' <= (ch = s.charAt(i)) && ch <= 'z'
                         || 'A' <= ch && ch <= 'Z'
                         || '0' <= ch && ch <= '9');
                 i++) {
            }

            country = s.substring(head, i);
        }

        if (ch == '-' || ch == '_') {
            int head = ++i;

            for (;
                 i < len && ('a' <= (ch = s.charAt(i)) && ch <= 'z'
                         || 'A' <= ch && ch <= 'Z'
                         || '0' <= ch && ch <= '9');
                 i++) {
            }

            var = s.substring(head, i);
        }

        if (var != null)
            return new Locale(language, country, var);
        else if (country != null)
            return new Locale(language, country);
        else
            return new Locale(language);
    }
    public LocaleHandle(String locale) {
        this.value = locale;
    }
    public void writeObject(Object obj, AbstractHessianOutput out)
            throws IOException {
        if (obj == null)
            out.writeNull();
        else {
            Locale locale = (Locale) obj;

            out.writeObject(new LocaleHandle(locale.toString()));
        }
    }
