    public XMLEventReader createXMLEventReader(InputStream in, Exchange exchange) throws XMLStreamException {
        XMLInputFactory factory = getInputFactory();
        try {
            return factory.createXMLEventReader(IOHelper.buffered(in), IOHelper.getCharsetName(exchange));
        } finally {
            returnXMLInputFactory(factory);
        }
    }
    public XMLStreamReader createXMLStreamReader(InputStream in, Exchange exchange) throws XMLStreamException {
        XMLInputFactory factory = getInputFactory();
        try {
            return factory.createXMLStreamReader(IOHelper.buffered(in), IOHelper.getCharsetName(exchange));
        } finally {
            returnXMLInputFactory(factory);
        }
    }
