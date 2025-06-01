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

	    // FIXME: not sure what's intended here, but this entire
	    // loop can be replaced by 'mergedPlugins.addAll( childPlugins.values() );
	    // since assembledPlugins is never updated and remains empty.
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
