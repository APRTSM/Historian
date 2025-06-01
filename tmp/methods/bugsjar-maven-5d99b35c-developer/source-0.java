    public DefaultProfileManager( PlexusContainer container, Properties props )
    {
        this( container, (Settings)null );
        if (props != null) {
            systemProperties = props;
        }
        
    }
