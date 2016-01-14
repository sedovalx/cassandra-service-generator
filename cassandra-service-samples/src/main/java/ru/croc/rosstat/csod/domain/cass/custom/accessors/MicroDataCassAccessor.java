package ru.croc.rosstat.csod.domain.cass.custom.accessors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.util.concurrent.ListenableFuture;
import ru.croc.rosstat.csod.domain.cass.MicroDataCass;

/**
 * Author alsedov on 12.01.2016
 */
@Accessor
public interface MicroDataCassAccessor {
    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    MicroDataCass deleteById1(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ResultSet deleteById2(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    Result<MicroDataCass> deleteById3(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ListenableFuture<MicroDataCass> deleteById4(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ResultSetFuture deleteById5(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    MicroDataCass updateById1(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ResultSet updateById2(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    Result<MicroDataCass> updateById3(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ListenableFuture<MicroDataCass> updateById4(int togs, String templateCode, int periodYear, int periodCode);

    @Query("delete from micro_data where togs = ? and tpl_code = ? and p_year = ? and p_code = ?")
    ResultSetFuture updateById5(int togs, String templateCode, int periodYear, int periodCode);
}
