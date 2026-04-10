return new Iterator<Chromosome>() {
            private Iterator<Chromosome> iter = chromosomes.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Chromosome next() {
                return iter.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };