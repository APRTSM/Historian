    public void alignToBaseDirectory( Model model, File basedir )
    {
        if ( basedir == null )
        {
            return;
        }

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
    public void unalignFromBaseDirectory( Model model, File basedir )
    {
        if ( basedir == null )
        {
            return;
        }

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
    public String alignToBaseDirectory( String path, File basedir )
    {
        if ( basedir == null )
        {
            return path;
        }

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
    public String unalignFromBaseDirectory( String path, File basedir )
    {
        if ( basedir == null )
        {
            return path;
        }

        if ( path == null )
        {
            return null;
        }

        path = path.trim();

        String base = basedir.getAbsolutePath();
        if ( path.startsWith( base ) )
        {
            path = chopLeadingFileSeparator( path.substring( base.length() ) );
        }

        if ( path.length() <= 0 )
        {
            path = ".";
        }

        if ( !new File( path ).isAbsolute() )
        {
            path = path.replace( '\\', '/' );
        }

        return path;
    }
