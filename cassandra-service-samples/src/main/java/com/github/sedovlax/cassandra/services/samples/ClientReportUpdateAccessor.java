package com.github.sedovlax.cassandra.services.samples;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Created by Alexander
 * on 14.01.2016.
 */
@Accessor
interface ClientReportUpdateAccessor {
    @Query("update client_region set is_deleted = true where region = ?")
    Result<ClientReport> markAllByRegion(int region);

    @Query("delete from client_region where region = ?")
    ResultSet deleteAllInRegion(int region);

    @Query("update client_region set is_deleted = true where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ?")
    ListenableFuture<Result<ClientReport>> markAllByClient(int region, String templateCode, int periodYear, int periodCode, long clientId);

    @Query("update client_region set data = ? where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ?")
    Statement updateData(String newData, int region, String templateCode, int periodYear, int periodCode, long clientId);
}
