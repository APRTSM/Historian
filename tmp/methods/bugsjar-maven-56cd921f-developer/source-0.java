    private static void decomposeParameterIntoUserInstructions( MojoDescriptor mojo, Parameter param,
                                                                StringBuilder messageBuffer )
    {
        String expression = param.getExpression();

        if ( param.isEditable() )
        {
            messageBuffer.append( "Inside the definition for plugin \'" + mojo.getPluginDescriptor().getArtifactId()
                + "\', specify the following:\n\n<configuration>\n  ...\n  <" + param.getName() + ">VALUE</"
                + param.getName() + ">\n</configuration>" );

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
