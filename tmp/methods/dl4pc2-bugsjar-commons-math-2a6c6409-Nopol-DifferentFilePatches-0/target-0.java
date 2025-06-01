    public void run() {
        if (org.apache.commons.math3.ml.neuralnet.sofm.KohonenTrainingTask.this.featuresIterator.hasNext()) {
            while (featuresIterator.hasNext()) {
                updateAction.update(net, featuresIterator.next());
            }
        }
    }
