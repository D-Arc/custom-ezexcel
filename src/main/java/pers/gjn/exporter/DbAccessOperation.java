package pers.gjn.exporter;

import java.util.List;
import java.util.Map;

/**
 * @author gjn
 * @date 2018/6/12
 * 用于连接数据库、获取表信息的接口
 */
public interface DbAccessOperation {

    /**
     * 获取所有表的表名和注释
     * @return List of Map(map.keySet = [TABLE_NAME, COMMENTS])
     */
    List<Map<String, String>> getAllTablesAndComments();

    /**
     * 获取表的列名和注释
     * @param tableName 表名
     * @return List of Map(map.keySet = [TABLE_NAME, COLUMN_NAME, COMMENTS])
     */
    List<Map<String, String>> getTableColumnAndComments(String tableName);

    /**
     * 通过sql和参数查询结果的集的方法
     * @param sql 查询sql
     * @param params 参数集
     * @return 结果集的List of List(String)
     */
    List<List<String>> findModeResult(String sql, List<Object> params);
}
