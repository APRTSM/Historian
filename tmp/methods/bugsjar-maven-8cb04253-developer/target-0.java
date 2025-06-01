    private static int getRelationOrder( String value, RangeValue rangeValue, boolean isLeft )
    {
        if ( rangeValue.value.length() <= 0 )
        {
            return isLeft ? 1 : -1;
        }

        value = value.replaceAll( "[^0-9\\.\\-\\_]", "" );

        List<String> valueTokens = new ArrayList<String>( Arrays.asList( value.split( "[\\.\\-\\_]" ) ) );
        List<String> rangeValueTokens = new ArrayList<String>( Arrays.asList( rangeValue.value.split( "\\." ) ) );

        int max = Math.max( valueTokens.size(), rangeValueTokens.size() );
        addZeroTokens( valueTokens, max );
        addZeroTokens( rangeValueTokens, max );

        if ( value.equals( rangeValue.getValue() ) )
        {
            if ( !rangeValue.isClosed() )
            {
                return isLeft ? -1 : 1;
            }
            return 0;
        }

        for ( int i = 0; i < valueTokens.size() && i < rangeValueTokens.size(); i++ )
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
