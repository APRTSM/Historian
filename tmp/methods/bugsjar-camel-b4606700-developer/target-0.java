    private Iterable<ProcessorExchangePair> createProcessorExchangePairsIterable(final Exchange exchange, final Object value) {
        final Iterator iterator = ObjectHelper.createIterator(value);
        return new Iterable() {
            // create a copy which we use as master to copy during splitting
            // this avoids any side effect reflected upon the incoming exchange
            private final Exchange copy = ExchangeHelper.createCopy(exchange, true);

            public Iterator iterator() {
                return new Iterator() {
                    private int index;
                    private boolean closed;

                    public boolean hasNext() {
                        if (closed) {
                            return false;
                        }

                        boolean answer = iterator.hasNext();
                        if (!answer) {
                            // we are now closed
                            closed = true;
                            // nothing more so we need to close the expression value in case it needs to be
                            if (value instanceof Closeable) {
                                IOHelper.close((Closeable) value, value.getClass().getName(), LOG);
                            } else if (value instanceof Scanner) {
                                // special for Scanner as it does not implement Closeable
                                ((Scanner) value).close();
                            }
                        }
                        return answer;
                    }

                    public Object next() {
                        Object part = iterator.next();
                        // create a copy as the new exchange to be routed in the splitter from the copy
                        Exchange newExchange = ExchangeHelper.createCopy(copy, true);
                        if (part instanceof Message) {
                            newExchange.setIn((Message)part);
                        } else {
                            Message in = newExchange.getIn();
                            in.setBody(part);
                        }
                        return createProcessorExchangePair(index++, getProcessors().iterator().next(), newExchange);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("Remove is not supported by this iterator");
                    }
                };
            }

        };
    }
