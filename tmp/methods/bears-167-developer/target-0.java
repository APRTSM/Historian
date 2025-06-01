    private void validateAddresses(AddressSpace addressSpace, AddressList addressList) {
        Schema schema = schemaProvider.getSchema();
        AddressSpaceType type = schema.findAddressSpaceType(addressSpace.getType()).orElseThrow(() -> new UnresolvedAddressSpaceException("Unable to resolve address space type " + addressSpace.getType()));

        AddressResolver addressResolver = new AddressResolver(schema, type);
        Set<Address> existingAddresses = addressSpaceApi.withAddressSpace(addressSpace).listAddresses();
        for (Address address : addressList) {
            addressResolver.validate(address);
            for (Address existing : existingAddresses) {
                if (address.getAddress().equals(existing.getAddress()) && !address.getName().equals(existing.getName())) {
                    throw new BadRequestException("Address '" + address.getAddress() + "' already exists with resource name '" + existing.getName() + "'");
                }
            }

            for (Address b : addressList) {
                if (address.getAddress().equals(b.getAddress()) && !address.getName().equals(b.getName())) {
                    throw new BadRequestException("Address '" + address.getAddress() + "' defined in resource names '" + address.getName() + "' and '" + b.getName() + "'");
                }
            }
        }
    }
