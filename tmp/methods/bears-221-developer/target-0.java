    private void pregenerateNote(AnsiAttributeElement element) {
        element.emitOpen(html -> pregenerateNote(html));
        element.emitClose(html -> pregenerateNote(html));
    }
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
                        byte[] pregenerated = notes.get(html);
                        if (pregenerated != null) {
                            logger.write(pregenerated);
                        } else {
                            new SimpleHtmlNote(html).encodeTo(logger);
                        }
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
    public AnsiColorConsoleLogFilter(AnsiColorMap colorMap) {
        super();
        this.colorMap = colorMap;
        this.notes = new HashMap<>();
        // some cases of AnsiHtmlOutputStream.setForegroundColor:
        for (AnsiColorMap.Color color : AnsiColorMap.Color.values()) {
            pregenerateNote(new AnsiAttributeElement(AnsiAttributeElement.AnsiAttrType.FG, "span", "style=\"color: " + colorMap.getNormal(color.ordinal()) + ";\""));
        }
        // TODO other cases, and other methods
        LOG.log(Level.FINE, "Notes pregenerated for {0}", notes.keySet());
    }
    private void pregenerateNote(String html) {
        if (!notes.containsKey(html)) {
            JenkinsJVM.checkJenkinsJVM();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                new SimpleHtmlNote(html).encodeTo(baos);
            } catch (IOException x) { // should be impossible
                throw new RuntimeException(x);
            }
            notes.put(html, baos.toByteArray());
        }
    }
    private Object readResolve() { // handle old program.dat
        return notes == null ? new AnsiColorConsoleLogFilter(colorMap) : this;
    }
