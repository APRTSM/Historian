    public void run() {
        while (featuresIterator.hasNext()) {
            updateAction.update(net, featuresIterator.next());
        }
    }
