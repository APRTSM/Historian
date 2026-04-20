    public Wagon getWagon( String protocol )
        throws UnsupportedProtocolException
    {
        if ( protocol == null )
        {
            throw new UnsupportedProtocolException( "Unspecified protocol" );
        }

        String hint = protocol.toLowerCase( java.util.Locale.ENGLISH );
        Wagon wagon = (Wagon) wagons.get( hint );

        if ( wagon == null )
        {
            throw new UnsupportedProtocolException( "Cannot find wagon which supports the requested protocol: " + protocol );
        }

        return wagon;
    }
