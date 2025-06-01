        public Reader getCharacterStream() {
            InputStream is = getByteStream();
            return camelContext.getTypeConverter().convertTo(Reader.class, is);
        }
