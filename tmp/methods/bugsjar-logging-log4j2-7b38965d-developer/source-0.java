    public byte[] getHeader() {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" ");
        sbuf.append("\"http://www.w3.org/TR/html4/loose.dtd\">");
        sbuf.append(Constants.LINE_SEP);
        sbuf.append("<html>").append(Constants.LINE_SEP);
        sbuf.append("<head>").append(Constants.LINE_SEP);
        sbuf.append("<title>").append(title).append("</title>").append(Constants.LINE_SEP);
        sbuf.append("<style type=\"text/css\">").append(Constants.LINE_SEP);
        sbuf.append("<!--").append(Constants.LINE_SEP);
        sbuf.append("body, table {font-family:").append(font).append("; font-size: ");
        sbuf.append(headerSize).append(";}").append(Constants.LINE_SEP);
        sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}").append(Constants.LINE_SEP);
        sbuf.append("-->").append(Constants.LINE_SEP);
        sbuf.append("</style>").append(Constants.LINE_SEP);
        sbuf.append("</head>").append(Constants.LINE_SEP);
        sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">").append(Constants.LINE_SEP);
        sbuf.append("<hr size=\"1\" noshade>").append(Constants.LINE_SEP);
        sbuf.append("Log session start time " + new java.util.Date() + "<br>").append(Constants.LINE_SEP);
        sbuf.append("<br>").append(Constants.LINE_SEP);
        sbuf.append(
            "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
        sbuf.append(Constants.LINE_SEP);
        sbuf.append("<tr>").append(Constants.LINE_SEP);
        sbuf.append("<th>Time</th>").append(Constants.LINE_SEP);
        sbuf.append("<th>Thread</th>").append(Constants.LINE_SEP);
        sbuf.append("<th>Level</th>").append(Constants.LINE_SEP);
        sbuf.append("<th>Logger</th>").append(Constants.LINE_SEP);
        if (locationInfo) {
            sbuf.append("<th>File:Line</th>").append(Constants.LINE_SEP);
        }
        sbuf.append("<th>Message</th>").append(Constants.LINE_SEP);
        sbuf.append("</tr>").append(Constants.LINE_SEP);
        return sbuf.toString().getBytes(getCharset());
    }
    public static HTMLLayout createLayout(@PluginAttr("locationInfo") final String locationInfo,
                                          @PluginAttr("title") String title,
                                          @PluginAttr("contentType") String contentType,
                                          @PluginAttr("charset") final String charsetName,
                                          @PluginAttr("fontSize") String fontSize,
                                          @PluginAttr("fontName") String font) {
        final Charset charset = Charsets.getSupportedCharset(charsetName);
        if (font == null) {
            font = "arial,sans-serif";
        }
        final FontSize fs = FontSize.getFontSize(fontSize);
        fontSize = fs.getFontSize();
        final String headerSize = fs.larger().getFontSize();
        final boolean info = locationInfo == null ? false : Boolean.valueOf(locationInfo);
        if (title == null) {
            title = DEFAULT_TITLE;
        }
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return new HTMLLayout(info, title, contentType, charset, font, fontSize, headerSize);
    }
