    public void remove(SettableBeanProperty propToRm) {
        ArrayList<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>(_size);
        String key = getPropertyName(propToRm);
        boolean found = false;

        for (int i = 1, end = _hashArea.length; i < end; i += 2) {
            SettableBeanProperty prop = (SettableBeanProperty) _hashArea[i];
            if (prop == null) {
                continue;
            }
            if (!found) {
                found = key.equals(prop.getName());
                if (found) {
                    // need to leave a hole here
                    _propsInOrder[_findFromOrdered(prop)] = null;
                    continue;
                }
            }
            props.add(prop);
        }
        if (!found) {
            throw new NoSuchElementException("No entry '"+propToRm.getName()+"' found, can't remove");
        }
        init(props);
    }
    protected void init(Collection<SettableBeanProperty> props)
    {
        _size = props.size();
        
        // First: calculate size of primary hash area
        final int hashSize = findSize(_size);
        _hashMask = hashSize-1;

        // and allocate enough to contain primary/secondary, expand for spillovers as need be
        int alloc = (hashSize + (hashSize>>1)) * 2;
        Object[] hashed = new Object[alloc];
        int spillCount = 0;

        for (SettableBeanProperty prop : props) {
            // Due to removal, renaming, theoretically possible we'll have "holes" so:
            if (prop == null) {
                continue;
            }
            
            String key = getPropertyName(prop);
            int slot = _hashCode(key);
            int ix = (slot<<1);

            // primary slot not free?
            if (hashed[ix] != null) {
                // secondary?
                ix = (hashSize + (slot >> 1)) << 1;
                if (hashed[ix] != null) {
                    // ok, spill over.
                    ix = ((hashSize + (hashSize >> 1) ) << 1) + spillCount;
                    spillCount += 2;
                    if (ix >= hashed.length) {
                        hashed = Arrays.copyOf(hashed, hashed.length + 4);
                    }
                }
            }
//System.err.println(" add '"+key+" at #"+(ix>>1)+"/"+size+" (hashed at "+slot+")");             
            hashed[ix] = key;
            hashed[ix+1] = prop;
        }
/*
for (int i = 0; i < hashed.length; i += 2) {
System.err.printf("#%02d: %s\n", i>>1, (hashed[i] == null) ? "-" : hashed[i]);
}
*/
        _hashArea = hashed;
        _spillCount = spillCount;
    }
