    public boolean equals(final Object o) {
        if (o!=null) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ObjectMessage that = (ObjectMessage) o;
        return obj == null ? that.obj == null : obj.equals(that.obj);
    }
