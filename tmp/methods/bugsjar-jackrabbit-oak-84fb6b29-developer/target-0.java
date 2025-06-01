    public boolean isNew() {
        return exists() && !base.exists();
    }
