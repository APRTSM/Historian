    public boolean isEmpty() {
        final Map<String, String> map = localMap.get();
        return map == null || map.size() == 0;
    }
    public boolean retainAll(final Collection<?> objects) {
        if (!useStack || objects.isEmpty()) {
            return false;
        }
        final List<String> list = stack.get();
        if (list == null || list.isEmpty()) {
            return false;
        }
        final List<String> copy = new ArrayList<String>(list);
        final boolean result = copy.retainAll(objects);
        stack.set(Collections.unmodifiableList(copy));
        return result;
    }
