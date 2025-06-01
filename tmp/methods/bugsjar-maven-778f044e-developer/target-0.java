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
