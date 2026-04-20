    public Artifact createDependencyArtifact( Dependency d )
    {
        VersionRange versionRange;
        try
        {
            versionRange = VersionRange.createFromVersionSpec( d.getVersion() );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            return null;
        }

        Artifact artifact =
            XcreateDependencyArtifact( d.getGroupId(), d.getArtifactId(), versionRange, d.getType(),
                                                      d.getClassifier(), d.getScope(), d.isOptional() );

        if ( Artifact.SCOPE_SYSTEM.equals( d.getScope() ) && d.getSystemPath() != null )
        {
            artifact.setFile( new File( d.getSystemPath() ) );
        }

        if ( !d.getExclusions().isEmpty() )
        {
            List<String> exclusions = new ArrayList<String>();

            for ( Exclusion exclusion : d.getExclusions() )
            {
                exclusions.add( exclusion.getGroupId() + ':' + exclusion.getArtifactId() );
            }

            artifact.setDependencyFilter( new ExcludesArtifactFilter( exclusions ) );
        }

        return artifact;
    }
    private void initProject( MavenProject project, Map<String, MavenProject> projects, ModelBuildingResult result,
                              Map<File, Boolean> profilesXmls, ProjectBuildingRequest projectBuildingRequest )
    {
        Model model = result.getEffectiveModel();

        project.setModel( model );
        project.setOriginalModel( result.getRawModel() );
        project.setFile( model.getPomFile() );
        Parent p = model.getParent();
        if ( p != null )
        {
            project.setParentArtifact( repositorySystem.createProjectArtifact( p.getGroupId(), p.getArtifactId(),
                                                                               p.getVersion() ) );
            // org.apache.maven.its.mng4834:parent:0.1
            String parentModelId = result.getModelIds().get( 1 );
            File parentPomFile = result.getRawModel( parentModelId ).getPomFile();
            MavenProject parent = projects.get( parentModelId );
            if ( parent == null )
            {
                //
                // At this point the DefaultModelBuildingListener has fired and it populates the
                // remote repositories with those found in the pom.xml, along with the existing externally
                // defined repositories.
                //
                projectBuildingRequest.setRemoteRepositories( project.getRemoteArtifactRepositories() );
                if ( parentPomFile != null )
                {
                    project.setParentFile( parentPomFile );
                    try
                    {
                        parent = build( parentPomFile, projectBuildingRequest ).getProject();
                    }
                    catch ( ProjectBuildingException e )
                    {
                        // MNG-4488 where let invalid parents slide on by
                        logger.warn( "Failed to build parent project for " + project.getId() );
                    }
                }
                else
                {
                    Artifact parentArtifact = project.getParentArtifact();
                    try
                    {
                        parent = build( parentArtifact, projectBuildingRequest ).getProject();
                    }
                    catch ( ProjectBuildingException e )
                    {
                        // MNG-4488 where let invalid parents slide on by
                        logger.warn( "Failed to build parent project for " + project.getId() );
                    }
                }
            }
            project.setParent( parent );
        }

        Artifact projectArtifact =
            repositorySystem.createArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion(), null,
                                             project.getPackaging() );
        project.setArtifact( projectArtifact );

        if ( project.getFile() != null )
        {
            Build build = project.getBuild();
            project.addScriptSourceRoot( build.getScriptSourceDirectory() );
            project.addCompileSourceRoot( build.getSourceDirectory() );
            project.addTestCompileSourceRoot( build.getTestSourceDirectory() );
        }

        List<Profile> activeProfiles = new ArrayList<Profile>();
        activeProfiles.addAll( result.getActivePomProfiles( result.getModelIds().get( 0 ) ) );
        activeProfiles.addAll( result.getActiveExternalProfiles() );
        project.setActiveProfiles( activeProfiles );

        project.setInjectedProfileIds( "external", getProfileIds( result.getActiveExternalProfiles() ) );
        for ( String modelId : result.getModelIds() )
        {
            project.setInjectedProfileIds( modelId, getProfileIds( result.getActivePomProfiles( modelId ) ) );
        }

        String modelId = findProfilesXml( result, profilesXmls );
        if ( modelId != null )
        {
            ModelProblem problem =
                new DefaultModelProblem( "Detected profiles.xml alongside " + modelId
                    + ", this file is no longer supported and was ignored" + ", please use the settings.xml instead",
                                         ModelProblem.Severity.WARNING, ModelProblem.Version.V30, model, -1, -1, null );
            result.getProblems().add( problem );
        }

        //
        // All the parts that were taken out of MavenProject for Maven 4.0.0
        //

        project.setProjectBuildingRequest( projectBuildingRequest );

        // pluginArtifacts
        Set<Artifact> pluginArtifacts = new HashSet<Artifact>();
        for ( Plugin plugin : project.getBuildPlugins() )
        {
            Artifact artifact = repositorySystem.createPluginArtifact( plugin );

            if ( artifact != null )
            {
                pluginArtifacts.add( artifact );
            }
        }
        project.setPluginArtifacts( pluginArtifacts );

        // reportArtifacts
        Set<Artifact> reportArtifacts = new HashSet<Artifact>();
        for ( ReportPlugin report : project.getReportPlugins() )
        {
            Plugin pp = new Plugin();
            pp.setGroupId( report.getGroupId() );
            pp.setArtifactId( report.getArtifactId() );
            pp.setVersion( report.getVersion() );

            Artifact artifact = repositorySystem.createPluginArtifact( pp );

            if ( artifact != null )
            {
                reportArtifacts.add( artifact );
            }
        }
        project.setReportArtifacts( reportArtifacts );

        // extensionArtifacts
        Set<Artifact> extensionArtifacts = new HashSet<Artifact>();
        List<Extension> extensions = project.getBuildExtensions();
        if ( extensions != null )
        {
            for ( Extension ext : extensions )
            {
                String version;
                if ( StringUtils.isEmpty( ext.getVersion() ) )
                {
                    version = "RELEASE";
                }
                else
                {
                    version = ext.getVersion();
                }

                Artifact artifact =
                    repositorySystem.createArtifact( ext.getGroupId(), ext.getArtifactId(), version, null, "jar" );

                if ( artifact != null )
                {
                    extensionArtifacts.add( artifact );
                }
            }
        }
        project.setExtensionArtifacts( extensionArtifacts );

        // managedVersionMap
        Map<String, Artifact> map = null;
        if ( repositorySystem != null )
        {
            List<Dependency> deps;
            DependencyManagement dependencyManagement = project.getDependencyManagement();
            if ( ( dependencyManagement != null ) && ( ( deps = dependencyManagement.getDependencies() ) != null )
                && ( deps.size() > 0 ) )
            {
                map = new HashMap<String, Artifact>();
                for ( Dependency d : dependencyManagement.getDependencies() )
                {
                    Artifact artifact = repositorySystem.createDependencyArtifact( d );

                    if ( artifact == null )
                    {
                        map = Collections.emptyMap();
                    }

                    map.put( d.getManagementKey(), artifact );
                }
            }
            else
            {
                map = Collections.emptyMap();
            }
        }
        project.setManagedVersionMap( map );

        // release artifact repository
        if ( project.getDistributionManagement() != null && project.getDistributionManagement().getRepository() != null )
        {
            try
            {
                DeploymentRepository r = project.getDistributionManagement().getRepository();
                if ( !StringUtils.isEmpty( r.getId() ) && !StringUtils.isEmpty( r.getUrl() ) )
                {
                    ArtifactRepository repo =
                        repositorySystem.buildArtifactRepository( project.getDistributionManagement().getRepository() );
                    repositorySystem.injectProxy( projectBuildingRequest.getRepositorySession(), Arrays.asList( repo ) );
                    repositorySystem.injectAuthentication( projectBuildingRequest.getRepositorySession(), Arrays.asList( repo ) );
                    project.setReleaseArtifactRepository( repo );
                }
            }
            catch ( InvalidRepositoryException e )
            {
                throw new IllegalStateException( "Failed to create release distribution repository for "
                    + project.getId(), e );
            }
        }

        // snapshot artifact repository
        if ( project.getDistributionManagement() != null
            && project.getDistributionManagement().getSnapshotRepository() != null )
        {
            try
            {
                DeploymentRepository r = project.getDistributionManagement().getSnapshotRepository();
                if ( !StringUtils.isEmpty( r.getId() ) && !StringUtils.isEmpty( r.getUrl() ) )
                {
                    ArtifactRepository repo =
                        repositorySystem.buildArtifactRepository( project.getDistributionManagement().getSnapshotRepository() );
                    repositorySystem.injectProxy( projectBuildingRequest.getRepositorySession(), Arrays.asList( repo ) );
                    repositorySystem.injectAuthentication( projectBuildingRequest.getRepositorySession(), Arrays.asList( repo ) );
                    project.setSnapshotArtifactRepository( repo );
                }
            }
            catch ( InvalidRepositoryException e )
            {
                throw new IllegalStateException( "Failed to create snapshot distribution repository for "
                    + project.getId(), e );
            }
        }
    }
