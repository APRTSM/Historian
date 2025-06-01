    public void validateRawModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        Parent parent = model.getParent();
        if ( parent != null )
        {
            validateStringNotEmpty( "parent.groupId", problems, Severity.FATAL, parent.getGroupId(), parent );

            validateStringNotEmpty( "parent.artifactId", problems, Severity.FATAL, parent.getArtifactId(), parent );

            validateStringNotEmpty( "parent.version", problems, Severity.FATAL, parent.getVersion(), parent );

            if ( equals( parent.getGroupId(), model.getGroupId() )
                && equals( parent.getArtifactId(), model.getArtifactId() ) )
            {
                addViolation( problems, Severity.FATAL, "parent.artifactId", null, "must be changed"
                    + ", the parent element cannot have the same groupId:artifactId as the project.", parent );
            }
        }

        if ( request.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
        {
            Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

            validateEnum( "modelVersion", problems, Severity.ERROR, model.getModelVersion(), null, model, "4.0.0" );
            validateStringNoExpression( "groupId", problems, Severity.WARNING, model.getGroupId(), model );
            validateStringNoExpression( "artifactId", problems, Severity.WARNING, model.getArtifactId(), model );
            validateStringNoExpression( "version", problems, Severity.WARNING, model.getVersion(), model );

            validateRawDependencies( problems, model.getDependencies(), "dependencies.dependency", request );

            if ( model.getDependencyManagement() != null )
            {
                validateRawDependencies( problems, model.getDependencyManagement().getDependencies(),
                                      "dependencyManagement.dependencies.dependency", request );
            }

            validateRepositories( problems, model.getRepositories(), "repositories.repository", request );

            validateRepositories( problems, model.getPluginRepositories(), "pluginRepositories.pluginRepository", request );

            Build build = model.getBuild();
            if ( build != null )
            {
                validateRawPlugins( problems, build.getPlugins(), "build.plugins.plugin", request );

                PluginManagement mngt = build.getPluginManagement();
                if ( mngt != null )
                {
                    validateRawPlugins( problems, mngt.getPlugins(), "build.pluginManagement.plugins.plugin", request );
                }
            }

            Set<String> profileIds = new HashSet<String>();

            for ( Profile profile : model.getProfiles() )
            {
                String prefix = "profiles.profile[" + profile.getId() + "]";

                if ( !profileIds.add( profile.getId() ) )
                {
                    addViolation( problems, errOn30, "profiles.profile.id", null,
                                  "must be unique but found duplicate profile with id " + profile.getId(), profile );
                }

                validateRawDependencies( problems, profile.getDependencies(), prefix + ".dependencies.dependency",
                                         request );

                if ( profile.getDependencyManagement() != null )
                {
                    validateRawDependencies( problems, profile.getDependencyManagement().getDependencies(), prefix
                        + ".dependencyManagement.dependencies.dependency", request );
                }

                validateRepositories( problems, profile.getRepositories(), prefix + ".repositories.repository", request );

                validateRepositories( problems, profile.getPluginRepositories(), prefix
                    + ".pluginRepositories.pluginRepository", request );

                BuildBase buildBase = profile.getBuild();
                if ( buildBase != null )
                {
                    validateRawPlugins( problems, buildBase.getPlugins(), prefix + ".plugins.plugin", request );

                    PluginManagement mngt = buildBase.getPluginManagement();
                    if ( mngt != null )
                    {
                        validateRawPlugins( problems, mngt.getPlugins(), prefix + ".pluginManagement.plugins.plugin",
                                            request );
                    }
                }
            }
        }
    }
    private void validateRawPlugins( ModelProblemCollector problems, List<Plugin> plugins, String prefix,
                                     ModelBuildingRequest request )
    {
        Severity errOn31 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1 );

        Map<String, Plugin> index = new HashMap<String, Plugin>();

        for ( Plugin plugin : plugins )
        {
            String key = plugin.getKey();

            Plugin existing = index.get( key );

            if ( existing != null )
            {
                addViolation( problems, errOn31, prefix + ".(groupId:artifactId)", null,
                              "must be unique but found duplicate declaration of plugin " + key, plugin );
            }
            else
            {
                index.put( key, plugin );
            }

            Set<String> executionIds = new HashSet<String>();

            for ( PluginExecution exec : plugin.getExecutions() )
            {
                if ( !executionIds.add( exec.getId() ) )
                {
                    addViolation( problems, Severity.ERROR, prefix + "[" + plugin.getKey()
                        + "].executions.execution.id", null, "must be unique but found duplicate execution with id "
                        + exec.getId(), exec );
                }
            }
        }
    }
