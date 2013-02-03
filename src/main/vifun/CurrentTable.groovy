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
        //  Color row based on a cell value
        if (!isRowSelected(row)) {
            int halfRows = (griffon.util.ApplicationHolder.application.models.vifun.rows as Integer)/2
            c.setBackground(getBackground());
            int modelRow = convertRowIndexToModel(row);
            if (getModel().getValueAt(modelRow, 1)) {
                String deltapos = (String) getModel().getValueAt(modelRow, 1)
                if ('-'.equals(deltapos)) {
                    c.setBackground(DARK_GREEN)
                } else {
                    switch (deltapos as Integer) {
                        case 1..halfRows:
                            c.setBackground(LIGHT_GREEN); break
                        case halfRows+1..Integer.MAX_VALUE:
                            c.setBackground(DARK_GREEN); break
                        case Integer.MIN_VALUE+1..-halfRows:
                            c.setBackground(DARK_RED); break
                        case -halfRows+1..-1:
                            c.setBackground(LIGHT_RED); break
                        default:
                            c.setBackground(Color.LIGHT_GRAY); break
                    }
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
