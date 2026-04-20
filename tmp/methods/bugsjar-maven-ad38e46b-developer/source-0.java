    private void assembleDistributionInheritence( Model child, Model parent, String childPathAdjustment, boolean appendPaths )
    {
        if ( parent.getDistributionManagement() != null )
        {
            DistributionManagement parentDistMgmt = parent.getDistributionManagement();

            DistributionManagement childDistMgmt = child.getDistributionManagement();

            if ( childDistMgmt == null )
            {
                childDistMgmt = new DistributionManagement();

                child.setDistributionManagement( childDistMgmt );
            }

            if ( childDistMgmt.getSite() == null )
            {
                if ( parentDistMgmt.getSite() != null )
                {
                    Site site = new Site();

                    childDistMgmt.setSite( site );

                    site.setId( parentDistMgmt.getSite().getId() );

                    site.setName( parentDistMgmt.getSite().getName() );

                    site.setUrl( parentDistMgmt.getSite().getUrl() );

                    if ( site.getUrl() != null )
                    {
                        site.setUrl( appendPath( site.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
                    }
                }
            }

            if ( childDistMgmt.getRepository() == null )
            {
                if ( parentDistMgmt.getRepository() != null )
                {
                    DeploymentRepository repository = new DeploymentRepository();

                    childDistMgmt.setRepository( repository );

                    repository.setId( parentDistMgmt.getRepository().getId() );

                    repository.setName( parentDistMgmt.getRepository().getName() );

                    repository.setUrl( parentDistMgmt.getRepository().getUrl() );

                    repository.setUniqueVersion( parentDistMgmt.getRepository().isUniqueVersion() );
                }
            }

            if ( childDistMgmt.getSnapshotRepository() == null )
            {
                if ( parentDistMgmt.getSnapshotRepository() != null )
                {
                    DeploymentRepository repository = new DeploymentRepository();

                    childDistMgmt.setSnapshotRepository( repository );

                    repository.setId( parentDistMgmt.getSnapshotRepository().getId() );

                    repository.setName( parentDistMgmt.getSnapshotRepository().getName() );

                    repository.setUrl( parentDistMgmt.getSnapshotRepository().getUrl() );

                    repository.setUniqueVersion( parentDistMgmt.getSnapshotRepository().isUniqueVersion() );
                }
            }
        }
    }
    private void assembleReportingInheritance( Model child, Model parent )
    {
        // Reports :: aggregate
        Reporting childReporting = child.getReporting();
        Reporting parentReporting = parent.getReporting();

        if ( parentReporting != null )
        {
            if ( childReporting == null )
            {
                childReporting = new Reporting();
                child.setReporting( childReporting );
            }

            if ( StringUtils.isEmpty( childReporting.getOutputDirectory() ) )
            {
                childReporting.setOutputDirectory( parentReporting.getOutputDirectory() );
            }

            ModelUtils.mergeReportPluginLists( childReporting, parentReporting, true );
        }
    }
    protected String appendPath( String parentPath, String childPath, String pathAdjustment, boolean appendPaths )
    {
        List pathFragments = new ArrayList();
        
        String rootPath = parentPath;

        String protocol = null;
        int protocolIdx = rootPath.indexOf( "://" );
        
        if ( protocolIdx > -1 )
        {
            protocol = rootPath.substring( 0, protocolIdx + 3 );
            rootPath = rootPath.substring( protocolIdx + 3 );
        }
        
        pathFragments.add( rootPath );
        
        if ( appendPaths )
        {
            if ( pathAdjustment != null )
            {
                pathFragments.add( pathAdjustment );
            }
            
            pathFragments.add( childPath );
        }
        
        StringBuffer cleanedPath = new StringBuffer();
        
        if ( protocol != null )
        {
            cleanedPath.append( protocol );
        }
        
        if ( rootPath.startsWith( "/" ) )
        {
            cleanedPath.append( '/' );
        }
        
        String lastToken = null;
        String currentToken = null;
        
        for ( Iterator it = pathFragments.iterator(); it.hasNext(); )
        {
            String pathFragment = (String) it.next();
            
            StringTokenizer tokens = new StringTokenizer( pathFragment, "/" );
            
            while( tokens.hasMoreTokens() )
            {
                lastToken = currentToken;
                currentToken = tokens.nextToken();
                
                if ( "..".equals( currentToken ) )
                {
                    // trim the previous path part off...
                    cleanedPath.setLength( cleanedPath.length() - ( lastToken.length() + 1 ) );
                }
                else if ( !".".equals( currentToken ) )
                {
                    // don't worry about /./ self-references.
                    cleanedPath.append( currentToken ).append( '/' );
                }
            }
        }
        
        if ( !childPath.endsWith( "/" ) && appendPaths )
        {
            cleanedPath.setLength( cleanedPath.length() - 1 );
        }
        
        return cleanedPath.toString();
    }
    private void assembleScmInheritance( Model child, Model parent, String childPathAdjustment, boolean appendPaths )
    {
        if ( parent.getScm() != null )
        {
            Scm parentScm = parent.getScm();

            Scm childScm = child.getScm();

            if ( childScm == null )
            {
                childScm = new Scm();

                child.setScm( childScm );
            }

            if ( StringUtils.isEmpty( childScm.getConnection() ) && !StringUtils.isEmpty( parentScm.getConnection() ) )
            {
                childScm.setConnection( appendPath( parentScm.getConnection(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }

            if ( StringUtils.isEmpty( childScm.getDeveloperConnection() ) &&
                !StringUtils.isEmpty( parentScm.getDeveloperConnection() ) )
            {
                childScm
                    .setDeveloperConnection(
                        appendPath( parentScm.getDeveloperConnection(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }

            if ( StringUtils.isEmpty( childScm.getUrl() ) && !StringUtils.isEmpty( parentScm.getUrl() ) )
            {
                childScm.setUrl( appendPath( parentScm.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }
        }
    }
