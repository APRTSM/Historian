    public TarArchiveEntry getNextTarEntry() throws IOException {
        if (hasHitEOF) {
            throw new IOException("The stream is closed");

        }

        if (currEntry != null) {
            /* Skip will only go to the end of the current entry */
            skip(Long.MAX_VALUE);

            /* skip to the end of the last record */
            skipRecordPadding();
        }

        byte[] headerBuf = getRecord();

        if (headerBuf == null) {
            /* hit EOF */
            currEntry = null;
            return null;
        }

        try {
            currEntry = new TarArchiveEntry(headerBuf, encoding);
        } catch (IllegalArgumentException e) {
            IOException ioe = new IOException("Error detected parsing the header");
            ioe.initCause(e);
            throw ioe;
        }

        entryOffset = 0;
        entrySize = currEntry.getSize();

        if (currEntry.isGNULongLinkEntry()) {
            byte[] longLinkData = getLongNameData();
            if (longLinkData == null) {
                // Bugzilla: 40334
                // Malformed tar file - long link entry name not followed by
                // entry
                return null;
            }
            currEntry.setLinkName(encoding.decode(longLinkData));
        }

        if (currEntry.isGNULongNameEntry()) {
            byte[] longNameData = getLongNameData();
            if (longNameData == null) {
                // Bugzilla: 40334
                // Malformed tar file - long entry name not followed by
                // entry
                return null;
            }
            currEntry.setName(encoding.decode(longNameData));
        }

        if (currEntry.isPaxHeader()){ // Process Pax headers
            paxHeaders();
        }

        if (currEntry.isGNUSparse()){ // Process sparse files
            readGNUSparse();
        }

        // If the size of the next element in the archive has changed
        // due to a new size being reported in the posix header
        // information, we update entrySize here so that it contains
        // the correct value.
        entrySize = currEntry.getSize();

        return currEntry;
    }
