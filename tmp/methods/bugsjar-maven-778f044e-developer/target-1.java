    public List<ModelProperty> getModelProperties() throws IOException
    {
        if(modelProperties == null)
        {
            Set<String> s = new HashSet<String>();
            //TODO: Should add all collections from ProjectUri
            s.addAll(PomTransformer.URIS);
            s.add(ProjectUri.Build.PluginManagement.Plugins.Plugin.Executions.xUri);
            s.add(ProjectUri.DependencyManagement.Dependencies.Dependency.Exclusions.xUri);
            s.add(ProjectUri.Dependencies.Dependency.Exclusions.xUri);
            s.add(ProjectUri.Build.Plugins.Plugin.Executions.xUri);
            s.add(ProjectUri.Build.Plugins.Plugin.Executions.Execution.Goals.xURI);
            s.add(ProjectUri.Reporting.Plugins.Plugin.ReportSets.xUri);
            s.add(ProjectUri.Reporting.Plugins.Plugin.ReportSets.ReportSet.configuration);
            s.add(ProjectUri.Build.Plugins.Plugin.Executions.Execution.configuration);
            s.add(ProjectUri.Profiles.Profile.Build.Plugins.Plugin.configuration);//TODO: More profile info
            modelProperties = ModelMarshaller.marshallXmlToModelProperties(
                getInputStream(), ProjectUri.baseUri, s );
        }
        return new ArrayList<ModelProperty>(modelProperties);
    }
    private PomClassicDomainModel buildModel( File pom,
                                             List<Model> mixins,
                                             Collection<InterpolatorProperty> interpolatorProperties,
                                             Collection<String> activeProfileIds,
                                             PomArtifactResolver resolver ) 
        throws IOException    
    {
        if ( pom == null )
        {
            throw new IllegalArgumentException( "pom: null" );
        }

        if ( resolver == null )
        {
            throw new IllegalArgumentException( "resolver: null" );
        }

        if ( mixins == null )
        {
            mixins = new ArrayList<Model>();
            mixins.add( getSuperModel() );            
        }
        else
        {
            mixins = new ArrayList<Model>( mixins );
            Collections.reverse( mixins );
        }

        if(activeProfileIds == null)
        {
            activeProfileIds = new ArrayList<String>();
        }

        List<InterpolatorProperty> properties;
        if ( interpolatorProperties == null )
        {
            properties = new ArrayList<InterpolatorProperty>();
        }
        else
        {
            properties = new ArrayList<InterpolatorProperty>( interpolatorProperties );
        }

        PomClassicDomainModel domainModel = new PomClassicDomainModel( pom );
        domainModel.setProjectDirectory( pom.getParentFile() );
        List<DomainModel> domainModels = new ArrayList<DomainModel>();
        domainModels.add( domainModel );

        ProfileContext profileContext = new ProfileContext(new DefaultModelDataSource(domainModel.getModelProperties(),
                PomTransformer.MODEL_CONTAINER_FACTORIES), activeProfileIds, properties);

        Collection<ModelContainer> profileContainers = profileContext.getActiveProfiles();

        for(ModelContainer mc : profileContainers)
        {
            List<ModelProperty> transformed = new ArrayList<ModelProperty>();
            transformed.add(new ModelProperty(ProjectUri.xUri, null));
            for(ModelProperty mp : mc.getProperties())
            {
                if(mp.getUri().startsWith(ProjectUri.Profiles.Profile.xUri) && !mp.getUri().equals(ProjectUri.Profiles.Profile.id)
                        && !mp.getUri().startsWith(ProjectUri.Profiles.Profile.Activation.xUri) )
                {
                    transformed.add(new ModelProperty(mp.getUri().replace(ProjectUri.Profiles.Profile.xUri, ProjectUri.xUri),
                            mp.getResolvedValue()));
                }
            }
            domainModels.add(new PomClassicDomainModel(transformed));
        }

        File parentFile = null;
        int lineageCount = 0;
        if ( domainModel.getModel().getParent() != null )
        {
            List<DomainModel> mavenParents;
            if ( isParentLocal( domainModel.getModel().getParent(), pom.getParentFile() ) )
            {
                mavenParents = getDomainModelParentsFromLocalPath( domainModel, resolver, pom.getParentFile(), properties, activeProfileIds );
            }
            else
            {
                mavenParents = getDomainModelParentsFromRepository( domainModel, resolver, properties, activeProfileIds );
            }
            
            if ( mavenParents.size() > 0 )
            {
                PomClassicDomainModel dm = (PomClassicDomainModel) mavenParents.get( 0 );
                parentFile = dm.getFile();
                domainModel.setParentFile( parentFile );
                lineageCount = mavenParents.size();
            }
            
            domainModels.addAll( mavenParents );
        }

        for ( Model model : mixins )
        {
            domainModels.add( new PomClassicDomainModel( model ) );
        }
        
        PomClassicTransformer transformer = new PomClassicTransformer( new PomClassicDomainModelFactory() );
        
        ModelTransformerContext ctx = new ModelTransformerContext(PomTransformer.MODEL_CONTAINER_INFOS );
        
        PomClassicDomainModel transformedDomainModel = ( (PomClassicDomainModel) ctx.transform( domainModels,
                                                                                                transformer,
                                                                                                transformer,
                                                                                                Collections.EMPTY_LIST,
                                                                                                properties,                                                                 
                                                                                                listeners ) );
        // Lineage count is inclusive to add the POM read in itself.
        transformedDomainModel.setLineageCount( lineageCount + 1 );
        transformedDomainModel.setParentFile( parentFile );
        
        return transformedDomainModel;
    }
