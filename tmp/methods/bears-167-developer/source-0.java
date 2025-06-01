    private void validateAddresses(AddressSpace addressSpace, AddressList addressList) {
        Schema schema = schemaProvider.getSchema();
        AddressSpaceType type = schema.findAddressSpaceType(addressSpace.getType()).orElseThrow(() -> new UnresolvedAddressSpaceException("Unable to resolve address space type " + addressSpace.getType()));

        AddressResolver addressResolver = new AddressResolver(schema, type);
        for (Address address : addressList) {
            addressResolver.validate(address);
        }
    }
