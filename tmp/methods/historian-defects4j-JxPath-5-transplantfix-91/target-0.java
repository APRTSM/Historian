    public boolean equals(Object object) {
        if (parent == null || parent.isContainer()) {
return true;
}

return object == this || object instanceof DOMNodePointer && node == ((DOMNodePointer) object).node;
    }
