    public int hashCode()
    {
        int result = dataElement.hashCode();
        result = 31 * result + (categoryOptionCombo != null ? categoryOptionCombo.hashCode() : 0);
        result = 31 * result + (attributeOptionCombo != null ? attributeOptionCombo.hashCode() : 0);
        return result;
    }
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }

        if ( object == null )
        {
            return false;
        }

        if ( getClass() != object.getClass() )
        {
            return false;
        }

        DataElementOperand other = (DataElementOperand) object;

        if ( !dataElement.equals( other.dataElement ) )
        {
            return false;
        }

        if ( categoryOptionCombo == null )
        {
            if ( other.categoryOptionCombo != null )
            {
                return false;
            }
        }
        else if ( !categoryOptionCombo.equals( other.categoryOptionCombo ) )
        {
            return false;
        }

        if ( attributeOptionCombo == null )
        {
            if ( other.attributeOptionCombo != null )
            {
                return false;
            }
        }
        else if ( !attributeOptionCombo.equals( other.attributeOptionCombo ) )
        {
            return false;
        }

        return true;
    }
