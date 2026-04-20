    protected Source getSource(Exchange exchange, Object body) {
        // body may already be a source
        if (body instanceof Source) {
            return (Source) body;
        }
        Source source = null;
        if (body != null) {
            if (isAllowStAX()) {
                source = exchange.getContext().getTypeConverter().tryConvertTo(StAXSource.class, exchange, body);
            }
            if (source == null) {
                // then try SAX
                source = exchange.getContext().getTypeConverter().tryConvertTo(SAXSource.class, exchange, body);
            }
            if (source == null) {
                // then try stream
                source = exchange.getContext().getTypeConverter().tryConvertTo(StreamSource.class, exchange, body);
            }
            if (source == null) {
                // and fallback to DOM
                source = exchange.getContext().getTypeConverter().tryConvertTo(DOMSource.class, exchange, body);
            }
            // as the TypeConverterRegistry will look up source the converter differently if the type converter is loaded different
            // now we just put the call of source converter at last
            if (source == null) {
                TypeConverter tc = exchange.getContext().getTypeConverterRegistry().lookup(Source.class, body.getClass());
                if (tc != null) {
                    source = tc.convertTo(Source.class, exchange, body);
                }
            }
        }
        if (source == null) {
            if (isFailOnNullBody()) {
                throw new ExpectedBodyTypeException(exchange, Source.class);
            } else {
                try {
                    source = converter.toDOMSource(converter.createDocument());
                } catch (ParserConfigurationException e) {
                    throw new RuntimeTransformException(e);
                }
            }
        }
        return source;
    }
