package vifun

import javax.swing.JTable
import javax.swing.JToolTip
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent

public class BaselineTable extends JTable {
    def vmodel = griffon.util.ApplicationHolder.application.models.vifun

    //http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#celltooltip
    public String getToolTipText(MouseEvent e) {
        String tip = "unknown"
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);

        TableModel tmodel = getModel();
        //get id
        String id = vmodel.baselineMap[rowIndex].id
        if (realColumnIndex == 2) { //score
            //get doc in baseline
            def curdoc = vmodel.baselineMap.find { it.id == id }
            if (curdoc) {
                tip = "${curdoc.explain}"
            }
        } else {
            //get doc in current
            def curdoc = vmodel.currentMap.find { it.id == id }
            if (curdoc) {
                tip = "Current: Pos: ${curdoc.pos} Score:${curdoc.score}"
            }
        }
        return tip;
    }

    public JToolTip createToolTip() {
        MultiLineToolTip tip = new MultiLineToolTip();
        tip.setComponent(this);
        return tip;
    }
}
