    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        validateStringNotEmpty( "modelVersion", problems, false, model.getModelVersion() );

        validateId( "groupId", problems, model.getGroupId() );

        validateId( "artifactId", problems, model.getArtifactId() );

        validateStringNotEmpty( "packaging", problems, false, model.getPackaging() );

        if ( !model.getModules().isEmpty() && !"pom".equals( model.getPackaging() ) )
        {
            addViolation( problems, false, "Packaging '" + model.getPackaging() + "' is invalid. Aggregator projects "
                + "require 'pom' as packaging." );
        }

        Parent parent = model.getParent();
        if ( parent != null )
        {
            if ( parent.getGroupId().equals( model.getGroupId() )
                && parent.getArtifactId().equals( model.getArtifactId() ) )
            {
                addViolation( problems, false, "The parent element cannot have the same ID as the project." );
            }
        }

        validateStringNotEmpty( "version", problems, false, model.getVersion() );

        boolean warnOnly = request.getValidationLevel() < ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0;

        for ( Dependency d : model.getDependencies() )
        {
            validateId( "dependencies.dependency.artifactId", problems, d.getArtifactId() );

            validateId( "dependencies.dependency.groupId", problems, d.getGroupId() );

            validateStringNotEmpty( "dependencies.dependency.type", problems, false, d.getType(), d.getManagementKey() );

            validateStringNotEmpty( "dependencies.dependency.version", problems, false, d.getVersion(),
                                    d.getManagementKey() );

            if ( "system".equals( d.getScope() ) )
            {
                String systemPath = d.getSystemPath();

                if ( StringUtils.isEmpty( systemPath ) )
                {
                    addViolation( problems, false, "For dependency " + d + ": system-scoped dependency must specify systemPath." );
                }
                else
                {
                    if ( !new File( systemPath ).isAbsolute() )
                    {
                        addViolation( problems, false, "For dependency " + d + ": system-scoped dependency must "
                            + "specify an absolute path systemPath." );
                    }
                }
            }
            else if ( StringUtils.isNotEmpty( d.getSystemPath() ) )
            {
                addViolation( problems, false,
                    "For dependency " + d + ": only dependency with system scope can specify systemPath." );
            }

            if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
            {
                validateVersion( "dependencies.dependency.version", problems, warnOnly, d.getVersion(),
                                 d.getManagementKey() );

                validateBoolean( "dependencies.dependency.optional", problems, warnOnly, d.getOptional(),
                                 d.getManagementKey() );

                /*
                 * TODO: Extensions like Flex Mojos use custom scopes like "merged", "internal", "external", etc. In
                 * order to don't break backward-compat with those, only warn but don't error out.
                 */
                validateEnum( "dependencies.dependency.scope", problems, true, d.getScope(),
                              d.getManagementKey(), "provided", "compile", "runtime", "test", "system" );
            }
        }

        DependencyManagement mgmt = model.getDependencyManagement();
        if ( mgmt != null )
        {
            for ( Dependency d : mgmt.getDependencies() )
            {
                validateSubElementStringNotEmpty( d, "dependencyManagement.dependencies.dependency.artifactId", problems,
                                                  d.getArtifactId() );

                validateSubElementStringNotEmpty( d, "dependencyManagement.dependencies.dependency.groupId", problems,
                                                  d.getGroupId() );

                if ( "system".equals( d.getScope() ) )
                {
                    String systemPath = d.getSystemPath();

                    if ( StringUtils.isEmpty( systemPath ) )
                    {
                        addViolation( problems, false,
                            "For managed dependency " + d + ": system-scoped dependency must specify systemPath." );
                    }
                    else
                    {
                        if ( !new File( systemPath ).isAbsolute() )
                        {
                            addViolation( problems, false, "For managed dependency " + d + ": system-scoped dependency must "
                                + "specify an absolute path systemPath." );
                        }
                    }
                }
                else if ( StringUtils.isNotEmpty( d.getSystemPath() ) )
                {
                    addViolation( problems, false,
                        "For managed dependency " + d + ": only dependency with system scope can specify systemPath." );
                }

                if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
                {
                    validateBoolean( "dependencyManagement.dependencies.dependency.optional", problems, warnOnly,
                                     d.getOptional(), d.getManagementKey() );
                }
            }
        }

        if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
        {
            boolean warnOnMissingPluginVersion =
                request.getValidationLevel() < ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1;

            Build build = model.getBuild();
            if ( build != null )
            {
                for ( Plugin p : build.getPlugins() )
                {
                    validateStringNotEmpty( "build.plugins.plugin.artifactId", problems, false, p.getArtifactId() );

                    validateStringNotEmpty( "build.plugins.plugin.groupId", problems, false, p.getGroupId() );

                    validateStringNotEmpty( "build.plugins.plugin.version", problems, warnOnMissingPluginVersion,
                                            p.getVersion(), p.getKey() );

                    validateBoolean( "build.plugins.plugin.inherited", problems, warnOnly, p.getInherited(),
                                     p.getKey() );

                    validateBoolean( "build.plugins.plugin.extensions", problems, warnOnly, p.getExtensions(),
                                     p.getKey() );

                    for ( Dependency d : p.getDependencies() )
                    {
                        validateEnum( "build.plugins.plugin[" + p.getKey() + "].dependencies.dependency.scope",
                                      problems, warnOnly, d.getScope(), d.getManagementKey(),
                                      "compile", "runtime", "system" );
                    }
                }

                validateResources( problems, build.getResources(), "build.resources.resource", request );

                validateResources( problems, build.getTestResources(), "build.testResources.testResource", request );
            }

            Reporting reporting = model.getReporting();
            if ( reporting != null )
            {
                for ( ReportPlugin p : reporting.getPlugins() )
                {
                    validateStringNotEmpty( "reporting.plugins.plugin.artifactId", problems, false, p.getArtifactId() );

                    validateStringNotEmpty( "reporting.plugins.plugin.groupId", problems, false, p.getGroupId() );

                    validateStringNotEmpty( "reporting.plugins.plugin.version", problems, warnOnMissingPluginVersion,
                                            p.getVersion(), p.getKey() );
                }
            }

            forcePluginExecutionIdCollision( model, problems );

            for ( Repository repository : model.getRepositories() )
            {
                validateRepositoryLayout( problems, repository, "repositories.repository", request );
            }

            for ( Repository repository : model.getPluginRepositories() )
            {
                validateRepositoryLayout( problems, repository, "pluginRepositories.pluginRepository", request );
            }

            DistributionManagement distMgmt = model.getDistributionManagement();
            if ( distMgmt != null )
            {
                validateRepositoryLayout( problems, distMgmt.getRepository(), "distributionManagement.repository",
                                          request );
                validateRepositoryLayout( problems, distMgmt.getSnapshotRepository(),
                                          "distributionManagement.snapshotRepository", request );
            }
        }
    }
    private boolean validateEnum( String fieldName, ModelProblemCollector problems, boolean warning, String string,
                                  String sourceHint, String... validValues )
    {
        if ( string == null || string.length() <= 0 )
        {
            return true;
        }

        List<String> values = Arrays.asList( validValues );

        if ( values.contains( string ) )
        {
            return true;
        }

        if ( sourceHint != null )
        {
            addViolation( problems, warning, "'" + fieldName + "' must be one of " + values + " for " + sourceHint
                + " but is '" + string + "'." );
        }
        else
        {
            addViolation( problems, warning, "'" + fieldName + "' must be one of " + values + " but is '" + string
                + "'." );
        }

        return false;
    }
    private boolean validateBoolean( String fieldName, ModelProblemCollector problems, boolean warning, String string,
                                     String sourceHint )
    {
        if ( string == null || string.length() <= 0 )
        {
            return true;
        }

        if ( "true".equalsIgnoreCase( string ) || "false".equalsIgnoreCase( string ) )
        {
            return true;
        }

        if ( sourceHint != null )
        {
            addViolation( problems, warning, "'" + fieldName + "' must be 'true' or 'false' for " + sourceHint
                + " but is '" + string + "'." );
        }
        else
        {
            addViolation( problems, warning, "'" + fieldName + "' must be 'true' or 'false' but is '" + string + "'." );
        }

        return false;
    }
    private boolean validateVersion( String fieldName, ModelProblemCollector problems, boolean warning, String string,
                                     String sourceHint )
    {
        if ( string == null || string.length() <= 0 )
        {
            return true;
        }

        if ( !hasExpression( string ) )
        {
            return true;
        }

        if ( sourceHint != null )
        {
            addViolation( problems, warning, "'" + fieldName + "' must be a valid version for " + sourceHint
                + " but is '" + string + "'." );
        }
        else
        {
            addViolation( problems, warning, "'" + fieldName + "' must be a valid version but is '" + string + "'." );
        }

        return false;
    }
