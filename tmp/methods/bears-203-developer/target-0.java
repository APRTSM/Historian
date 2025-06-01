    public static Marshaller newMarshaller(JAXBContext context) {
        Marshaller result;
        try {
            result = context.createMarshaller();
            result.setProperty(JAXB_FORMATTED_OUTPUT, preferences.getBoolean("prettyPrintXml", true));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        try {
            result.setProperty(JAXB_IMPL_HEADER, HEADER_LINE);
        } catch (PropertyException e) {
            // intentionally left empty
        }
        return result;
    }
