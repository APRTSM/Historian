    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // special cases
        if (n == 0) {
            return;
        } else if (n == 1) {
            target.addVertex();
            return;
        }

        // create ring lattice
        List<V> ring = new ArrayList<>(n);
        Map<V, List<E>> adj = new LinkedHashMap<>(n);

        for (int i = 0; i < n; i++) {
            V v = target.addVertex();
            ring.add(v);
            adj.put(v, new ArrayList<>(k));
        }

        for (int i = 0; i < n; i++) {
            V vi = ring.get(i);
            List<E> viAdj = adj.get(i);

            for (int j = 1; j <= k / 2; j++) {
                viAdj.add(target.addEdge(vi, ring.get((i + j) % n)));
            }
        }

        // re-wire edges
        for (int r = 0; r < k / 2; r++) {
            for (int i = 0; i < n; i++) {
                V v = ring.get(i);
                E e = adj.get(i).get(r);

                if (rng.nextDouble() < p) {
                    V other = ring.get(rng.nextInt(n));
                    if (!other.equals(v) && !target.containsEdge(v, other)) {
                        if (!addInsteadOfRewire) {
                            target.removeEdge(e);
                        }
                        target.addEdge(v, other);
                    }
                }
            }
        }

    }
