    public boolean equals(Object object) {
        return object == this || object instanceof DOMNodePointer && node == ((DOMNodePointer) object).node;
    }
