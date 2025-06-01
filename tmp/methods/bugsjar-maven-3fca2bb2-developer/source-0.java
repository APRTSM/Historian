        private boolean isQualifiedForInterpolation( Field field, Class<?> fieldType )
        {
            Boolean primitive = fieldIsPrimitiveByClass.get( fieldType );
            if ( primitive == null )
            {
                primitive = fieldType.isPrimitive();
                fieldIsPrimitiveByClass.put( fieldType, primitive );
            }

            if ( primitive )
            {
                return false;
            }

            return !"parent".equals( field.getName() );
        }
