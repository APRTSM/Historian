        public Object next() {
            Object o = nextToken;
            try {
                nextToken = getNextToken();
            } catch (XMLStreamException e) {
                //
            }
            return o;
        }
