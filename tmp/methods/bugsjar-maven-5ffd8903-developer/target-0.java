    public boolean containsVersion( ArtifactVersion version )
    {
        boolean snapshot = isSnapshot( version );

        if ( lowerBound != null )
        {
            int comparison = lowerBound.compareTo( version );

            if ( snapshot && comparison == 0 )
            {
                return true;
            }

            if ( ( comparison == 0 ) && !lowerBoundInclusive )
            {
                return false;
            }
            if ( comparison > 0 )
            {
                return false;
            }
        }
        if ( upperBound != null )
        {
            int comparison = upperBound.compareTo( version );

            if ( snapshot && comparison == 0 )
            {
                return true;
            }

            if ( ( comparison == 0 ) && !upperBoundInclusive )
            {
                return false;
            }
            if ( comparison < 0 )
            {
                return false;
            }
        }

        if ( lowerBound != null || upperBound != null )
        {
            return !snapshot;
        }

        return true;
    }
    private boolean isSnapshot( ArtifactVersion version )
    {
        return Artifact.SNAPSHOT_VERSION.equals( version.getQualifier() );
    }
