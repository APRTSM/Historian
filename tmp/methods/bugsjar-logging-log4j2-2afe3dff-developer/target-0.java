    public static boolean execute(final File source, final File destination, boolean renameEmptyFiles) {
        if (renameEmptyFiles || (source.length() > 0)) {
            File parent = destination.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    LOGGER.error("Unable to create directory {}", parent.getAbsolutePath());
                    return false;
                }
            }
            try {
                if (!source.renameTo(destination)) {
                    try {
                        copyFile(source, destination);
                        return source.delete();
                    } catch (IOException iex) {
                        LOGGER.error("Unable to rename file {} to {} - {}", source.getAbsolutePath(),
                            destination.getAbsolutePath(), iex.getMessage());
                    }
                }
                return true;
            } catch (Exception ex) {
                try {
                    copyFile(source, destination);
                    return source.delete();
                } catch (IOException iex) {
                    LOGGER.error("Unable to rename file {} to {} - {}", source.getAbsolutePath(),
                        destination.getAbsolutePath(), iex.getMessage());
                }
            }
        }

        return false;
    }
