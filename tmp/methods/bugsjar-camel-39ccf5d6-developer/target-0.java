        public Object next() {
            Object o = nextToken;
            try {
                nextToken = getNextToken();
            } catch (XMLStreamException e) {
                nextToken = null;
                throw new RuntimeException(e);
            }
            return o;
        }
