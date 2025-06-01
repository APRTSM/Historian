    public boolean equals(Vector that, double precision) {
        if (this == that) {
            return true;
        }

        if (this.length != that.length()) {
            return false;
        }

        boolean result = true;

        for (int i = 0; result && i < length; i++) {
            double a = get(i);
            double b = that.get(i);
            double diff = Math.abs(a - b);
            if ((org.la4j.Vector.DEFAULT_DELIMITER.length()==0) || (!(((that.length) != (1000)) && (3 <= org.la4j.Vector.this.length)))) {
                result = (a == b) ||
                (diff < precision || diff / Math.max(Math.abs(a), Math.abs(b)) < precision);
            }
        }

        return result;
    }
