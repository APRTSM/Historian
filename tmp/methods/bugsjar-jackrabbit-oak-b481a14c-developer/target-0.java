        synchronized void clear() {

            // calculate the size of the map array
            // assume a fill factor of at most 80%
            long maxLen = (long) (maxMemory / averageMemory / 0.75);
            // the size needs to be a power of 2
            long l = 8;
            while (l < maxLen) {
                l += l;
            }
            // the array size is at most 2^31 elements
            int len = (int) Math.min(1L << 31, l);

            // initialize the stack and queue heads
            stack = new Entry<K, V>();
            stack.stackPrev = stack.stackNext = stack;
            queue = new Entry<K, V>();
            queue.queuePrev = queue.queueNext = queue;
            queue2 = new Entry<K, V>();
            queue2.queuePrev = queue2.queueNext = queue2;

            // first set to a small array, to avoiding out of memory
            @SuppressWarnings("unchecked")
            Entry<K, V>[] small = new Entry[1];
            entries = small;
            @SuppressWarnings("unchecked")
            Entry<K, V>[] e = new Entry[len];
            entries = e;

            mapSize = 0;
            usedMemory = 0;
            stackSize = queueSize = queue2Size = 0;
        }
        synchronized V put(K key, int hash, V value, int memory) {
            if (value == null) {
                throw new NullPointerException("The value may not be null");
            }
            V old;
            Entry<K, V> e = find(key, hash);
            if (e == null) {
                old = null;
            } else {
                old = e.value;
                invalidate(key, hash);
            }
            e = new Entry<K, V>();
            e.key = key;
            e.value = value;
            e.memory = memory;
            Entry<K, V>[] array = entries;
            int mask = array.length - 1;
            int index = hash & mask;
            e.mapNext = array[index];
            array[index] = e;
            usedMemory += memory;
            if (usedMemory > maxMemory && mapSize > 0) {
                // an old entry needs to be removed
                evict(e);
            }
            mapSize++;
            // added entries are always added to the stack
            addToStack(e);
            return old;
        }
        synchronized void refresh(K key, int hash, CacheLoader<K, V> loader) throws ExecutionException {
            if (loader == null) {
                // no loader - no refresh
                return;
            }
            V value;
            V old = get(key, hash);
            long start = System.nanoTime();
            try {
                if (old == null) {
                    value = loader.load(key);
                } else {
                    ListenableFuture<V> future = loader.reload(key, old);
                    value = future.get();
                }
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
        synchronized void invalidate(Object key, int hash) {
            Entry<K, V>[] array = entries;
            int mask = array.length - 1;
            int index = hash & mask;
            Entry<K, V> e = array[index];
            if (e == null) {
                return;
            }
            if (e.key.equals(key)) {
                array[index] = e.mapNext;
            } else {
                Entry<K, V> last;
                do {
                    last = e;
                    e = e.mapNext;
                    if (e == null) {
                        return;
                    }
                } while (!e.key.equals(key));
                last.mapNext = e.mapNext;
            }
            mapSize--;
            usedMemory -= e.memory;
            if (e.stackNext != null) {
                removeFromStack(e);
            }
            if (e.isHot()) {
                // when removing a hot entry, the newest cold entry gets hot,
                // so the number of hot entries does not change
                e = queue.queueNext;
                if (e != queue) {
                    removeFromQueue(e);
                    if (e.stackNext == null) {
                        addToStackBottom(e);
                    }
                }
            } else {
                removeFromQueue(e);
            }
            pruneStack();
        }
        Entry<K, V> find(Object key, int hash) {
            Entry<K, V>[] array = entries;
            int mask = array.length - 1;
            int index = hash & mask;
            Entry<K, V> e = array[index];
            while (e != null && !e.key.equals(key)) {
                e = e.mapNext;
            }
            return e;
        }
