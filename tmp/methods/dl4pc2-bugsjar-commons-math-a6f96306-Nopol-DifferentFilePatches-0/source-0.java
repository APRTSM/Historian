    public void update(Network net,
                       double[] features) {
        final long numCalls = numberOfCalls.incrementAndGet();
        final double currentLearning = learningFactor.value(numCalls);
        final Neuron best = findAndUpdateBestNeuron(net,
                                                    features,
                                                    currentLearning);

        final int currentNeighbourhood = neighbourhoodSize.value(numCalls);
        // The farther away the neighbour is from the winning neuron, the
        // smaller the learning rate will become.
        final Gaussian neighbourhoodDecay
            = new Gaussian(currentLearning,
                           0,
                           1d / currentNeighbourhood);

        if (currentNeighbourhood > 0) {
            // Initial set of neurons only contains the winning neuron.
            Collection<Neuron> neighbours = new HashSet<Neuron>();
            neighbours.add(best);
            // Winning neuron must be excluded from the neighbours.
            final HashSet<Neuron> exclude = new HashSet<Neuron>();
            exclude.add(best);

            int radius = 1;
            do {
                // Retrieve immediate neighbours of the current set of neurons.
                neighbours = net.getNeighbours(neighbours, exclude);

                // Update all the neighbours.
                for (Neuron n : neighbours) {
                    updateNeighbouringNeuron(n, features, neighbourhoodDecay.value(radius));
                }

                // Add the neighbours to the exclude list so that they will
                // not be update more than once per training step.
                exclude.addAll(neighbours);
                ++radius;
            } while (radius <= currentNeighbourhood);
        }
    }
