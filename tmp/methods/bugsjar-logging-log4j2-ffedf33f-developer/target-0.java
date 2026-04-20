        protected PrettyPrinter newPrettyPrinter() {
            return new Log4jXmlPrettyPrinter(DEFAULT_INDENT);
        }
        public void writePrologLinefeed(XMLStreamWriter2 sw) throws XMLStreamException {
            // nothing
        }
        public DefaultXmlPrettyPrinter createInstance() {
            return new Log4jXmlPrettyPrinter(XML.DEFAULT_INDENT);
        }
        Log4jXmlPrettyPrinter(int nesting) {
            _nesting = nesting;
        }
