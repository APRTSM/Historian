    private StringBuffer appendQuotedString(String pattern, ParsePosition pos,
            StringBuffer appendTo, boolean escapingOn) {
        int start = pos.getIndex();
        pos.setIndex(pos.getIndex() + 1);
		char[] c = pattern.toCharArray();
        if (escapingOn && c[start] == QUOTE) {
            return appendTo == null ? null : appendTo.append(QUOTE);
        }
        int lastHold = start;
        for (int i = pos.getIndex(); i < pattern.length(); i++) {
            if (escapingOn && pattern.substring(i).startsWith(ESCAPED_QUOTE)) {
                appendTo.append(c, lastHold, pos.getIndex() - lastHold).append(
                        QUOTE);
                pos.setIndex(i + ESCAPED_QUOTE.length());
                lastHold = pos.getIndex();
                continue;
            }
            switch (c[pos.getIndex()]) {
            case QUOTE:
                next(pos);
                return appendTo == null ? null : appendTo.append(c, lastHold,
                        pos.getIndex() - lastHold);
            default:
                next(pos);
            }
        }
        throw new IllegalArgumentException(
                "Unterminated quoted string at position " + start);
    }
    public final void applyPattern(String pattern) {
        if (registry == null) {
            super.applyPattern(pattern);
            toPattern = super.toPattern();
            return;
        }
        ArrayList foundFormats = new ArrayList();
        ArrayList foundDescriptions = new ArrayList();
        StringBuffer stripCustom = new StringBuffer(pattern.length());

        toPattern = insertFormats(super.toPattern(), foundDescriptions);
		ParsePosition pos = new ParsePosition(0);
        char[] c = pattern.toCharArray();
        int fmtCount = 0;
        while (pos.getIndex() < pattern.length()) {
            switch (c[pos.getIndex()]) {
            case QUOTE:
                appendQuotedString(pattern, pos, stripCustom, true);
                {
					while (pos.getIndex() < pattern.length()) {
						switch (c[pos.getIndex()]) {
						case QUOTE:
							appendQuotedString(pattern, pos, stripCustom, true);
							break;
						case START_FE:
							fmtCount++;
							seekNonWs(pattern, pos);
							int start = pos.getIndex();
							int index = readArgumentIndex(pattern, next(pos));
							stripCustom.append(START_FE).append(index);
							seekNonWs(pattern, pos);
							Format format = null;
							String formatDescription = null;
							if (c[pos.getIndex()] == START_FMT) {
								formatDescription = parseFormatDescription(
										pattern, next(pos));
								format = getFormat(formatDescription);
								if (format == null) {
									stripCustom.append(START_FMT).append(
											formatDescription);
								}
							}
							foundFormats.add(format);
							foundDescriptions.add(format == null ? null
									: formatDescription);
							Validate.isTrue(foundFormats.size() == fmtCount);
							Validate.isTrue(foundDescriptions.size() == fmtCount);
							if (c[pos.getIndex()] != END_FE) {
								throw new IllegalArgumentException(
										"Unreadable format element at position "
												+ start);
							}
						default:
							stripCustom.append(c[pos.getIndex()]);
							next(pos);
						}
					}
					break;
				}
            case START_FE:
                fmtCount++;
                seekNonWs(pattern, pos);
                int start = pos.getIndex();
                int index = readArgumentIndex(pattern, next(pos));
                stripCustom.append(START_FE).append(index);
                seekNonWs(pattern, pos);
                Format format = null;
                String formatDescription = null;
                if (c[pos.getIndex()] == START_FMT) {
                    formatDescription = parseFormatDescription(pattern,
                            next(pos));
                    format = getFormat(formatDescription);
                    if (format == null) {
                        stripCustom.append(START_FMT).append(formatDescription);
                    }
                }
                foundFormats.add(format);
                foundDescriptions.add(format == null ? null : formatDescription);
                Validate.isTrue(foundFormats.size() == fmtCount);
                Validate.isTrue(foundDescriptions.size() == fmtCount);
                if (c[pos.getIndex()] != END_FE) {
                    throw new IllegalArgumentException(
                            "Unreadable format element at position " + start);
                }
                // fall through
            default:
                stripCustom.append(c[pos.getIndex()]);
                next(pos);
            }
        }
        super.applyPattern(stripCustom.toString());
        toPattern = insertFormats(super.toPattern(), foundDescriptions);
        if (containsElements(foundFormats)) {
            Format[] origFormats = getFormats();
            // only loop over what we know we have, as MessageFormat on Java 1.3 
            // seems to provide an extra format element:
            int i = 0;
            for (Iterator it = foundFormats.iterator(); it.hasNext(); i++) {
                Format f = (Format) it.next();
                if (f != null) {
                    origFormats[i] = f;
                }
            }
            super.setFormats(origFormats);
        }
    }
