    public String format(Exchange exchange) {
        Message in = exchange.getIn();

        StringBuilder sb = new StringBuilder();
        if (showAll || showExchangeId) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", Id:").append(exchange.getExchangeId());
        }
        if (showAll || showExchangePattern) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", ExchangePattern:").append(exchange.getPattern());
        }

        if (showAll || showProperties) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", Properties:").append(exchange.getProperties());
        }
        if (showAll || showHeaders) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", Headers:").append(in.getHeaders());
        }
        if (showAll || showBodyType) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", BodyType:").append(getBodyTypeAsString(in));
        }
        if (showAll || showBody) {
            if (multiline) {
                sb.append(LS);
            }
            sb.append(", Body:").append(getBodyAsString(in));
        }

        if (showAll || showException || showCaughtException) {

            // try exception on exchange first
            Exception exception = exchange.getException();
            boolean caught = false;
            if ((showAll || showCaughtException) && exception == null) {
                // fallback to caught exception
                exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                caught = true;
            }

            if (exception != null) {
                if (multiline) {
                    sb.append(LS);
                }
                if (caught) {
                    sb.append(", CaughtExceptionType:").append(exception.getClass().getCanonicalName());
                    sb.append(", CaughtExceptionMessage:").append(exception.getMessage());
                } else {
                    sb.append(", ExceptionType:").append(exception.getClass().getCanonicalName());
                    sb.append(", ExceptionMessage:").append(exception.getMessage());
                }
                if (showAll || showStackTrace) {
                    StringWriter sw = new StringWriter();
                    exception.printStackTrace(new PrintWriter(sw));
                    sb.append(", StackTrace:").append(sw.toString());
                }
            }
        }

        if (showAll || showOut) {
            if (exchange.hasOut()) {
                Message out = exchange.getOut();
                if (showAll || showHeaders) {
                    if (multiline) {
                        sb.append(LS);
                    }
                    sb.append(", OutHeaders:").append(out.getHeaders());
                }
                if (showAll || showBodyType) {
                    if (multiline) {
                        sb.append(LS);
                    }
                    sb.append(", OutBodyType:").append(getBodyTypeAsString(out));
                }
                if (showAll || showBody) {
                    if (multiline) {
                        sb.append(LS);
                    }
                    sb.append(", OutBody:").append(getBodyAsString(out));
                }
            } else {
                if (multiline) {
                    sb.append(LS);
                }
                sb.append(", Out: null");
            }
        }

        if (maxChars > 0) {
            StringBuilder answer = new StringBuilder();
            for (String s : sb.toString().split(LS)) {
                if (s != null) {
                    if (s.length() > maxChars) {
                        s = s.substring(0, maxChars);
                        answer.append(s).append("...");
                    } else {
                        answer.append(s);
                    }
                    if (multiline) {
                        answer.append(LS);
                    }
                }
            }

            // get rid of the leading space comma if needed
            return "Exchange[" + (multiline ? answer.append(']').toString() : answer.toString().substring(2) + "]");
        }

        // get rid of the leading space comma if needed
        return "Exchange[" + (multiline ? sb.append(']').toString() : sb.toString().substring(2) + "]");
    }
