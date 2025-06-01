    public ToolchainPrivate[] getToolchainsForType( String type, MavenSession context )
        throws MisconfiguredToolchainException
    {
        DefaultToolchainsBuildingRequest buildRequest = new DefaultToolchainsBuildingRequest();
        
        File globalToolchainsFile = context.getRequest().getGlobalToolchainsFile();
        if ( globalToolchainsFile != null && globalToolchainsFile.isFile() )
        {
            buildRequest.setGlobalToolchainsSource( new FileSource( globalToolchainsFile ) );
        }

        File userToolchainsFile = context.getRequest().getUserToolchainsFile();
        if ( userToolchainsFile != null && userToolchainsFile.isFile() )
        {
            buildRequest.setUserToolchainsSource( new FileSource( userToolchainsFile ) );
        }
        
        ToolchainsBuildingResult buildResult;
        try
        {
            buildResult = toolchainsBuilder.build( buildRequest );
        }
        catch ( ToolchainsBuildingException e )
        {
            throw new MisconfiguredToolchainException( e.getMessage(), e );
        }
        
        PersistedToolchains pers = buildResult.getEffectiveToolchains();

        List<ToolchainPrivate> toRet = new ArrayList<ToolchainPrivate>();

        ToolchainFactory fact = factories.get( type );
        if ( fact == null )
        {
            logger.error( "Missing toolchain factory for type: " + type
                + ". Possibly caused by misconfigured project." );
        }
        else if ( pers != null )
        {
            List<ToolchainModel> lst = pers.getToolchains();
            if ( lst != null )
            {
                for ( ToolchainModel toolchainModel : lst )
                {
                    if ( type.equals( toolchainModel.getType() ) )
                    {
                        toRet.add( fact.createToolchain( toolchainModel ) );
                    }
                }
            }
        }

        for ( ToolchainFactory toolchainFactory : factories.values() )
        {
            ToolchainPrivate tool = toolchainFactory.createDefaultToolchain();
            if ( tool != null )
            {
                toRet.add( tool );
            }
        }

        return toRet.toArray( new ToolchainPrivate[toRet.size()] );
    }
