    public OutputStream decorateLogger(AbstractBuild build, final OutputStream logger)
            throws IOException, InterruptedException {
        if (logger == null) {
            return null;
        }

        return new LineTransformationOutputStream() {
            AnsiHtmlOutputStream ansi = new AnsiHtmlOutputStream(logger, colorMap, new AnsiAttributeElement.Emitter() {
                @Override
                public void emitHtml(String html) {
                    try {
                        new SimpleHtmlNote(html).encodeTo(logger);
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Failed to add HTML markup '" + html + "'", e);
                    }
                }
            });

            @Override
            protected void eol(byte[] b, int len) throws IOException {
                ansi.write(b, 0, len);
                ansi.flush();
                logger.flush();
            }

            @Override
            public void close() throws IOException {
                ansi.close();
                logger.close();
                super.close();
            }
        };
    }
