        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
                return ((PutTokenImpl) obj).id == id;
            }
            return super.equals(obj);
        }
