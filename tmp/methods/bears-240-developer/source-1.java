    CxJoined(final Iterable<Context> contexts) {
        this(contexts, new CxSimple());
    }
    public StDefault(final Suit origin) {
        this(origin, new CxProperties());
    }
    StDefault(final Suit origin, final CxProperties ctx) {
        this(
            origin,
            new CxJoined(
                ctx,
                origin.context()
            )
        );
    }
