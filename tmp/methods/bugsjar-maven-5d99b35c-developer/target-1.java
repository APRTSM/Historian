    public DefaultProfileManager( PlexusContainer container, Properties props )
    {
        this( container, (Settings)null, props );
        
    }
    public DefaultProfileManager( PlexusContainer container, Settings settings, Properties props )
    {
        this.container = container;

        loadSettingsProfiles( settings );

        if ( props != null )
        {
            systemProperties = props;
        }
    }
    public static Set createArtifacts( ArtifactFactory artifactFactory, List dependencies, String inheritedScope,
                                       ArtifactFilter dependencyFilter, MavenProject project )
        throws InvalidDependencyVersionException
    {
        Set projectArtifacts = new LinkedHashSet( dependencies.size() );

        for ( Iterator i = dependencies.iterator(); i.hasNext(); )
        {
            Dependency d = (Dependency) i.next();

            String scope = d.getScope();

            if ( StringUtils.isEmpty( scope ) )
            {
                scope = Artifact.SCOPE_COMPILE;

                d.setScope( scope );
            }

            VersionRange versionRange;
            try
            {
                versionRange = VersionRange.createFromVersionSpec( d.getVersion() );
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new InvalidDependencyVersionException( "Unable to parse version '" + d.getVersion() +
                    "' for dependency '" + d.getManagementKey() + "': " + e.getMessage(), e );
            }
            Artifact artifact = artifactFactory.createDependencyArtifact( d.getGroupId(), d.getArtifactId(),
                                                                          versionRange, d.getType(), d.getClassifier(),
                                                                          scope, inheritedScope, d.isOptional() );

            if ( Artifact.SCOPE_SYSTEM.equals( scope ) )
            {
                artifact.setFile( new File( d.getSystemPath() ) );
            }

            ArtifactFilter artifactFilter = dependencyFilter;

            if ( artifact != null && ( artifactFilter == null || artifactFilter.include( artifact ) ) )
            {
                if ( d.getExclusions() != null && !d.getExclusions().isEmpty() )
                {
                    List exclusions = new ArrayList();
                    for ( Iterator j = d.getExclusions().iterator(); j.hasNext(); )
                    {
                        Exclusion e = (Exclusion) j.next();
                        exclusions.add( e.getGroupId() + ":" + e.getArtifactId() );
                    }

                    ArtifactFilter newFilter = new ExcludesArtifactFilter( exclusions );

                    if ( artifactFilter != null )
                    {
                        AndArtifactFilter filter = new AndArtifactFilter();
                        filter.add( artifactFilter );
                        filter.add( newFilter );
                        artifactFilter = filter;
                    }
                    else
                    {
                        artifactFilter = newFilter;
                    }
                }

                artifact.setDependencyFilter( artifactFilter );

                if ( project != null )
                {
                    artifact = project.replaceWithActiveArtifact( artifact );
                }

                projectArtifacts.add( artifact );
            }
        }

        return projectArtifacts;
    }
