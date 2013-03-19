package vifun

import javax.swing.JTable
import javax.swing.JToolTip
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import java.awt.*
import java.awt.event.*

public class CurrentTable extends JTable {
    //taken from http://grepcode.com/file/repo1.maven.org/maven2/net.sourceforge.jadex/jadex-tools-comanalyzer/2.2.1/jadex/tools/comanalyzer/ToolColor.java
    public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40)
    public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00)
    public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40)
    public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00)
    def vmodel = griffon.util.ApplicationHolder.application.models.vifun

    public CurrentTable(){
        super()
        addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              JTable target = (JTable)e.getSource();
              int row = target.getSelectedRow();
              int column = target.getSelectedColumn();
              int modelRow = convertRowIndexToModel(row);
              showScoringInfo(modelRow)
            }
          }
        })
    }

    private void showScoringInfo(int rowIndex){
        TableModel tmodel = getModel();
        String id = vmodel.currentMap[rowIndex].id
        String explcur = vmodel.currentMap[rowIndex].explain
        def basdoc = vmodel.baselineMap.find { it.id == id }
        String explbas = basdoc.explain
        griffon.util.ApplicationHolder.application.controllers.vifun.showExplainComparison(explcur, explbas, basdoc.name)
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setBackground(getBackground());
        //do this otherwise selected row is unreadable
        c.setForeground(Color.BLACK)
        //only on delta columns
        if (column !=1 && column != 4) {
            return c
        }
        //  Color row based on a cell value
        if (isRowSelected(row)) {
            c.setBackground(Color.BLUE)
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

    public String getToolTipText(MouseEvent e) {
        String tip = "unknown"
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int colRow = convertColumnIndexToModel(colIndex);
        int modelRow = convertRowIndexToModel(rowIndex);

        TableModel tmodel = getModel();
        //get id
        String id = vmodel.currentMap[rowIndex].id
        switch (colRow){
            case [0, 1]:
                //get doc in baseline
                def curdoc = vmodel.baselineMap.find { it.id == id }
                if (curdoc) {
                    tip = "Baseline: Pos: ${curdoc.pos} Score:${curdoc.score}"
                }
                break
            case [3,4]:
                def curdoc = vmodel.currentMap.find { it.id == id }
                if (curdoc) {
                    tip = "${curdoc.explain}"
                }
                break
            case 2:
                String val = tmodel.getValueAt(modelRow, colRow)
                if (val) {
                    tip = val
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
