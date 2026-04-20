    private static int extractOptions(String pattern, int i, List<String> options) {
        while ((i < pattern.length()) && (pattern.charAt(i) == '{')) {
            int begin = i++;
            int end;
            int depth = 0;
            do {
                end = pattern.indexOf('}', i);
                if (end != -1) {
                    int next = pattern.indexOf("{", i);
                    if (next != -1 && next < end) {
                        i = end + 1;
                        ++depth;
                    } else if (depth > 0) {
                        --depth;
                    }
                }
            } while (depth > 0);

            if (end == -1) {
                break;
            }

            String r = pattern.substring(begin + 1, end);
            options.add(r);
            i = end + 1;
        }

        return i;
    }
