package pers.gjn.remake;

import com.alibaba.excel.metadata.Sheet;

import java.util.List;
import java.util.Map;

/**
 * @author gjn
 * @date 2018/6/29
 * 拓展的excel builder实现类
 */
public class CustomExcelBuilderImpl extends ExcelBuilderImpl implements CustomExcelBuilder {

    @Override
    public void addContent(List data, Sheet sheetParam, Map<Integer, Integer> colWidth) {
        addContent(data, sheetParam);
        org.apache.poi.ss.usermodel.Sheet poiSheet = context.getCurrentSheet();
        for (int i : colWidth.keySet()) {
            poiSheet.setColumnWidth(i, 252 * colWidth.get(i) + 323);
        }
        //冻结第一行（head）
        poiSheet.createFreezePane(0, 1, 0, 1);
    }
}
