    private void assembleModelInheritance( Model child, Model parent, String childPathAdjustment, boolean appendPaths )
    {
        // cannot inherit from null parent.
        if ( parent == null )
        {
            return;
        }

        // Group id
        if ( child.getGroupId() == null )
        {
            child.setGroupId( parent.getGroupId() );
        }

        // version
        if ( child.getVersion() == null )
        {
            // The parent version may have resolved to something different, so we take what we asked for...
            // instead of - child.setVersion( parent.getVersion() );

            if ( child.getParent() != null )
            {
                child.setVersion( child.getParent().getVersion() );
            }
        }

        // inceptionYear
        if ( child.getInceptionYear() == null )
        {
            child.setInceptionYear( parent.getInceptionYear() );
        }

        // url
        if ( child.getUrl() == null )
        {
            if ( parent.getUrl() != null )
            {
                child.setUrl( appendPath( parent.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
            }
            else
            {
                child.setUrl( parent.getUrl() );
            }
        }

        assembleDistributionInheritence( child, parent, childPathAdjustment, appendPaths );

        // issueManagement
        if ( child.getIssueManagement() == null )
        {
            child.setIssueManagement( parent.getIssueManagement() );
        }

        // description
        if ( child.getDescription() == null )
        {
            child.setDescription( parent.getDescription() );
        }

        // Organization
        if ( child.getOrganization() == null )
        {
            child.setOrganization( parent.getOrganization() );
        }

        // Scm
        assembleScmInheritance( child, parent, childPathAdjustment, appendPaths );

        // ciManagement
        if ( child.getCiManagement() == null )
        {
            child.setCiManagement( parent.getCiManagement() );
        }

        // developers
        if ( child.getDevelopers().size() == 0 )
        {
            child.setDevelopers( parent.getDevelopers() );
        }

        // licenses
        if ( child.getLicenses().size() == 0 )
        {
            child.setLicenses( parent.getLicenses() );
        }

        // developers
        if ( child.getContributors().size() == 0 )
        {
            child.setContributors( parent.getContributors() );
        }

        // mailingLists
        if ( child.getMailingLists().size() == 0 )
        {
            child.setMailingLists( parent.getMailingLists() );
        }

        // Build
        assembleBuildInheritance( child, parent );

        assembleDependencyInheritance( child, parent );

        child.setRepositories( ModelUtils.mergeRepositoryLists( child.getRepositories(), parent.getRepositories() ) );
        child.setPluginRepositories(
            ModelUtils.mergeRepositoryLists( child.getPluginRepositories(), parent.getPluginRepositories() ) );

        assembleReportingInheritance( child, parent );

        assembleDependencyManagementInheritance( child, parent );

        Properties props = new Properties();
        props.putAll( parent.getProperties() );
        props.putAll( child.getProperties() );

        child.setProperties( props );
    }
    private static DeploymentRepository copyDistributionRepository( DeploymentRepository parentRepository )
    {
        DeploymentRepository repository = new DeploymentRepository();

        repository.setId( parentRepository.getId() );

        repository.setName( parentRepository.getName() );

        repository.setUrl( parentRepository.getUrl() );

        repository.setLayout( parentRepository.getLayout() );

        repository.setUniqueVersion( parentRepository.isUniqueVersion() );

        return repository;
    }
    private void assembleDistributionInheritence( Model child, Model parent, String childPathAdjustment,
                                                  boolean appendPaths )
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
                        site.setUrl(
                            appendPath( site.getUrl(), child.getArtifactId(), childPathAdjustment, appendPaths ) );
                    }
                }
            }

            if ( childDistMgmt.getRepository() == null )
            {
                if ( parentDistMgmt.getRepository() != null )
                {
                    DeploymentRepository repository = copyDistributionRepository( parentDistMgmt.getRepository() );
                    childDistMgmt.setRepository( repository );
                }
            }

            if ( childDistMgmt.getSnapshotRepository() == null )
            {
                if ( parentDistMgmt.getSnapshotRepository() != null )
                {
                    DeploymentRepository repository =
                        copyDistributionRepository( parentDistMgmt.getSnapshotRepository() );
                    childDistMgmt.setSnapshotRepository( repository );
                }
            }

            if ( StringUtils.isEmpty( childDistMgmt.getDownloadUrl() ) )
            {
                childDistMgmt.setDownloadUrl( parentDistMgmt.getDownloadUrl() );
            }

            // NOTE: We SHOULD NOT be inheriting status, since this is an assessment of the POM quality.
            // NOTE: We SHOULD NOT be inheriting relocation, since this relates to a single POM
        }
    }
