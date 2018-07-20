package dbtrans;

import com.alibaba.excel.metadata.Font;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.TableStyle;
import com.alibaba.excel.support.ExcelTypeEnum;
import pers.gjn.exporter.DbAccessOperation;
import pers.gjn.exporter.OracleDbAccessOperation;
import pers.gjn.remake.ExcelWriter;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gjn
 * @date 2018/6/12
 * 导出某数据库所有数据至excel的方法测试类
 */
public class TableExportTest {

    // 多sheet无模板、无注解导出
    public static void testBatchTable2Excel() throws Exception {

        DbAccessOperation access = new OracleDbAccessOperation();

        OutputStream out = new FileOutputStream("77.xlsx");
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX,true);

        // 生成sheet数据
//        List<SimpleSheetWrapper> list = new ArrayList<>();
        List<Map<String, String>> tables = access.getAllTablesAndComments();
        for (int i = 0; i < tables.size(); i++) {
            Map<String, String> tab = tables.get(i);

            //获取表注释
            String tabName = tab.get("TABLE_NAME");
            String tabCom = tab.get("COMMENTS");
            if (tabCom == null || "".equals(tabCom)) {
                tabCom = tabName;
            }

            //表格内容数据
            List<Map<String, String>> cols = access.getTableColumnAndComments(tabName);
            List<List<String>> header = new ArrayList<>();
            List<String> colNames = new ArrayList<>();
            for (Map<String, String> col : cols) {
                String colName = col.get("COLUMN_NAME");
                String colCom = col.get("COMMENTS");
                if (colCom == null || "".equals(colCom)) {
                    colCom = colName;
                }
                List<String> singleHead = new ArrayList<>();
                singleHead.add(colCom);
                // 表头数据
                header.add(singleHead);
                colNames.add(colName);
            }
            Sheet sheet = new Sheet(i, 1, null, tabCom, header);

            //TODO:若想使用alert样式，必须在此setTableStyle，有待改进
            sheet.setTableStyle(getTableStyle());
            // 行数据集合
            List<List<String>> data = getData(access, tabName, colNames);
            //sheet写入excel
            writer.writerWidth(data, sheet, new HashMap<Integer, Integer>(){{put(1, 57);}});
        }
        writer.finish();
    }

    private static List<List<String>> getData(DbAccessOperation access, String tabName, List<String> colNames) {
        //TODO:此处sql应加条件，而不是全查出来
        StringBuilder sql = new StringBuilder(128);
        sql.append("SELECT ");
        for (String col : colNames) {
            sql.append(col).append(",");
        }
        sql.deleteCharAt(sql.length() - 1).append(" FROM ").append(tabName).append(" WHERE ROWNUM < 100");
        System.out.println(sql.toString());
        return access.findModeResult(sql.toString(), null);
    }

    private static TableStyle getTableStyle() {
        TableStyle tableStyle = new TableStyle();
        Font headFont = new Font();
        headFont.setBold(true);
        headFont.setFontName("黑体");
        headFont.setFontHeightInPoints((short)14);
        tableStyle.setTableHeadFont(headFont);
        tableStyle.setTableHeadBackGroundColor(IndexedColors.GREY_25_PERCENT);

        Font contentFont = new Font();
        contentFont.setBold(false);
        contentFont.setFontHeightInPoints((short)11);
        contentFont.setFontName("宋体");
        tableStyle.setTableContentFont(contentFont);
        tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);
        return tableStyle;
    }

    public static void main(String[] args) {
        try {
            testBatchTable2Excel();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //从sql中拿出表名的工具方法
    private static String pickTableName(String upperCasedSql){
        Matcher matcher;
        String sql = upperCasedSql.replace("FROM", "from");
        sql = sql.replaceFirst("from", "FROM");
        matcher = Pattern.compile("SELECT\\s.+FROM\\s(.+)WHERE\\s(.*)").matcher(sql);
        if(matcher.find()){
            String match = matcher.group(1);
            match = match.trim().split(" ")[0];
            if(match.contains(".")){
                String[] matchSlice = match.split("[.]");
                match = matchSlice[matchSlice.length-1];
            }
            return match;
        }else{
            return null;
        }
    }

    //判断并补全where关键字的方法
    private static String judgeWhereClause(String sql){
        sql = sql.toUpperCase();
        if(!sql.contains("WHERE")){
            return sql + " WHERE 1=1 ";
        }else{
            return sql;
        }
    }
}
