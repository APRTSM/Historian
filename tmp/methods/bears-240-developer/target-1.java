    public CxJoined(final Iterable<Context> contexts) {
        this(contexts, new CxSimple());
    }
    public StDefault(final Suit origin) {
        this(
            origin,
            new CxProperties(System.getProperties()),
            new CxProperties()
        );
    }
    StDefault(final Suit origin, final Context... ctxs) {
        this(
            origin,
            new IterableOf<>(ctxs)
        );
    }
    StDefault(final Suit origin, final Iterable<Context> ctxs) {
        this(
            origin,
            new CxJoined(
                new Joined<>(
                    origin.context(),
                    ctxs
                )
            )
        );
    }
