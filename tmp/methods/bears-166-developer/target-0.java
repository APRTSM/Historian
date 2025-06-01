    public Status(io.enmasse.address.model.Status other) {
        this.isReady = other.isReady();
        this.phase = other.getPhase();
        this.messages.addAll(other.getMessages());
    }
