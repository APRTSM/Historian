    protected Collection<V> values() {
        ArrayList<V> list = new ArrayList<V>();
        for (K k : keySet()) {
            V v = peek(k);
            if (v != null) {
                list.add(v);
            }
        }
        return list;
    }
    public synchronized Set<Map.Entry<K, V>> entrySet() {
        HashMap<K, V> map = new HashMap<K, V>();
        for (K k : keySet()) {
            V v = peek(k);
            if (v != null) {
                map.put(k,  v);
            }
        }
        return map.entrySet();
    }
        synchronized V get(K key, int hash, CacheLoader<K, V> loader) throws ExecutionException {
            V value = get(key, hash);
            if (value == null) {
                if (loader == null) {
                    return null;
                }
                long start = System.nanoTime();
                try {
                    value = loader.load(key);
                    loadSuccessCount++;
                } catch (Exception e) {
                    loadExceptionCount++;
                    throw new ExecutionException(e);
                } finally {
                    long time = System.nanoTime() - start;
                    totalLoadTime += time;
                }
                put(key, hash, value, cache.sizeOf(key, value));
            }
            return value;
        }
    public ConcurrentMap<K, V> asMap() {
        return new ConcurrentMap<K, V>() {

            @Override
            public int size() {
                long size = CacheLIRS.this.size();
                return (int) Math.min(size, Integer.MAX_VALUE);
            }

            @Override
            public boolean isEmpty() {
                return CacheLIRS.this.size() == 0;
            }

            @Override
            public boolean containsKey(Object key) {
                return CacheLIRS.this.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return CacheLIRS.this.containsValue(value);
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(Object key) {
                return CacheLIRS.this.peek((K) key);
            }

            @Override
            public V put(K key, V value) {
                return CacheLIRS.this.put(key, value, sizeOf(key, value));
            }

            @Override
            public V remove(Object key) {
                @SuppressWarnings("unchecked")
                V old = CacheLIRS.this.getUnchecked((K) key);
                CacheLIRS.this.invalidate(key);
                return old;
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {
                for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                    put(e.getKey(), e.getValue());
                }                
            }

            @Override
            public void clear() {
                CacheLIRS.this.clear();
            }

            @Override
            public Set<K> keySet() {
                return CacheLIRS.this.keySet();
            }

            @Override
            public Collection<V> values() {
                return CacheLIRS.this.values();
            }

            @Override
            public Set<java.util.Map.Entry<K, V>> entrySet() {
                return CacheLIRS.this.entrySet();
            }

            @Override
            public V putIfAbsent(K key, V value) {
                return CacheLIRS.this.putIfAbsent(key, value);
            }

            @Override
            public boolean remove(Object key, Object value) {
                return CacheLIRS.this.remove(key, value);
            }

            @Override
            public boolean replace(K key, V oldValue, V newValue) {
                return CacheLIRS.this.replace(key, oldValue, newValue);
            }

            @Override
            public V replace(K key, V value) {
                return CacheLIRS.this.replace(key, value);
            }
            
        };
    }
    public V peek(K key) {
        int hash = getHash(key);
        Entry<K, V> e = getSegment(hash).find(key, hash);
        return e == null ? null : e.value;
    }
    boolean containsValue(Object value) {
        for (Segment<K, V> s : segments) {
            for (K k : s.keySet()) {
                V v = peek(k);
                if (v != null && v.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
