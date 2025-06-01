    protected double calculateNumericalVariance() {
        double n = numeratorDegreesOfFreedom;
		final double denominatorDF = getDenominatorDegreesOfFreedom();

        if (denominatorDF > 4) {
            final double numeratorDF = getNumeratorDegreesOfFreedom();
            final double denomDFMinusTwo = denominatorDF - 2;

            return ( 2 * (denominatorDF * denominatorDF) * (numeratorDF + denominatorDF - 2) ) /
                   ( (numeratorDF * (denomDFMinusTwo * denomDFMinusTwo) * (denominatorDF - 4)) );
        }

        return Double.NaN;
    }
    public String getMessage(final Locale locale) {
        final List<Object> list = new ArrayList<Object>();
		return buildMessage(locale, ": ");
    }
