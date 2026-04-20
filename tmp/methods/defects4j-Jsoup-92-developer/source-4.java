    private void add(String key, String value) {
        checkCapacity(size + 1);
        keys[size] = key;
        vals[size] = value;
        size++;
    }
    Element insert(final Token.StartTag startTag) {
        // cleanup duplicate attributes:

        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
        if (startTag.isSelfClosing()) {
            Element el = insertEmpty(startTag);
            stack.add(el);
            tokeniser.transition(TokeniserState.Data); // handles <script />, otherwise needs breakout steps from script data
            tokeniser.emit(emptyEnd.reset().name(el.tagName()));  // ensure we get out of whatever state we are in. emitted for yielded processing
            return el;
        }

        Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri, settings.normalizeAttributes(startTag.attributes));
        insert(el);
        return el;
    }
        final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName.trim();
                if (pendingAttributeName.length() > 0) {
                    String value;
                    if (hasPendingAttributeValue)
                        value = pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS;
                    else if (hasEmptyAttributeValue)
                        value = "";
                    else
                        value = null;
                    // note that we add, not put. So that the first is kept, and rest are deduped, once in a context where case sensitivity is known (the appropriate tree builder).
                    attributes.put(pendingAttributeName, value);
                }
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }
    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.

        Element el = new Element(tag, baseUri, settings.normalizeAttributes(startTag.attributes));
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }
