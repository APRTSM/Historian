    public PluginDescriptor build( Reader reader, String source )
        throws PlexusConfigurationException
    {
        PlexusConfiguration c = buildConfiguration( reader );

        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        pluginDescriptor.setSource( source );
        pluginDescriptor.setGroupId( c.getChild( "groupId" ).getValue() );
        pluginDescriptor.setArtifactId( c.getChild( "artifactId" ).getValue() );
        pluginDescriptor.setVersion( c.getChild( "version" ).getValue() );
        pluginDescriptor.setGoalPrefix( c.getChild( "goalPrefix" ).getValue() );

        pluginDescriptor.setName( c.getChild( "name" ).getValue() );
        pluginDescriptor.setDescription( c.getChild( "description" ).getValue() );

        String isolatedRealm = c.getChild( "isolatedRealm" ).getValue();

        if ( isolatedRealm != null )
        {
            pluginDescriptor.setIsolatedRealm( Boolean.parseBoolean( isolatedRealm ) );
        }

        String inheritedByDefault = c.getChild( "inheritedByDefault" ).getValue();

        if ( inheritedByDefault != null )
        {
            pluginDescriptor.setInheritedByDefault( Boolean.parseBoolean( inheritedByDefault ) );
        }

        // ----------------------------------------------------------------------
        // Components
        // ----------------------------------------------------------------------

        PlexusConfiguration[] mojoConfigurations = c.getChild( "mojos" ).getChildren( "mojo" );

        for ( PlexusConfiguration component : mojoConfigurations )
        {
            MojoDescriptor mojoDescriptor = buildComponentDescriptor( component, pluginDescriptor );

            pluginDescriptor.addMojo( mojoDescriptor );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        PlexusConfiguration[] dependencyConfigurations = c.getChild( "dependencies" ).getChildren( "dependency" );

        List<ComponentDependency> dependencies = new ArrayList<ComponentDependency>();

        for ( PlexusConfiguration d : dependencyConfigurations )
        {
            ComponentDependency cd = new ComponentDependency();

            cd.setArtifactId( d.getChild( "artifactId" ).getValue() );

            cd.setGroupId( d.getChild( "groupId" ).getValue() );

            cd.setType( d.getChild( "type" ).getValue() );

            cd.setVersion( d.getChild( "version" ).getValue() );

            dependencies.add( cd );
        }

        pluginDescriptor.setDependencies( dependencies );

        return pluginDescriptor;
    }
