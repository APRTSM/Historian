    public Status(io.enmasse.address.model.Status other) {
        this.isReady = other.isReady();
        this.messages.addAll(other.getMessages());
    }
