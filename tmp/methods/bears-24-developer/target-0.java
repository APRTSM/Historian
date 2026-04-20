    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() == getClass()) {
            // 16-Jun-2017, tatu: as per [databind#1658], can not do recursive call since
            //    there is likely to be a cycle...

            // but... true or false?
            return false;

            /*
            // Do NOT ever match unresolved references
            if (_referencedType == null) {
                return false;
            }
            return (o.getClass() == getClass()
                    && _referencedType.equals(((ResolvedRecursiveType) o).getSelfReferencedType()));
                    */
        }
        return false;
    }
