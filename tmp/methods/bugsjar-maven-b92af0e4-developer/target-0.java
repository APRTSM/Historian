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
            changed = false;
        }

        if ( changed )
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
