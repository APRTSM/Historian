    private Iterable<ProcessorExchangePair> createProcessorExchangePairsIterable(final Exchange exchange, final Object value) {
        final Iterator iterator = ObjectHelper.createIterator(value);
        return new Iterable() {

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
                        Exchange newExchange = exchange.copy();
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
    public static Exchange createCorrelatedCopy(Exchange exchange, boolean handover) {
        Exchange copy = exchange.copy();
        // do not share the unit of work
        copy.setUnitOfWork(null);
        // hand over on completion to the copy if we got any
        UnitOfWork uow = exchange.getUnitOfWork();
        if (handover && uow != null) {
            uow.handoverSynchronization(copy);
        }
        // set a correlation id so we can track back the original exchange
        copy.setProperty(Exchange.CORRELATION_ID, exchange.getExchangeId());
        return copy;
    }
