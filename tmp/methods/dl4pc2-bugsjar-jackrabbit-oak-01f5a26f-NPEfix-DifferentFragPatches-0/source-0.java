    public boolean equals(Object o) {
        try {
            // assuming that we never compare other types of classes
            return this == o || string.equals(((ExternalIdentityRef) o).string);
        } catch (Exception e) {
            return false;
        }
    }
