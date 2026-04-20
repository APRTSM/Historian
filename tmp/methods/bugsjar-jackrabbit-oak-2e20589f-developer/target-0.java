        synchronized void refresh(K key, int hash, CacheLoader<K, V> loader) throws ExecutionException {
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
