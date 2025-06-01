    protected static String findEnclosingAttribute(Node n, String attrName) {
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String attr = e.getAttribute(attrName);
                if (attr != null && !attr.equals("")) {
                    return attr;
                }
            }
            n = n.getParentNode();
        }
        return null;
    }
    private String stringValue(Node node) {
        int nodeType = node.getNodeType();
        if (nodeType == Node.COMMENT_NODE) {
            return "";
        }
        boolean trim = !"preserve".equals(findEnclosingAttribute(node, "xml:space"));
        if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            String text = node.getNodeValue();
            return text == null ? "" : trim ? text.trim() : text;
        }
        if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
            String text = ((ProcessingInstruction) node).getData();
            return text == null ? "" : trim ? text.trim() : text;
        }
        NodeList list = node.getChildNodes();
        StringBuffer buf = new StringBuffer(16);
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            buf.append(stringValue(child));
        }
        return buf.toString();
    }
    public Object getValue() {
        if (node.getNodeType() == Node.COMMENT_NODE) {
            String text = ((Comment) node).getData();
            return text == null ? "" : text.trim();
        }
        return stringValue(node);
    }
    protected String getLanguage() {
        return findEnclosingAttribute(node, "xml:lang");
    }
    public Object getValue() {
        if (node instanceof Element) {
            StringBuffer buf = new StringBuffer();
            for (NodeIterator children = childIterator(null, false, null); children.setPosition(children.getPosition() + 1);) {
                NodePointer ptr = children.getNodePointer();
                if (ptr.getImmediateNode() instanceof Element || ptr.getImmediateNode() instanceof Text) {
                    buf.append(ptr.getValue());
                }
            }
            return buf.toString();
        }
        if (node instanceof Comment) {
            String text = ((Comment) node).getText();
            if (text != null) {
                text = text.trim();
            }
            return text;
        }
        String result = null;
        if (node instanceof Text) {
            result = ((Text) node).getText();
        }
        if (node instanceof ProcessingInstruction) {
            result = ((ProcessingInstruction) node).getData();
        }
        boolean trim = !"preserve".equals(findEnclosingAttribute(node, "space", Namespace.XML_NAMESPACE));
        return result != null && trim ? result.trim() : result;
    }
    protected String getLanguage() {
        return findEnclosingAttribute(node, "lang", Namespace.XML_NAMESPACE);
    }
    protected static String findEnclosingAttribute(Object n, String attrName, Namespace ns) {
        while (n != null) {
            if (n instanceof Element) {
                Element e = (Element) n;
                String attr = e.getAttributeValue(attrName, ns);
                if (attr != null && !attr.equals("")) {
                    return attr;
                }
            }
            n = nodeParent(n);
        }
        return null;
    }
