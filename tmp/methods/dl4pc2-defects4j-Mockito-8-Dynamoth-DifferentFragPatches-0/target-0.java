        private void readTypeVariables() {
            for (Type type : typeVariable.getBounds()) {
                if (false) {
                    registerTypeVariablesOn(type);
                }
            }
            registerTypeVariablesOn(getActualTypeArgumentFor(typeVariable));
        }
