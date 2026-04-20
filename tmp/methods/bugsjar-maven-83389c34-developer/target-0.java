    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        validateStringNotEmpty( "modelVersion", problems, Severity.ERROR, model.getModelVersion() );

        validateId( "groupId", problems, model.getGroupId() );

        validateId( "artifactId", problems, model.getArtifactId() );

        validateStringNotEmpty( "packaging", problems, Severity.ERROR, model.getPackaging() );

        if ( !model.getModules().isEmpty() )
        {
            if ( !"pom".equals( model.getPackaging() ) )
            {
                addViolation( problems, Severity.ERROR, "packaging", null, "with value '" + model.getPackaging()
                    + "' is invalid. Aggregator projects " + "require 'pom' as packaging." );
            }

            for ( int i = 0, n = model.getModules().size(); i < n; i++ )
            {
                String module = model.getModules().get( i );
                if ( StringUtils.isBlank( module ) )
                {
                    addViolation( problems, Severity.WARNING, "modules.module[" + i + "]", null,
                                  "has been specified without a path to the project directory." );
                }
            }
        }

        validateStringNotEmpty( "version", problems, Severity.ERROR, model.getVersion() );

        Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

        validateEffectiveDependencies( problems, model.getDependencies(), false, request );

        DependencyManagement mgmt = model.getDependencyManagement();
        if ( mgmt != null )
        {
            validateEffectiveDependencies( problems, mgmt.getDependencies(), true, request );
        }

        if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
        {
            Set<String> modules = new HashSet<String>();
            for ( int i = 0, n = model.getModules().size(); i < n; i++ )
            {
                String module = model.getModules().get( i );
                if ( !modules.add( module ) )
                {
                    addViolation( problems, Severity.ERROR, "modules.module[" + i + "]", null,
                                  "specifies duplicate child module " + module );
                }
            }

            Severity errOn31 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1 );

            Build build = model.getBuild();
            if ( build != null )
            {
                for ( Plugin p : build.getPlugins() )
                {
                    validateStringNotEmpty( "build.plugins.plugin.artifactId", problems, Severity.ERROR,
                                            p.getArtifactId() );

                    validateStringNotEmpty( "build.plugins.plugin.groupId", problems, Severity.ERROR, p.getGroupId() );

                    validatePluginVersion( "build.plugins.plugin.version", problems, p.getVersion(), p.getKey(),
                                           request );

                    validateBoolean( "build.plugins.plugin.inherited", problems, errOn30, p.getInherited(),
                                     p.getKey() );

                    validateBoolean( "build.plugins.plugin.extensions", problems, errOn30, p.getExtensions(),
                                     p.getKey() );

                    validateEffectivePluginDependencies( problems, p, request );
                }

                validateResources( problems, build.getResources(), "build.resources.resource", request );

                validateResources( problems, build.getTestResources(), "build.testResources.testResource", request );
            }

            Reporting reporting = model.getReporting();
            if ( reporting != null )
            {
                for ( ReportPlugin p : reporting.getPlugins() )
                {
                    validateStringNotEmpty( "reporting.plugins.plugin.artifactId", problems, Severity.ERROR,
                                            p.getArtifactId() );

                    validateStringNotEmpty( "reporting.plugins.plugin.groupId", problems, Severity.ERROR,
                                            p.getGroupId() );

                    validateStringNotEmpty( "reporting.plugins.plugin.version", problems, errOn31, p.getVersion(),
                                            p.getKey() );
                }
            }

            for ( Repository repository : model.getRepositories() )
            {
                validateRepository( problems, repository, "repositories.repository", request );
            }

            for ( Repository repository : model.getPluginRepositories() )
            {
                validateRepository( problems, repository, "pluginRepositories.pluginRepository", request );
            }

            DistributionManagement distMgmt = model.getDistributionManagement();
            if ( distMgmt != null )
            {
                if ( distMgmt.getStatus() != null )
                {
                    addViolation( problems, Severity.ERROR, "distributionManagement.status", null,
                                  "must not be specified." );
                }

                validateRepository( problems, distMgmt.getRepository(), "distributionManagement.repository", request );
                validateRepository( problems, distMgmt.getSnapshotRepository(),
                                    "distributionManagement.snapshotRepository", request );
            }
        }
    }
    private void validateEffectivePluginDependencies( ModelProblemCollector problems, Plugin plugin,
                                                      ModelBuildingRequest request )
    {
        List<Dependency> dependencies = plugin.getDependencies();

        if ( !dependencies.isEmpty() )
        {
            String prefix = "build.plugins.plugin[" + plugin.getKey() + "].dependencies.dependency.";

            Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

            for ( Dependency d : dependencies )
            {
                validateEffectiveDependency( problems, d, false, prefix, request );

                validateVersion( prefix + "version", problems, errOn30, d.getVersion(), d.getManagementKey() );

                validateEnum( prefix + "scope", problems, errOn30, d.getScope(), d.getManagementKey(), "compile",
                              "runtime", "system" );
            }
        }
    }
    private void validateEffectiveDependency( ModelProblemCollector problems, Dependency d, boolean management,
                                              String prefix, ModelBuildingRequest request )
    {
        validateId( prefix + "artifactId", problems, d.getArtifactId(), d.getManagementKey() );

        validateId( prefix + "groupId", problems, d.getGroupId(), d.getManagementKey() );

        if ( !management )
        {
            validateStringNotEmpty( prefix + "type", problems, Severity.ERROR, d.getType(), d.getManagementKey() );

            validateStringNotEmpty( prefix + "version", problems, Severity.ERROR, d.getVersion(), d.getManagementKey() );
        }

        if ( "system".equals( d.getScope() ) )
        {
            String systemPath = d.getSystemPath();

            if ( StringUtils.isEmpty( systemPath ) )
            {
                addViolation( problems, Severity.ERROR, prefix + "systemPath", d.getManagementKey(), "is missing." );
            }
            else
            {
                File sysFile = new File( systemPath );
                if ( !sysFile.isAbsolute() )
                {
                    addViolation( problems, Severity.ERROR, prefix + "systemPath", d.getManagementKey(),
                                  "must specify an absolute path but is " + systemPath );
                }
                else if ( !sysFile.isFile() )
                {
                    String msg = "refers to a non-existing file " + sysFile.getAbsolutePath();
                    systemPath = systemPath.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
                    String jdkHome =
                        request.getSystemProperties().getProperty( "java.home", "" ) + File.separator + "..";
                    if ( systemPath.startsWith( jdkHome ) )
                    {
                        msg += ". Please verify that you run Maven using a JDK and not just a JRE.";
                    }
                    addViolation( problems, Severity.WARNING, prefix + "systemPath", d.getManagementKey(), msg );
                }
            }
        }
        else if ( StringUtils.isNotEmpty( d.getSystemPath() ) )
        {
            addViolation( problems, Severity.ERROR, prefix + "systemPath", d.getManagementKey(), "must be omitted."
                + " This field may only be specified for a dependency with system scope." );
        }
    }
    private void validateEffectiveDependencies( ModelProblemCollector problems, List<Dependency> dependencies,
                                                boolean management, ModelBuildingRequest request )
    {
        Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

        String prefix = management ? "dependencyManagement.dependencies.dependency." : "dependencies.dependency.";

        for ( Dependency d : dependencies )
        {
            validateEffectiveDependency( problems, d, management, prefix, request );

            if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
            {
                validateBoolean( prefix + "optional", problems, errOn30, d.getOptional(), d.getManagementKey() );

                if ( !management )
                {
                    validateVersion( prefix + "version", problems, errOn30, d.getVersion(), d.getManagementKey() );

                    /*
                     * TODO: Extensions like Flex Mojos use custom scopes like "merged", "internal", "external", etc. In
                     * order to don't break backward-compat with those, only warn but don't error out.
                     */
                    validateEnum( prefix + "scope", problems, Severity.WARNING, d.getScope(), d.getManagementKey(),
                                  "provided", "compile", "runtime", "test", "system" );
                }
            }
        }
    }
