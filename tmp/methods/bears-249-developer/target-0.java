    public CommittedQueueBox(CommittedQueueBox commitDepBox) {
        this.dots = new Dots(commitDepBox.dots);
        if (commitDepBox.dep != null) {
            this.dep = new Clock<>(commitDepBox.dep);
        } else {
            this.dep = null;
        }
        this.messageMap = new MessageMap(commitDepBox.messageMap);
    }
