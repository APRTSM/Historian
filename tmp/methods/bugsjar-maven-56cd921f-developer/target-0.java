    private static void decomposeParameterIntoUserInstructions( MojoDescriptor mojo, Parameter param,
                                                                StringBuilder messageBuffer )
    {
        String expression = param.getExpression();

        if ( param.isEditable() )
        {
            boolean isArray = param.getType().endsWith( "[]" );
            boolean isCollection = false;
            boolean isMap = false;
            if ( !isArray )
            {
                try
                {
                    //assuming Type is available in current ClassLoader
                    isCollection = Collection.class.isAssignableFrom( Class.forName( param.getType() ) );
                    isMap = Map.class.isAssignableFrom( Class.forName( param.getType() ) );
                }
                catch ( ClassNotFoundException e )
                {
                    // assume it is not assignable from Collection or Map
                }
            }

            messageBuffer.append( "Inside the definition for plugin \'");
            messageBuffer.append( mojo.getPluginDescriptor().getArtifactId() );
            messageBuffer.append( "\', specify the following:\n\n<configuration>\n  ...\n" );
            messageBuffer.append( "  <" ).append( param.getName() ).append( '>' );
            if( isArray || isCollection )
            {
                messageBuffer.append(  '\n' );
                messageBuffer.append( "    <item>" );
            }
            else if ( isMap )
            {
                messageBuffer.append(  '\n' );
                messageBuffer.append( "    <KEY>" );
            }
            messageBuffer.append( "VALUE" );
            if( isArray || isCollection )
            {
                messageBuffer.append( "</item>\n" );
                messageBuffer.append( "  " );
            }
            else if ( isMap )
            {
                messageBuffer.append( "</KEY>\n" );
                messageBuffer.append( "  " );
            }
            messageBuffer.append( "</" ).append( param.getName() ).append( ">\n" );
            messageBuffer.append( "</configuration>" );

            String alias = param.getAlias();
            if ( StringUtils.isNotEmpty( alias ) && !alias.equals( param.getName() ) )
            {
                messageBuffer.append(
                    "\n\n-OR-\n\n<configuration>\n  ...\n  <" + alias + ">VALUE</" + alias + ">\n</configuration>\n" );
            }
        }

        if ( StringUtils.isEmpty( expression ) )
        {
            messageBuffer.append( "." );
        }
        else
        {
            if ( param.isEditable() )
            {
                messageBuffer.append( "\n\n-OR-\n\n" );
            }

            //addParameterUsageInfo( expression, messageBuffer );
        }
    }
