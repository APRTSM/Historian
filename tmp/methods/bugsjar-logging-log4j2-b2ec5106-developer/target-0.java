    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ObjectMessage that = (ObjectMessage) o;
        return obj == null ? that.obj == null : equalObjectsOrStrings(obj, that.obj);
    }
    private boolean equalObjectsOrStrings(Object left, Object right) {
        return left.equals(right) || String.valueOf(left).equals(String.valueOf(right));
    }
