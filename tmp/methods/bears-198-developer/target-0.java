        private Builder(DecryptionMaterialsRequest request) {
            this.algorithm = request.getAlgorithm();
            this.encryptionContext = request.getEncryptionContext();
            this.encryptedDataKeys = request.getEncryptedDataKeys();
        }
