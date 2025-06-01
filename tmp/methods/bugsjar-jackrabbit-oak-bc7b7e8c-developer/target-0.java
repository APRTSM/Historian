    private boolean internalAddEntry(@Nonnull ACE entry) throws RepositoryException {
        final Principal principal = entry.getPrincipal();
        List<ACE> subList = Lists.newArrayList(Iterables.filter(entries, new Predicate<ACE>() {
            @Override
            public boolean apply(@Nullable ACE ace) {
                return (ace != null) && ace.getPrincipal().getName().equals(principal.getName());
            }
        }));

        for (ACE existing : subList) {
            PrivilegeBits existingBits = PrivilegeBits.getInstance(existing.getPrivilegeBits());
            PrivilegeBits entryBits = entry.getPrivilegeBits();
            if (entry.getRestrictions().equals(existing.getRestrictions())) {
                if (entry.isAllow() == existing.isAllow()) {
                    if (existingBits.includes(entryBits)) {
                        // no changes
                        return false;
                    } else {
                        // merge existing and new ace
                        existingBits.add(entryBits);
                        int index = entries.indexOf(existing);
                        entries.remove(existing);
                        entries.add(index, createACE(existing, existingBits));
                        return true;
                    }
                } else {
                    // existing is complementary entry -> clean up redundant
                    // privileges defined by the existing entry
                    PrivilegeBits updated = PrivilegeBits.getInstance(existingBits).diff(entryBits);
                    if (updated.isEmpty()) {
                        // remove the existing entry as the new entry covers all privileges
                        entries.remove(existing);
                    } else if (!updated.includes(existingBits)) {
                        // replace the existing entry having it's privileges adjusted
                        int index = entries.indexOf(existing);
                        entries.remove(existing);
                        entries.add(index, createACE(existing, updated));
                    } /* else: no collision that requires adjusting the existing entry.*/
                }
            }
        }
        // finally add the new entry at the end of the list
        entries.add(entry);
        return true;
    }
