    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                   (Double.toString(x))
                   .setScale(scale, roundingMethod))
                   .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        } catch (RuntimeException ex) {
            throw new MathRuntimeException(ex);
        }
    }
    public String getMessage() {
        return getMessage(Locale.US);
    }
    public String getLocalizedMessage() {
        return getMessage(Locale.getDefault());
    }
