        public void clear() {
            Iterator<Map.Entry<K, V>> it = AbstractPatriciaTrie.this.entrySet().iterator();
            Set<K> currentKeys = keySet();
            while (it.hasNext()) {
                if (currentKeys.contains(it.next().getKey())) {
                    it.remove();
                }
            }
        }
