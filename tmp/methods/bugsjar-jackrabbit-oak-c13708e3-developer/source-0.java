    public long countDeleteChunks(List<String> chunkIds, long maxLastModifiedTime) throws Exception {
        long count = 0;

        for (List<String> chunk : Lists.partition(chunkIds, RDBJDBCTools.MAX_IN_CLAUSE)) {
            Connection con = this.ch.getRWConnection();
            PreparedStatement prepMeta = null;
            PreparedStatement prepData = null;

            try {
                PreparedStatementComponent inClause = RDBJDBCTools.createInStatement("ID", chunk, false);

                StringBuilder metaStatement = new StringBuilder("delete from " + this.tnMeta + " where ")
                        .append(inClause.getStatementComponent());
                StringBuilder dataStatement = new StringBuilder("delete from " + this.tnData + " where ")
                        .append(inClause.getStatementComponent());

                if (maxLastModifiedTime > 0) {
                    metaStatement.append(" and LASTMOD <= ?");
                    dataStatement.append(" and not exists(select * from " + this.tnMeta + " m where ID = m.ID and m.LASTMOD <= ?)");
                }

                prepMeta = con.prepareStatement(metaStatement.toString());
                prepData = con.prepareStatement(dataStatement.toString());

                int mindex = 1, dindex = 1;
                mindex = inClause.setParameters(prepMeta, mindex);
                dindex = inClause.setParameters(prepData, dindex);

                if (maxLastModifiedTime > 0) {
                    prepMeta.setLong(mindex, maxLastModifiedTime);
                    prepData.setLong(dindex, maxLastModifiedTime);
                }

                count += prepMeta.executeUpdate();
                prepData.execute();
            } finally {
                closeStatement(prepMeta);
                closeStatement(prepData);
                con.commit();
                this.ch.closeConnection(con);
            }
        }

        return count;
    }
