    public void add(TimeSeriesDataItem item) {
        add(item, true);
    }
    private double minIgnoreNaN(double a, double b) {
        if (Double.isNaN(a)) {
            return b;
        }
        else {
            if (Double.isNaN(b)) {
                return a;
            }
            else {
                return Math.min(a, b);
            }
        }
    }
