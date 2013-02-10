package vifun

import javax.swing.JTable
import javax.swing.JToolTip
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent

public class CurrentTable extends JTable {
    //taken from http://grepcode.com/file/repo1.maven.org/maven2/net.sourceforge.jadex/jadex-tools-comanalyzer/2.2.1/jadex/tools/comanalyzer/ToolColor.java
    public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40)
    public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00)
    public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40)
    public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00)
    def vmodel = griffon.util.ApplicationHolder.application.models.vifun

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setBackground(getBackground());
        //only on delta columns
        if (column !=1 && column != 4) {
            return c
        }
        //  Color row based on a cell value
        if (isRowSelected(row)) {
            return c
        }
        float halfRows
        if (column==1) {
            halfRows = (griffon.util.ApplicationHolder.application.models.vifun.rows as Integer)/2
        }else{
            halfRows = (griffon.util.ApplicationHolder.application.models.vifun.maxScoreDiff)/2
        }
        int modelRow = convertRowIndexToModel(row);
        String delta = getModel().getValueAt(modelRow, column)
        if (delta) {
            if ('+'.equals(delta)) {
                c.setBackground(DARK_GREEN)
            } else {
                float deltaf = delta as Float
                if (deltaf < -halfRows){
                        c.setBackground(DARK_RED)
                }else if (deltaf < 0){
                        c.setBackground(LIGHT_RED)
                }else if (deltaf == 0){
                    return c
                }else if (deltaf < halfRows){
                        c.setBackground(LIGHT_GREEN)
                }else {
                        c.setBackground(DARK_GREEN)
                }
            }
        }
        return c;
    }

    //http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#celltooltip
    public String getToolTipText(MouseEvent e) {
        String tip = "unknown"
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);

        TableModel tmodel = getModel();
        //get id
        String id = vmodel.currentMap[rowIndex].id
        //get doc in current
        def curdoc = vmodel.currentMap.find{it.id==id}
        if (curdoc){
            tip = curdoc.explain
        }
        return tip;
    }
    public JToolTip createToolTip() {
        MultiLineToolTip tip = new MultiLineToolTip();
        tip.setComponent(this);
        return tip;
    }
}
