    public void deleteSegmentFile() {
      // If segment build fails with an exception then we will not be able to create a segment file and
      // the file name will be null.
      if (_segmentFile != null) {
        FileUtils.deleteQuietly(new File(_segmentFile));
      }
    }
