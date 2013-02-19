package vifun
import net.miginfocom.swing.MigLayout
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.*
import java.awt.event.FocusEvent;
import ca.odell.glazedlists.swing.*
import ca.odell.glazedlists.gui.*
import ca.odell.glazedlists.*


application(title: 'vifun',
  preferredSize: [1480, 900],
  pack: true,
  locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]) {
    panel = panel(border:emptyBorder(1), layout:new MigLayout('fill')) {
        qpanel = panel(border:lineBorder(color:Color.BLACK), name:'qPanel', layout:new MigLayout('fill'), constraints: "west, width 350:400:400") {
            panel(name:'buttonPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
                label 'Solr URL:'
                textField text: bind('solrurl', source: model, mutual: true),columns: 25, editable:true
    //                    focusLost:{FocusEvent e -> controller.showHandlers()}
                button ("Show Handlers", actionPerformed:controller.showHandlers, constraints: "wrap")
                label 'Handler', visible: bind{model.enabledHandlerText}
                comboBox(id:'handlersCombo', visible: bind{model.enabledHandlerText}, actionPerformed: controller.selectHander, selectedItem: bind(target:model, targetProperty:'handler'), constraints: "wrap")
                //comboBox(id:'handlersCombo', model: eventComboBoxModel(source: model.handlerList), actionPerformed: controller.selectHander, selectedItem: bind(target:model, targetProperty:'handler'), constraints: "wrap")
                scrollPane (constraints: "growx, growy, width 200:350:400, height 100:200:250, span 5", visible: bind{model.enabledHandlerText}) {
                    textArea(text: bind('handlerText', source: model, mutual: true), constraints: "span 8", editable:false)
                }        
            }
            ppanel = panel(name:'pPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
                label 'Query params:', constraints: "wrap, span 3"
                label 'q:'
                textField text: bind('q', source: model, mutual: true), columns: 30, constraints: "wrap, span 3"
                label 'rows:'
                textField text: bind('rows', source: model, mutual: true), columns: 6, constraints: "wrap, span 3"
                label 'fl:'
                textField text: bind('fl', source: model, mutual: true), columns: 30, constraints: "wrap, span 3"
                label 'rest:' 
                textField text: bind('rest', source: model, mutual: true), columns: 30, constraints: "wrap, span 3"
                label '(i.e sfield=store&pt=45.15,-93.85)', constraints: "wrap, span 3"
                button ("Run Query" , enabled: bind{model.enabledQuery}, name: 'runQuery', actionPerformed: controller.runQuery, constraints: "wrap")
                //errMsgP = scrollPane (constraints: "growx, growy, width 0:0:400, height 0:0:100, spanx 2, spany 2", visible: bind{model.enabledErrMsg}) {
                errMsgP = scrollPane (constraints: "growx, growy, width 200:350:400, height 50:100:100, spanx 2, spany 2, hidemode 2", visible: bind{model.enabledErrMsg}) {
                    errMsg = textArea(text: bind('errMsg', source: model, mutual: true), constraints: "growx, hidemode 2", visible: bind{model.enabledErrMsg}, rows:2, columns: 25, editable:false)
                    errMsg.setForeground(Color.RED)
                }    
            }

            //boost values
            //panel(border:lineBorder(color:Color.BLACK),name:'boostPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
            fpanel = panel(name:'boostPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
                    label 'Scoring:'
                    ltweak = label 'Select a number as target to tweak', visible: bind{model.enabledBind}, constraints: "wrap, span 3"
                    ltweak.setFont(new Font("Serif", Font.BOLD, 14))
                    ltweak.setForeground(Color.BLUE)

                    lqf = label 'qf:', constraints: "hidemode 2", visible: bind{model.enabledqf};
                    qf = textField name: 'qf', text: bind('qf', source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledqf};
                    lpf = label 'pf:', constraints: "hidemode 2", visible: bind{model.enabledpf};
                    pf = textField name: 'pf', text: bind('pf', source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledpf};
                    lbf_0 = label 'bf:', constraints: "hidemode 2", visible: bind{model.enabledbf_0}; bf_0 = textField name: "bf_0", text: bind("bf_0", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbf_0}
                    lbf_1 = label 'bf:', constraints: "hidemode 2", visible: bind{model.enabledbf_1}; bf_1 = textField name: "bf_1", text: bind("bf_1", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbf_1}
                    lbf_2 = label 'bf:', constraints: "hidemode 2", visible: bind{model.enabledbf_2}; bf_2 = textField name: "bf_2", text: bind("bf_2", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbf_2}
                    lbq_0 = label 'bq:', constraints: "hidemode 2", visible: bind{model.enabledbq_0}; bq_0 = textField name: "bq_0", text: bind("bq_0", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbq_0}
                    lbq_1 = label 'bq:', constraints: "hidemode 2", visible: bind{model.enabledbq_1}; bq_1 = textField name: "bq_1", text: bind("bq_1", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbq_1}
                    lbq_2 = label 'bq:', constraints: "hidemode 2", visible: bind{model.enabledbq_2}; bq_2 = textField name: "bq_2", text: bind("bq_2", source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledbq_2}
                    lboost = label 'boost:', constraints: "hidemode 2", visible: bind{model.enabledboost};
                    boost = textField name: 'boost', text: bind('boost', source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledboost};
                    lmm = label 'mm:', constraints: "hidemode 2", visible: bind{model.enabledmm};
                    mm = textField name: 'mm', text: bind('mm', source: model, mutual: true), columns: 70, constraints: "wrap, span 3, hidemode 2", visible: bind{model.enabledmm};
                    //ltie = label 'tie:'
                    //tie = textField name: 'tie', text: bind('tie', source: model, mutual: true), columns: 70, constraints: "wrap, span 3"
                sl = slider(id:'sl',constraints: "wrap, span", enabled: bind{model.enabledSlider}, value: bind(target: model, targetProperty:'tweakedFValueNew', validator: {!sl.valueIsAdjusting}))
                sl.setPaintTicks(true)
                sl.setPaintLabels(true)
                sl.setPreferredSize(new Dimension(500,20))
                label(text:bind(source:sl, sourceProperty:'value'), visible: bind{model.enabledSlider}, border:lineBorder(color:Color.GREEN))
                button ("Set current value", enabled: bind{model.enabledSlider},name: 'setValueSlider', actionPerformed: controller.setValue, constraints: "left") 
                button ("Increase limits", enabled: bind{model.enabledSlider},name: 'largerSlider', actionPerformed: controller.largerSlider, constraints: "left") 

            }
        }
        panel(border:lineBorder(color:Color.BLACK), name:'resPanel', layout:new MigLayout('fill'), constraints: "east, growx, growy, width 500:1200:1600, gapy 0:0:0, gapx 0:0:0") {
            //button ("Save Baseline", constraints: "south, width 150:150:150", enabled: bind{model.enabledTake}, actionPerformed: controller.takeBaselineSnapshot)
            panel(layout:new MigLayout('top, fill, flowy', 'nogrid'), visible: bind{model.baselineMap!=null}, constraints: "growx, growy, width 200:740:1050") {
                label 'Current Result'
                scrollPane (constraints: "growx, growy") {
                    ctable = table( new CurrentTable(), id: 'ctable') {
                        tableFormat = defaultTableFormat(columns: model.columns)
                        eventTableModel(source: model.ctable, format: tableFormat)
                        //installTableComparatorChooser(source: model.ctable)
                    }
                    //setup custom comparator where needed (delta cols)
                    TableComparatorChooser tableSorter = TableComparatorChooser.install(ctable, model.ctable, AbstractTableComparatorChooser.SINGLE_COLUMN);
                    tableSorter.getComparatorsForColumn(1).clear()
                    tableSorter.getComparatorsForColumn(1).add(model.deltaPosComparator)
                    tableSorter.getComparatorsForColumn(4).clear()
                    tableSorter.getComparatorsForColumn(4).add(model.deltaScoreComparator)
                }
                currentParam = textArea(text: bind('currentParam', source: model, mutual: true), constraints: "growx", visible: bind{model.enabledCurrentParam}, editable:false)
            }
            panel(layout:new MigLayout('top, fill, flowy', 'nogrid'), visible: bind{model.baselineMap!=null}, constraints: "growx, growy, gapy 0:0:0, gapx 0:0:0, width 200:500:700") {
                label 'Baseline Result'
                scrollPane (constraints: "growx, growy, gapy 0:0:0, gapx 0:0:0") {
                    table( new BaselineTable(), id: 'btable') {
                        tableFormat = defaultTableFormat(columns: model.columnsbaseline)
                        eventTableModel(source: model.btable, format: tableFormat)
                        installTableComparatorChooser(source: model.btable)
                    }
                }
                baselineParam = textArea(text: bind('baselineParam', source: model, mutual: true),constraints: "growx", visible: bind{model.enabledBaselineParam}, editable:false)
            }
            ctable.columnModel.getColumn(0).setPreferredWidth(20)
            ctable.columnModel.getColumn(1).setPreferredWidth(20)
            ctable.columnModel.getColumn(2).setPreferredWidth(280)
            ctable.columnModel.getColumn(3).setPreferredWidth(60)
            ctable.columnModel.getColumn(4).setPreferredWidth(60)
            btable.columnModel.getColumn(0).setPreferredWidth(20)
            btable.columnModel.getColumn(1).setPreferredWidth(280)
            btable.columnModel.getColumn(2).setPreferredWidth(60)
        }
    }
}

