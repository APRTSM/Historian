    private static int getRelationOrder( String value, RangeValue rangeValue, boolean isLeft )
    {
        if ( rangeValue.value.length() <= 0 )
        {
            return isLeft ? 1 : -1;
        }

        value = value.replaceAll( "[^0-9\\.\\-\\_]", "" );

        List<String> valueTokens = new ArrayList<String>( Arrays.asList( value.split( "[\\.\\-\\_]" ) ) );
        List<String> rangeValueTokens = new ArrayList<String>( Arrays.asList( rangeValue.value.split( "\\." ) ) );

        addZeroTokens( valueTokens, 3 );
        addZeroTokens( rangeValueTokens, 3 );

        for ( int i = 0; i < 3; i++ )
        {
            int x = Integer.parseInt( valueTokens.get( i ) );
            int y = Integer.parseInt( rangeValueTokens.get( i ) );
            if ( x < y )
            {
                return -1;
            }
            else if ( x > y )
            {
                return 1;
            }
        }
        if ( !rangeValue.isClosed() )
        {
            return isLeft ? -1 : 1;
        }
        return 0;
    }
    private static void addZeroTokens( List<String> tokens, int max )
    {
        while ( tokens.size() < max )
        {
            tokens.add( "0" );
        }
    }
