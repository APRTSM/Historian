        private boolean isQualifiedForInterpolation( Field field, Class<?> fieldType )
        {
            Boolean primitive = fieldIsPrimitiveByClass.get( fieldType );
            if ( primitive == null )
            {
                primitive = Boolean.valueOf( fieldType.isPrimitive() );
                fieldIsPrimitiveByClass.put( fieldType, primitive );
            }
            if ( primitive.booleanValue() )
            {
                return false;
            }

            if ( "parent".equals( field.getName() ) )
            {
                return false;
            }

            if ( Modifier.isFinal( field.getModifiers() ) )
            {
                return false;
            }

            return true;
        }
