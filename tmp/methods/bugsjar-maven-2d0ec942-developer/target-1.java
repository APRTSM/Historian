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
        
        PersistedToolchains effectiveToolchains = buildResult.getEffectiveToolchains();

        List<ToolchainPrivate> toRet = new ArrayList<ToolchainPrivate>();

        ToolchainFactory fact = factories.get( type );
        if ( fact == null )
        {
            logger.error( "Missing toolchain factory for type: " + type
                + ". Possibly caused by misconfigured project." );
        }
        else
        {
            List<ToolchainModel> lst = effectiveToolchains.getToolchains();
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

            // add default toolchain
            ToolchainPrivate tool = fact.createDefaultToolchain();
            if ( tool != null )
            {
                toRet.add( tool );
            }
        }

        return toRet.toArray( new ToolchainPrivate[toRet.size()] );
    }
