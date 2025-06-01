    public void initialize(File homeDir) throws Exception {
        File dbDir = new File(homeDir, "db");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        Driver.load();
        String url = "jdbc:h2:" + dbDir.getCanonicalPath() + "/revs";
        if (FAST) {
            url += ";log=0;undo_log=0";
        }
        cp = JdbcConnectionPool.create(url, "sa", "");
        cp.setMaxConnections(40);
        Connection con = cp.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("create table if not exists REVS(ID binary primary key, DATA binary, TIME timestamp)");
            stmt.execute("create table if not exists HEAD(ID binary) as select null");
            stmt.execute("create sequence if not exists DATASTORE_ID");
/*
            DbBlobStore store = new DbBlobStore();
            store.setConnectionPool(cp);
            blobStore = store;
*/
        } finally {
            con.close();
        }
    }
    public int sweep() throws Exception {
        Timestamp ts = new Timestamp(gcStart);

        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con
                    .prepareStatement(
                            "delete REVS where TIME < ?");
            try {
                stmt.setTimestamp(1, ts);
                return stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
     }
    public Id writeNode(Node node) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        node.serialize(new BinaryBinding(out));
        byte[] bytes = out.toByteArray();
        byte[] rawId = idFactory.createContentId(bytes);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        //String id = StringUtils.convertBytesToHex(rawId);

        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con
                    .prepareStatement(
                            "insert into REVS (ID, DATA, TIME) select ?, ?, ? where not exists (select 1 from REVS where ID = ?)");
            try {
                stmt.setBytes(1, rawId);
                stmt.setBytes(2, bytes);
                stmt.setTimestamp(3, ts);
                stmt.setBytes(4, rawId);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
        return new Id(rawId);
    }
    public boolean markCommit(Id id) throws Exception {
        return touch(id, gcStart);
    }
    public Id writeCNEMap(ChildNodeEntries map) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        map.serialize(new BinaryBinding(out));
        byte[] bytes = out.toByteArray();
        byte[] rawId = idFactory.createContentId(bytes);
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con
                    .prepareStatement(
                            "insert into REVS (ID, DATA, TIME) select ?, ?, ? where not exists (select 1 from REVS where ID = ?)");
            try {
                stmt.setBytes(1, rawId);
                stmt.setBytes(2, bytes);
                stmt.setTimestamp(3, ts);
                stmt.setBytes(4, rawId);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
        return new Id(rawId);
    }
    public boolean markNode(Id id) throws Exception {
        return touch(id, gcStart);
    }
    public Id readHead() throws Exception {
        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("select * from HEAD");
            ResultSet rs = stmt.executeQuery();
            byte[] rawId = null;
            if (rs.next()) {
                rawId = rs.getBytes(1);
            }
            stmt.close();
            return rawId == null ? null : new Id(rawId); 
        } finally {
            con.close();
        }
    }
    private boolean touch(Id id, long timeMillis) throws Exception {
        Timestamp ts = new Timestamp(timeMillis);

        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con
                    .prepareStatement(
                            "update REVS set TIME = ? where ID = ? and TIME < ?");
            try {
                stmt.setTimestamp(1, ts);
                stmt.setBytes(2, id.getBytes());
                stmt.setTimestamp(3, ts);
                return stmt.executeUpdate() == 1;
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
    }
    public ChildNodeEntriesMap readCNEMap(Id id) throws NotFoundException, Exception {
        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("select DATA from REVS where ID = ?");
            try {
                stmt.setBytes(1, id.getBytes());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ByteArrayInputStream in = new ByteArrayInputStream(rs.getBytes(1));
                    return ChildNodeEntriesMap.deserialize(new BinaryBinding(in));
                } else {
                    throw new NotFoundException(id.toString());
                }
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
    }
    public void readNode(StoredNode node) throws NotFoundException, Exception {
        Id id = node.getId();
        Connection con = cp.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("select DATA from REVS where ID = ?");
            try {
                stmt.setBytes(1, id.getBytes());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ByteArrayInputStream in = new ByteArrayInputStream(rs.getBytes(1));
                    node.deserialize(new BinaryBinding(in));
                } else {
                    throw new NotFoundException(id.toString());
                }
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
    }
    public boolean markCNEMap(Id id) throws Exception {
        return touch(id, gcStart);
    }
