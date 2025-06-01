    public static boolean execute(final File source, final File destination, boolean renameEmptyFiles) {
        if (renameEmptyFiles || (source.length() > 0)) {
            try {

                boolean result = source.renameTo(destination);
                //System.out.println("Rename of " + source.getName() + " to " + destination.getName() + ": " + result);
                return result;
            } catch (Exception ex) {
                try {
                    copyFile(source, destination);
                    return source.delete();
                } catch (IOException iex) {
                    iex.printStackTrace();
                }
            }
        }

        return false;
    }
