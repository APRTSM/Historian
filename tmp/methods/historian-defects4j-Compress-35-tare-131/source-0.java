    public static boolean matches(byte[] signature, int length) {
        if (length < TarConstants.VERSION_OFFSET+TarConstants.VERSIONLEN) {
            return false;
        }

        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_POSIX,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
            &&
            ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_POSIX,
                signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                ){
            return true;
        }
        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_GNU,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
            &&
            (
             ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_GNU_SPACE,
                signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
            ||
            ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_GNU_ZERO,
                signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
            )
                ){
            return true;
        }
        // COMPRESS-107 - recognise Ant tar files
        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_ANT,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
            &&
            ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_ANT,
                signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                ){
            return true;
        }
        return false;
    }
