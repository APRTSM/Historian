    public List<Processor> next() {
        // must include wrapped in navigate
        List<Processor> list = super.next();
        list.add(wrapped);
        return list;
    }
