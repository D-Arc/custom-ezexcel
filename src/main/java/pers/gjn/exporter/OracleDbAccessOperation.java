package pers.gjn.exporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjn
 * @date 2018/6/12
 * oracle数据库的数据库连接实现类
 */
public class OracleDbAccessOperation implements DbAccessOperation{

    /**
     * 数据库地址
     */
    private static final String URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
    /**
     * 数据库用户名
     */
    private static final String USERNAME = "username";
    /**
     * 数据库密码
     */
    private static final String PASSWORD = "password";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Connection connection;

    public OracleDbAccessOperation() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("数据库连接成功！");
        } catch (Exception e) {
            System.out.println("连接数据库时发生异常！");
        }
    }

    /**
     * 获得数据库的连接
     */
    private void getConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("连接数据库时发生异常！");
            e.printStackTrace();
        }
    }

    /**
     * 关闭数据库的连接
     */
    private void closeConnection() {
        try {
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("关闭数据库连接发生异常！");
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, String>> getAllTablesAndComments() {
        getConnection();
        final String sql = "SELECT TABLE_NAME, COMMENTS FROM USER_TAB_COMMENTS";
        List<Map<String, String>> list = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>(2);
                try {
                    map.put("TABLE_NAME", resultSet.getString("TABLE_NAME"));
                    map.put("COMMENTS", resultSet.getString("COMMENTS"));
                    list.add(map);
                }catch (Exception e){
                    System.out.println("存在一条异常表数据，已跳过。");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e){
            System.out.println("查询所有表名异常，返回空列表。");
            e.printStackTrace();
            return new ArrayList<>();
        }
        closeConnection();
        return list;
    }

    @Override
    public List<Map<String, String>> getTableColumnAndComments(String tableName) {
        getConnection();
        final String sql = "SELECT * FROM USER_COL_COMMENTS WHERE TABLE_NAME=?";
        List<Map<String, String>> list = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                try {
                    map.put("TABLE_NAME", resultSet.getString("TABLE_NAME"));
                    map.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
                    map.put("COMMENTS", resultSet.getString("COMMENTS"));
                    list.add(map);
                }catch (Exception e){
                    System.out.println("存在一条异常列数据，已跳过。");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e){
            System.out.println("查询所有列名发生异常，返回空列表。");
            e.printStackTrace();
            return new ArrayList<>();
        }
        closeConnection();
        return list;
    }

    @Override
    public List<List<String>> findModeResult(String sql, List<Object> params){
        getConnection();
        List<List<String>> dist = new ArrayList<>();
        int index = 1;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (params != null && !params.isEmpty()) {
                for (Object param : params) {
                    preparedStatement.setObject(index++, param);
                }
            }

            long st = System.currentTimeMillis();
            System.out.println("querying...");
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("query fin,"+" cost "+(System.currentTimeMillis()-st)+"ms");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int colsLen = metaData.getColumnCount();

            int fmtIndex = 0;
            while (resultSet.next()) {
                fmtIndex++;
                if(fmtIndex%100 == 0){
                    System.out.println("formatting row: "+fmtIndex);
                }

                List<String> row = new ArrayList<>();
                try {
                    for (int i = 0; i < colsLen; i++) {
                        Object colsValue = resultSet.getObject(i + 1);
                        if (colsValue == null) {
                            colsValue = "";
                        }
                        if (colsValue instanceof java.util.Date) {
                            Timestamp v = resultSet.getTimestamp(i + 1);
                            colsValue = DATE_FORMAT.format(v);
                        }
                        row.add(String.valueOf(colsValue));
                    }
                    dist.add(row);
                } catch (Exception e){
                    System.out.println("查询结果有一条发生数据异常，已跳过。");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e){
            System.out.println("查询所有数据发生异常，返回空列表。");
            e.printStackTrace();
            return new ArrayList<>();
        }
        closeConnection();
        return dist;
    }
}
