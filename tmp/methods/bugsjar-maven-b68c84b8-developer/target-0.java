    public static void mergePluginDefinitions( Plugin child, Plugin parent, boolean handleAsInheritance )
    {
        if ( child == null || parent == null )
        {
            // nothing to do.
            return;
        }

        if ( parent.isExtensions() )
        {
            child.setExtensions( true );
        }

        if ( child.getVersion() == null && parent.getVersion() != null )
        {
            child.setVersion( parent.getVersion() );
        }

        Xpp3Dom childConfiguration = (Xpp3Dom) child.getConfiguration();
        Xpp3Dom parentConfiguration = (Xpp3Dom) parent.getConfiguration();

        childConfiguration = Xpp3Dom.mergeXpp3Dom( childConfiguration, parentConfiguration );

        child.setConfiguration( childConfiguration );

        child.setDependencies( mergeDependencyList( child.getDependencies(), parent.getDependencies() ) );

        // from here to the end of the method is dealing with merging of the <executions/> section.
        String parentInherited = parent.getInherited();

        boolean parentIsInherited = parentInherited == null || Boolean.valueOf( parentInherited ).booleanValue();

        List parentExecutions = parent.getExecutions();

        if ( parentExecutions != null && !parentExecutions.isEmpty() )
        {
            List mergedExecutions = new ArrayList();
            
            Map assembledExecutions = new TreeMap();

            Map childExecutions = child.getExecutionsAsMap();

            for ( Iterator it = parentExecutions.iterator(); it.hasNext(); )
            {
                PluginExecution parentExecution = (PluginExecution) it.next();

                if ( !handleAsInheritance || parentIsInherited )
                {
                    PluginExecution assembled = parentExecution;

                    PluginExecution childExecution = (PluginExecution) childExecutions.get( parentExecution.getId() );

                    if ( childExecution != null )
                    {
                        mergePluginExecutionDefinitions( childExecution, parentExecution );

                        assembled = childExecution;
                    }
                    else if ( handleAsInheritance && parentInherited == null )
                    {
                        parentExecution.unsetInheritanceApplied();
                    }

                    assembledExecutions.put( assembled.getId(), assembled );
                    mergedExecutions.add(assembled);
                }
            }

            for ( Iterator it = child.getExecutions().iterator(); it.hasNext(); )
            {
                PluginExecution childExecution = (PluginExecution)it.next();

                if ( !assembledExecutions.containsKey( childExecution.getId() ) )
                {
                    mergedExecutions.add(childExecution);
                }
            }

            child.setExecutions(mergedExecutions);

            child.flushExecutionMap();
        }

    }
    public static void mergePluginLists( PluginContainer childContainer, PluginContainer parentContainer,
                                         boolean handleAsInheritance )
    {
        if ( childContainer == null || parentContainer == null )
        {
            // nothing to do.
            return;
        }

        List mergedPlugins = new ArrayList();

        List parentPlugins = parentContainer.getPlugins();

        if ( parentPlugins != null && !parentPlugins.isEmpty() )
        {
            Map assembledPlugins = new TreeMap();

            Map childPlugins = childContainer.getPluginsAsMap();

            for ( Iterator it = parentPlugins.iterator(); it.hasNext(); )
            {
                Plugin parentPlugin = (Plugin) it.next();

                String parentInherited = parentPlugin.getInherited();

                if ( !handleAsInheritance || parentInherited == null ||
                    Boolean.valueOf( parentInherited ).booleanValue() )
                {

                    Plugin assembledPlugin = parentPlugin;

                    Plugin childPlugin = (Plugin) childPlugins.get( parentPlugin.getKey() );

                    if ( childPlugin != null )
                    {
                        assembledPlugin = childPlugin;

                        mergePluginDefinitions( childPlugin, parentPlugin, handleAsInheritance );
                    }

                    if ( handleAsInheritance && parentInherited == null )
                    {
                        assembledPlugin.unsetInheritanceApplied();
                    }

                    mergedPlugins.add(assembledPlugin);
                }
            }

            for ( Iterator it = childPlugins.values().iterator(); it.hasNext(); )
            {
                Plugin childPlugin = (Plugin) it.next();

                if ( !assembledPlugins.containsKey( childPlugin.getKey() ) )
                {
                    mergedPlugins.add(childPlugin);
                }
            }

            childContainer.setPlugins(mergedPlugins);

            childContainer.flushPluginMap();
        }
    }
    public static List mergeDependencyList( List child, List parent )
    {
        Map depsMap = new HashMap();

        if ( parent != null )
        {
            for ( Iterator it = parent.iterator(); it.hasNext(); )
            {
                Dependency dependency = (Dependency) it.next();
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }

        if ( child != null )
        {
            for ( Iterator it = child.iterator(); it.hasNext(); )
            {
                Dependency dependency = (Dependency) it.next();
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }

        return new ArrayList( depsMap.values() );
    }
