package pers.gjn.remake;

import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.write.ExcelBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author gjn
 * @date 2018/6/29
 */
public interface CustomExcelBuilder extends ExcelBuilder {

    /**
     * 扩展的方法，可通过map设置column宽度
     * @param data 数据
     * @param sheetParam sheet参数
     * @param colWidth 设置col宽度的map：key是列index下标, value为宽度
     */
    void addContent(List data, Sheet sheetParam, Map<Integer, Integer> colWidth);

}
