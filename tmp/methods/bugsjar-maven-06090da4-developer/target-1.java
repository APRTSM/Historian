    protected void updateRepositoryMetadata( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
        throws IOException, XmlPullParserException
    {
        MetadataXpp3Reader mappingReader = new MetadataXpp3Reader();

        Metadata metadata = null;

        File metadataFile = new File( localRepository.getBasedir(),
                                      localRepository.pathOfLocalRepositoryMetadata( this, remoteRepository ) );

        if ( metadataFile.exists() )
        {
            Reader reader = null;

            try
            {
                reader = new FileReader( metadataFile );

                metadata = mappingReader.read( reader, false );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }

        boolean changed;

        // If file could not be found or was not valid, start from scratch
        if ( metadata == null )
        {
            metadata = this.metadata;

            changed = true;
        }
        else
        {
            changed = metadata.merge( this.metadata );
        }

        // beware meta-versions!
        String version = metadata.getVersion();
        if ( version != null && ( Artifact.LATEST_VERSION.equals( version ) || Artifact.RELEASE_VERSION.equals( version ) ) )
        {
            // meta-versions are not valid <version/> values...don't write them.
            metadata.setVersion( null );
        }

        if ( changed || !metadataFile.exists() )
        {
            Writer writer = null;
            try
            {
                metadataFile.getParentFile().mkdirs();
                writer = new FileWriter( metadataFile );

                MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();

                mappingWriter.write( writer, metadata );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
        else
        {
            metadataFile.setLastModified( System.currentTimeMillis() );
        }
    }
    public void resolve( RepositoryMetadata metadata, List remoteRepositories, ArtifactRepository localRepository )
        throws RepositoryMetadataResolutionException
    {
        boolean alreadyResolved = alreadyResolved( metadata );
        if ( !alreadyResolved )
        {
            for ( Iterator i = remoteRepositories.iterator(); i.hasNext(); )
            {
                ArtifactRepository repository = (ArtifactRepository) i.next();

                ArtifactRepositoryPolicy policy =
                    metadata.isSnapshot() ? repository.getSnapshots() : repository.getReleases();

                if ( !policy.isEnabled() )
                {
                    getLogger().debug( "Skipping disabled repository " + repository.getId() );
                }
                else if ( repository.isBlacklisted() )
                {
                    getLogger().debug( "Skipping blacklisted repository " + repository.getId() );
                }
                else
                {
                    File file = new File( localRepository.getBasedir(),
                                          localRepository.pathOfLocalRepositoryMetadata( metadata, repository ) );



                    boolean checkForUpdates =
                        policy.checkOutOfDate( new Date( file.lastModified() ) ) || !file.exists();

                    boolean metadataIsEmpty = true;

                    if ( checkForUpdates )
                    {
                        getLogger().info( metadata.getKey() + ": checking for updates from " + repository.getId() );

                        try
                        {
                            resolveAlways( metadata, repository, file, policy.getChecksumPolicy(), true );
                            metadataIsEmpty = false;
                        }
                        catch ( TransferFailedException e )
                        {
                            // TODO: [jc; 08-Nov-2005] revisit this for 2.1
                            // suppressing logging to avoid logging this error twice.
                            metadataIsEmpty = true;
                        }
                    }

                    // TODO: should this be inside the above check?
                    // touch file so that this is not checked again until interval has passed
                    if ( file.exists() )
                    {
                        file.setLastModified( System.currentTimeMillis() );
                    }
                    else
                    {
                        // this ensures that files are not continuously checked when they don't exist remotely
                        try
                        {
                            metadata.storeInLocalRepository( localRepository, repository );
                        }
                        catch ( RepositoryMetadataStoreException e )
                        {
                            throw new RepositoryMetadataResolutionException(
                                "Unable to store local copy of metadata: " + e.getMessage(), e );
                        }
                    }
                }
            }
            cachedMetadata.add( metadata.getKey() );
        }

        try
        {
            mergeMetadata( metadata, remoteRepositories, localRepository );
        }
        catch ( RepositoryMetadataStoreException e )
        {
            throw new RepositoryMetadataResolutionException(
                "Unable to store local copy of metadata: " + e.getMessage(), e );
        }
        catch ( RepositoryMetadataReadException e )
        {
            throw new RepositoryMetadataResolutionException( "Unable to read local copy of metadata: " + e.getMessage(),
                                                             e );
        }
    }
