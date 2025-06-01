    public static Map<String, Object> parseQuery(String uri) throws URISyntaxException {
        // must check for trailing & as the uri.split("&") will ignore those
        if (uri != null && uri.endsWith("&")) {
            throw new URISyntaxException(uri, "Invalid uri syntax: Trailing & marker found. "
                + "Check the uri and remove the trailing & marker.");
        }

        try {
            // use a linked map so the parameters is in the same order
            Map<String, Object> rc = new LinkedHashMap<String, Object>();
            if (uri != null) {
                String[] parameters = uri.split("&");
                for (String parameter : parameters) {
                    int p = parameter.indexOf("=");
                    if (p >= 0) {
                        String name = URLDecoder.decode(parameter.substring(0, p), CHARSET);
                        String value = URLDecoder.decode(parameter.substring(p + 1), CHARSET);

                        // does the key already exist?
                        if (rc.containsKey(name)) {
                            // yes it does, so make sure we can support multiple values, but using a list
                            // to hold the multiple values
                            Object existing = rc.get(name);
                            List<String> list;
                            if (existing instanceof List) {
                                list = CastUtils.cast((List<?>) existing);
                            } else {
                                // create a new list to hold the multiple values
                                list = new ArrayList<String>();
                                String s = existing != null ? existing.toString() : null;
                                if (s != null) {
                                    list.add(s);
                                }
                            }
                            list.add(value);
                            rc.put(name, list);
                        } else {
                            rc.put(name, value);
                        }
                    } else {
                        rc.put(parameter, null);
                    }
                }
            }
            return rc;
        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }
    public static String createQueryString(Map<Object, Object> options) throws URISyntaxException {
        try {
            if (options.size() > 0) {
                StringBuilder rc = new StringBuilder();
                boolean first = true;
                for (Object o : options.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        rc.append("&");
                    }

                    String key = (String) o;
                    Object value = options.get(key);

                    // the value may be a list since the same key has multiple values
                    if (value instanceof List) {
                        List<String> list = (List<String>) value;
                        for (Iterator<String> it = list.iterator(); it.hasNext();) {
                            String s = it.next();
                            appendQueryStringParameter(key, s, rc);
                            // append & separator if there is more in the list to append
                            if (it.hasNext()) {
                                rc.append("&");
                            }
                        }
                    } else {
                        // use the value as a String
                        String s = value != null ? value.toString() : null;
                        appendQueryStringParameter(key, s, rc);
                    }
                }
                return rc.toString();
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }
    private static void appendQueryStringParameter(String key, String value, StringBuilder rc) throws UnsupportedEncodingException {
        rc.append(URLEncoder.encode(key, CHARSET));
        // only append if value is not null
        if (value != null) {
            rc.append("=");
            rc.append(URLEncoder.encode(value, CHARSET));
        }
    }
