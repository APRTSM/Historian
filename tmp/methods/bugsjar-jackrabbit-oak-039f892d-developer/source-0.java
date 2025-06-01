        private void init() {
            if (result != null) {
                return;
            }
            ArrayList<K> list = new ArrayList<K>();
            while (source.hasNext()) {
                K x = source.next();
                list.add(x);
                checkMemoryLimit(list.size(), settings);
                // from time to time, sort and truncate
                // this should results in O(n*log(2*keep)) operations,
                // which is close to the optimum O(n*log(keep))
                if (list.size() > max * 2) {
                    // remove tail entries right now, to save memory
                    Collections.sort(list, orderBy);
                    keepFirst(list, max);
                }
            }
            Collections.sort(list, orderBy);
            keepFirst(list, max);
            result = list.iterator();
        }
