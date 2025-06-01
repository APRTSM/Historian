    public CommittedQueueBox(CommittedQueueBox commitDepBox) {
        this.dots = new Dots(commitDepBox.dots);
        this.dep = new Clock<>(commitDepBox.dep);
        this.messageMap = new MessageMap(commitDepBox.messageMap);
    }
