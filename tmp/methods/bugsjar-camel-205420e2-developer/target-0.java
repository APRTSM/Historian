    public static void copyResults(Exchange result, Exchange source) {

        // --------------------------------------------------------------------
        //  TODO: merge logic with that of copyResultsPreservePattern()
        // --------------------------------------------------------------------

        if (result != source) {
            result.setException(source.getException());
            if (source.hasOut()) {
                result.getOut().copyFrom(source.getOut());
            } else if (result.getPattern() == ExchangePattern.InOptionalOut) {
                // special case where the result is InOptionalOut and with no OUT response
                // so we should return null to indicate this fact
                result.setOut(null);
            } else {
                // no results so lets copy the last input
                // as the final processor on a pipeline might not
                // have created any OUT; such as a mock:endpoint
                // so lets assume the last IN is the OUT
                if (result.getPattern().isOutCapable()) {
                    // only set OUT if its OUT capable or already has OUT
                    result.getOut().copyFrom(source.getIn());
                } else {
                    // if not replace IN instead to keep the MEP
                    result.getIn().copyFrom(source.getIn());
                    // clear any existing OUT as the result is on the IN
                    if (result.hasOut()) {
                        result.setOut(null);
                    }
                }
            }

            if (source.hasProperties()) {
                result.getProperties().putAll(source.getProperties());
            }
        }
    }
