    public String toSerializable(final LogEvent event) {
        final StringBuilder buf = new StringBuilder(DEFAULT_SIZE);

        buf.append(this.indent1);
        buf.append('<');
        if (!complete) {
            buf.append(this.namespacePrefix);
        }
        buf.append("Event logger=\"");
        String name = event.getLoggerName();
        if (name.isEmpty()) {
            name = "root";
        }
        buf.append(Transform.escapeHtmlTags(name));
        buf.append("\" timestamp=\"");
        buf.append(event.getMillis());
        buf.append("\" level=\"");
        buf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
        buf.append("\" thread=\"");
        buf.append(Transform.escapeHtmlTags(event.getThreadName()));
        buf.append("\">");
        buf.append(this.eol);

        final Message msg = event.getMessage();
        if (msg != null) {
            boolean xmlSupported = false;
            if (msg instanceof MultiformatMessage) {
                final String[] formats = ((MultiformatMessage) msg).getFormats();
                for (final String format : formats) {
                    if (format.equalsIgnoreCase("XML")) {
                        xmlSupported = true;
                        break;
                    }
                }
            }
            buf.append(this.indent2);
            buf.append('<');
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Message>");
            if (xmlSupported) {
                buf.append(((MultiformatMessage) msg).getFormattedMessage(FORMATS));
            } else {
                buf.append("<![CDATA[");
                // Append the rendered message. Also make sure to escape any
                // existing CDATA sections.
                Transform.appendEscapingCDATA(buf, event.getMessage().getFormattedMessage());
                buf.append("]]>");
            }
            buf.append("</");
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Message>");
            buf.append(this.eol);
        }

        if (event.getContextStack().getDepth() > 0) {
            buf.append(this.indent2);
            buf.append('<');
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("NDC><![CDATA[");
            Transform.appendEscapingCDATA(buf, event.getContextStack().toString());
            buf.append("]]></");
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("NDC>");
            buf.append(this.eol);
        }

        final Throwable throwable = event.getThrown();
        if (throwable != null) {
            final List<String> s = Throwables.toStringList(throwable);
            buf.append(this.indent2);
            buf.append('<');
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Throwable><![CDATA[");
            for (final String str : s) {
                Transform.appendEscapingCDATA(buf, str);
                buf.append(this.eol);
            }
            buf.append("]]></");
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Throwable>");
            buf.append(this.eol);
        }

        if (locationInfo) {
            final StackTraceElement element = event.getSource();
            buf.append(this.indent2);
            buf.append('<');
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("LocationInfo class=\"");
            buf.append(Transform.escapeHtmlTags(element.getClassName()));
            buf.append("\" method=\"");
            buf.append(Transform.escapeHtmlTags(element.getMethodName()));
            buf.append("\" file=\"");
            buf.append(Transform.escapeHtmlTags(element.getFileName()));
            buf.append("\" line=\"");
            buf.append(element.getLineNumber());
            buf.append("\"/>");
            buf.append(this.eol);
        }

        if (properties && event.getContextMap().size() > 0) {
            buf.append(this.indent2);
            buf.append('<');
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Properties>");
            buf.append(this.eol);
            for (final Map.Entry<String, String> entry : event.getContextMap().entrySet()) {
                buf.append(this.indent3);
                buf.append('<');
                if (!complete) {
                    buf.append(this.namespacePrefix);
                }
                buf.append("Data name=\"");
                buf.append(Transform.escapeHtmlTags(entry.getKey()));
                buf.append("\" value=\"");
                buf.append(Transform.escapeHtmlTags(String.valueOf(entry.getValue())));
                buf.append("\"/>");
                buf.append(this.eol);
            }
            buf.append(this.indent2);
            buf.append("</");
            if (!complete) {
                buf.append(this.namespacePrefix);
            }
            buf.append("Properties>");
            buf.append(this.eol);
        }

        buf.append(this.indent1);
        buf.append("</");
        if (!complete) {
            buf.append(this.namespacePrefix);
        }
        buf.append("Event>");
        buf.append(this.eol);

        return buf.toString();
    }
