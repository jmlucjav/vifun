package vifun

import javax.swing.JTable
import javax.swing.JToolTip
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent

public class BaselineTable extends JTable {
    def vmodel = griffon.util.ApplicationHolder.application.models.vifun

    public BaselineTable(){
        super()
    }

    //http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#celltooltip
    public String getToolTipText(MouseEvent e) {
        String tip = "unknown"
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int colRow = convertColumnIndexToModel(colIndex);
        int modelRow = convertRowIndexToModel(rowIndex);

        TableModel tmodel = getModel();
        //get id
        String id = vmodel.baselineMap[rowIndex].id
        switch (colRow){
            case 0:
                //get doc in current
                def curdoc = vmodel.currentMap.find { it.id == id }
                if (curdoc) {
                    tip = "Current: Pos: ${curdoc.pos} Score:${curdoc.score}"
                }
                break
            case 1:
                String val = tmodel.getValueAt(modelRow, colRow)
                if (val) {
                    tip = val
                }
                break
            case 2:
                //get doc in baseline
                def curdoc = vmodel.baselineMap.find { it.id == id }
                if (curdoc) {
                    tip = "${curdoc.explain}"
                }
                break
        }
        return tip;
    }

    public JToolTip createToolTip() {
        MultiLineToolTip tip = new MultiLineToolTip();
        tip.setComponent(this);
        return tip;
    }
}
