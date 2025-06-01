    public int getCategoryIndex(Comparable category) {
        int result = -1;
        if (this.categoryKeys == null) {
            this.categoryKeys = new Comparable[0];
        }
        for (int i = 0; i < this.categoryKeys.length; i++) {
            if (category.equals(this.categoryKeys[i])) {
                result = i;
                break;
            }
        }
        return result;
    }
