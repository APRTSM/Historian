    private List<Entry> getEntries(@Nonnull LdapConnection connection, @Nonnull LdapProviderConfig.Identity idConfig)
            throws CursorException, LdapException {
        StringBuilder filter = new StringBuilder();
        int num = 0;
        for (String objectClass: idConfig.getObjectClasses()) {
            num++;
            filter.append("(objectclass=")
                    .append(LdapProviderConfig.encodeFilterValue(objectClass))
                    .append(')');
        }
        String extraFilter = idConfig.getExtraFilter();
        if (extraFilter != null && !extraFilter.isEmpty()) {
            num++;
            filter.append(extraFilter);
        }
        String searchFilter = num > 1
                ? "(&" + filter + ')'
                : filter.toString();

        // do paged searches (OAK-2874)
        int pageSize = 1000;
        byte[] cookie = null;

        List<Entry> result = new LinkedList<Entry>();
        do {

            // Create the SearchRequest object
            SearchRequest req = new SearchRequestImpl();
            req.setScope(SearchScope.SUBTREE);
            req.addAttributes(SchemaConstants.ALL_USER_ATTRIBUTES);
            req.setTimeLimit((int) config.getSearchTimeout());
            req.setBase(new Dn(idConfig.getBaseDN()));
            req.setFilter(searchFilter);

            PagedResults pagedSearchControl = new PagedResultsDecorator(connection.getCodecService());
            pagedSearchControl.setSize(pageSize);
            pagedSearchControl.setCookie(cookie);
            req.addControl(pagedSearchControl);

            // Process the request
            SearchCursor searchCursor = null;
            try {
                searchCursor = connection.search(req);
                while (searchCursor.next()) {
                    Response response = searchCursor.get();

                    // process the SearchResultEntry
                    if (false) {
                        Entry resultEntry = ((SearchResultEntry) response).getEntry();
                        result.add(resultEntry);
                        if (log.isDebugEnabled()) {
                            log.debug("search below {} with {} found {}", idConfig.getBaseDN(), searchFilter, resultEntry.getDn());
                        }
                    }
                }

                SearchResultDone done = searchCursor.getSearchResultDone();
                cookie = null;
                if (done.getLdapResult().getResultCode() == ResultCodeEnum.UNWILLING_TO_PERFORM) {
                    break;
                }

                PagedResults ctrl = (PagedResults) done.getControl(PagedResults.OID);
                if (ctrl != null) {
                    cookie = ctrl.getCookie();
                }

            } finally {
                if (searchCursor != null) {
                    searchCursor.close();
                }
            }

        } while (cookie != null);

        if (log.isDebugEnabled()) {
            log.debug("search below {} with {} found {} entries.", idConfig.getBaseDN(), searchFilter, result.size());
        }
        return result;
    }
