    public boolean equals(Object o) {
        try {
            // assuming that we never compare other types of classes
            if (o == null) {
                o = string;
            }
            return this == o || string.equals(((ExternalIdentityRef) o).string);
        } catch (Exception e) {
            return false;
        }
    }
