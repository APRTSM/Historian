    public static Marshaller newMarshaller(JAXBContext context) {
        try {
            Marshaller result = context.createMarshaller();
            result.setProperty(JAXB_FORMATTED_OUTPUT, preferences.getBoolean("prettyPrintXml", true));
            result.setProperty(JAXB_IMPL_HEADER, HEADER_LINE);
            return result;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
