    private void parseTarHeader(byte[] header, ZipEncoding encoding,
                                final boolean oldStyle)
        throws IOException {
        int offset = 0;

        name = oldStyle ? TarUtils.parseName(header, offset, NAMELEN)
            : TarUtils.parseName(header, offset, NAMELEN, encoding);
        offset += NAMELEN;
        mode = (int) TarUtils.parseOctalOrBinary(header, offset, MODELEN);
        offset += MODELEN;
        userId = (int) TarUtils.parseOctalOrBinary(header, offset, UIDLEN);
        offset += UIDLEN;
        groupId = (int) TarUtils.parseOctalOrBinary(header, offset, GIDLEN);
        offset += GIDLEN;
        size = TarUtils.parseOctalOrBinary(header, offset, SIZELEN);
        offset += SIZELEN;
        modTime = TarUtils.parseOctalOrBinary(header, offset, MODTIMELEN);
        offset += MODTIMELEN;
        checkSumOK = TarUtils.verifyCheckSum(header);
        offset += CHKSUMLEN;
        linkFlag = header[offset++];
        linkName = oldStyle ? TarUtils.parseName(header, offset, NAMELEN)
            : TarUtils.parseName(header, offset, NAMELEN, encoding);
        offset += NAMELEN;
        magic = TarUtils.parseName(header, offset, MAGICLEN);
        offset += MAGICLEN;
        version = TarUtils.parseName(header, offset, VERSIONLEN);
        offset += VERSIONLEN;
        userName = oldStyle ? TarUtils.parseName(header, offset, UNAMELEN)
            : TarUtils.parseName(header, offset, UNAMELEN, encoding);
        offset += UNAMELEN;
        groupName = oldStyle ? TarUtils.parseName(header, offset, GNAMELEN)
            : TarUtils.parseName(header, offset, GNAMELEN, encoding);
        offset += GNAMELEN;
        devMajor = (int) TarUtils.parseOctalOrBinary(header, offset, DEVLEN);
        offset += DEVLEN;
        devMinor = (int) TarUtils.parseOctalOrBinary(header, offset, DEVLEN);
        offset += DEVLEN;

        int type = evaluateType(header);
        switch (type) {
        case FORMAT_OLDGNU: {
            offset += ATIMELEN_GNU;
            offset += CTIMELEN_GNU;
            offset += OFFSETLEN_GNU;
            offset += LONGNAMESLEN_GNU;
            offset += PAD2LEN_GNU;
            offset += SPARSELEN_GNU;
            isExtended = TarUtils.parseBoolean(header, offset);
            offset += ISEXTENDEDLEN_GNU;
            realSize = TarUtils.parseOctal(header, offset, REALSIZELEN_GNU);
            offset += REALSIZELEN_GNU;
            break;
        }
        case FORMAT_POSIX:
        default: {
            String prefix = oldStyle
                ? TarUtils.parseName(header, offset, PREFIXLEN)
                : TarUtils.parseName(header, offset, PREFIXLEN, encoding);
            // SunOS tar -E does not add / to directory names, so fix
            // up to be consistent
            if (isDirectory() && !name.endsWith("/")){
                name = name + "/";
            }
            if (prefix.length() > 0){
                version = version.substring(0, offset);
name = prefix + "/" + name;
            }
        }
        }
    }
