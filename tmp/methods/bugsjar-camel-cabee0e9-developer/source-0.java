    public <T> Map<String, T> findByTypeWithName(Class<T> type) {
        Map<String, T> answer = new LinkedHashMap<String, T>();
        try {
            NamingEnumeration<NameClassPair> list = getContext().list("");
            while (list.hasMore()) {
                NameClassPair pair = list.next();
                if (type.isInstance(pair.getClass()) || type.getName().equals(pair.getClassName())) {
                    Object instance = context.lookup(pair.getName());
                    answer.put(pair.getName(), type.cast(instance));
                }
            }
        } catch (NamingException e) {
            // ignore
        }

        return answer;
    }
    public <T> Set<T> findByType(Class<T> type) {
        Set<T> answer = new LinkedHashSet<T>();
        try {
            NamingEnumeration<NameClassPair> list = getContext().list("");
            while (list.hasMore()) {
                NameClassPair pair = list.next();
                if (type.isInstance(pair.getClass()) || type.getName().equals(pair.getClassName())) {
                    Object instance = context.lookup(pair.getName());
                    answer.add(type.cast(instance));
                }
            }
        } catch (NamingException e) {
            // ignore
        }
        return answer;
    }
