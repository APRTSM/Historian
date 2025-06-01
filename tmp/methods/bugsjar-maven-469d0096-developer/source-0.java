    public String alignToBaseDirectory( String path, File basedir )
    {
        if ( path == null )
        {
            return null;
        }

        String s = stripBasedirToken( path );

        File file = new File( s );
        if ( file.isAbsolute() )
        {
            // path was already absolute, just normalize file separator and we're done
            s = file.getPath();
        }
        else if ( file.getPath().startsWith( File.separator ) )
        {
            // drive-relative Windows path, don't align with project directory but with drive root
            s = file.getAbsolutePath();
        }
        else
        {
            // an ordinary relative path, align with project directory
            s = new File( new File( basedir, s ).toURI().normalize() ).getAbsolutePath();
        }

        return s;
    }
    public void unalignFromBaseDirectory( Model model, File basedir )
    {
        Build build = model.getBuild();

        if ( build != null )
        {
            build.setDirectory( unalignFromBaseDirectory( build.getDirectory(), basedir ) );

            build.setSourceDirectory( unalignFromBaseDirectory( build.getSourceDirectory(), basedir ) );

            build.setTestSourceDirectory( unalignFromBaseDirectory( build.getTestSourceDirectory(), basedir ) );

            for ( Resource resource : build.getResources() )
            {
                resource.setDirectory( unalignFromBaseDirectory( resource.getDirectory(), basedir ) );
            }

            for ( Resource resource : build.getTestResources() )
            {
                resource.setDirectory( unalignFromBaseDirectory( resource.getDirectory(), basedir ) );
            }

            if ( build.getFilters() != null )
            {
                List<String> filters = new ArrayList<String>();
                for ( String filter : build.getFilters() )
                {
                    filters.add( unalignFromBaseDirectory( filter, basedir ) );
                }
                build.setFilters( filters );
            }

            build.setOutputDirectory( unalignFromBaseDirectory( build.getOutputDirectory(), basedir ) );

            build.setTestOutputDirectory( unalignFromBaseDirectory( build.getTestOutputDirectory(), basedir ) );
        }

        Reporting reporting = model.getReporting();

        if ( reporting != null )
        {
            reporting.setOutputDirectory( unalignFromBaseDirectory( reporting.getOutputDirectory(), basedir ) );
        }
    }
    public void alignToBaseDirectory( Model model, File basedir )
    {
        Build build = model.getBuild();

        if ( build != null )
        {
            build.setDirectory( alignToBaseDirectory( build.getDirectory(), basedir ) );

            build.setSourceDirectory( alignToBaseDirectory( build.getSourceDirectory(), basedir ) );

            build.setTestSourceDirectory( alignToBaseDirectory( build.getTestSourceDirectory(), basedir ) );

            for ( Resource resource : build.getResources() )
            {
                resource.setDirectory( alignToBaseDirectory( resource.getDirectory(), basedir ) );
            }

            for ( Resource resource : build.getTestResources() )
            {
                resource.setDirectory( alignToBaseDirectory( resource.getDirectory(), basedir ) );
            }

            if ( build.getFilters() != null )
            {
                List<String> filters = new ArrayList<String>();
                for ( String filter : build.getFilters() )
                {
                    filters.add( alignToBaseDirectory( filter, basedir ) );
                }
                build.setFilters( filters );
            }

            build.setOutputDirectory( alignToBaseDirectory( build.getOutputDirectory(), basedir ) );

            build.setTestOutputDirectory( alignToBaseDirectory( build.getTestOutputDirectory(), basedir ) );
        }

        Reporting reporting = model.getReporting();

        if ( reporting != null )
        {
            reporting.setOutputDirectory( alignToBaseDirectory( reporting.getOutputDirectory(), basedir ) );
        }
    }
    public String unalignFromBaseDirectory( String directory, File basedir )
    {
        String path = basedir.getPath();
        if ( directory.startsWith( path ) )
        {
            directory = directory.substring( path.length() + 1 ).replace( '\\', '/' );
        }
        return directory;
    }
