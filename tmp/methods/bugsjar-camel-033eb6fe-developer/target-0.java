    public void loadContent(Exchange exchange, GenericFile<?> file) throws IOException {
        if (content == null) {
            try {
                content = exchange.getContext().getTypeConverter().mandatoryConvertTo(byte[].class, exchange, file.getFile());
            } catch (NoTypeConversionAvailableException e) {
                throw new IOException("Cannot load file content: " + file.getAbsoluteFilePath(), e);
            }
        }
    }
