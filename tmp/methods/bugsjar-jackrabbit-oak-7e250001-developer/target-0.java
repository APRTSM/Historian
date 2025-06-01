        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key) obj;
                return name.equals(other.name) &&
                        (revision != null ? revision.equals(other.revision) : other.revision == null);
            }
            return false;
        }
