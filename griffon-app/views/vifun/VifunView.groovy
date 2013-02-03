package vifun
import net.miginfocom.swing.MigLayout
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.*
import java.awt.event.FocusEvent;


application(title: 'vifun',
  preferredSize: [1280, 900],
  pack: true,
  locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]) {
    panel(border:emptyBorder(1), layout:new MigLayout('fill')) {
        panel(border:lineBorder(color:Color.BLACK), name:'qPanel', layout:new MigLayout('fill'), constraints: "west, width 150:400:450") {
            panel(name:'buttonPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
                label 'Solr URL:'
                textField text: bind('solrurl', source: model, mutual: true),columns: 25, editable:true
    //                    focusLost:{FocusEvent e -> controller.showHandlers()}
                button ("Show Handlers", actionPerformed:controller.showHandlers, constraints: "wrap")
                label 'Handler', visible: bind{model.enabledHandlerText}
                comboBox(id:'handlersCombo', visible: bind{model.enabledHandlerText}, actionPerformed: controller.selectHander, selectedItem: bind(target:model, targetProperty:'handler'), constraints: "wrap")
                //comboBox(id:'handlersCombo', model: eventComboBoxModel(source: model.handlerList), actionPerformed: controller.selectHander, selectedItem: bind(target:model, targetProperty:'handler'), constraints: "wrap")
                scrollPane (constraints: "growx, growy, width 200:350:400, height 200:350:400, span 5", visible: bind{model.enabledHandlerText}) {
                    textArea(text: bind('handlerText', source: model, mutual: true), constraints: "span 8")
                }        
            }
            panel(name:'fPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
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
                button ("Run Query" , enabled: bind{model.enabledQuery}, name: 'runQuery', actionPerformed: controller.runQuery)
            }

            //boost values
            //panel(border:lineBorder(color:Color.BLACK),name:'boostPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
            panel(name:'boostPanel', layout:new MigLayout('fill'), constraints: "span, wrap") {
                    label 'Scoring:'
                    ltweak = label 'Select a number as target to tweak', visible: bind{model.enabledBind}, constraints: "wrap, span 3"
                    ltweak.setFont(new Font("Serif", Font.BOLD, 14))
                    ltweak.setForeground(Color.BLUE)

                    lqf = label 'qf:'
                    qf = textField name: 'qf', text: bind('qf', source: model, mutual: true), columns: 70, constraints: "wrap, span 3"
                    lpf = label 'pf:'
                    pf = textField name: 'pf', text: bind('pf', source: model, mutual: true), columns: 70, constraints: "wrap, span 3"
                    lbf = label 'bf:'
                    bf = textField name: 'bf', text: bind('bf', source: model, mutual: true), columns: 70, constraints: "wrap, span 3"
                    lmm = label 'mm:'
                    mm = textField name: 'mm', text: bind('mm', source: model, mutual: true), columns: 70, constraints: "wrap, span 3"
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
        panel(border:lineBorder(color:Color.BLACK), name:'resPanel', layout:new MigLayout('fill'), constraints: "east, growx, growy, width 500:870:1000, gapy 0:0:0, gapx 0:0:0") {
            //button ("Save Baseline", constraints: "south, width 150:150:150", enabled: bind{model.enabledTake}, actionPerformed: controller.takeBaselineSnapshot)
            panel(layout:new MigLayout('top, fill, flowy', 'nogrid'), visible: bind{model.baselineMap!=null}, constraints: "growx, growy, width 200:440:550") {
                label 'Current Result'
                scrollPane (constraints: "growx, growy") {
                    table( new CurrentTable(), id: 'ctable') {
                        tableFormat = defaultTableFormat(columns: model.columns)
                        eventTableModel(source: model.ctable, format: tableFormat)
                        installTableComparatorChooser(source: model.ctable)
                    }
                }
                currentParam = textArea(text: bind('currentParam', source: model, mutual: true), constraints: "growx", visible: bind{model.enabledCurrentParam})
            }
            panel(layout:new MigLayout('top, fill, flowy', 'nogrid'), visible: bind{model.baselineMap!=null}, constraints: "growx, growy, gapy 0:0:0, gapx 0:0:0, width 200:350:400") {
                label 'Baseline Result'
                scrollPane (constraints: "growx, growy, gapy 0:0:0, gapx 0:0:0") {
                    table( new BaselineTable(), id: 'btable') {
                        tableFormat = defaultTableFormat(columns: model.columnsbaseline)
                        eventTableModel(source: model.btable, format: tableFormat)
                        installTableComparatorChooser(source: model.btable)
                    }
                }
                baselineParam = textArea(text: bind('baselineParam', source: model, mutual: true),constraints: "growx", visible: bind{model.enabledBaselineParam})
            }
            ctable.columnModel.getColumn(0).setPreferredWidth(20)
            ctable.columnModel.getColumn(1).setPreferredWidth(25)
            ctable.columnModel.getColumn(2).setPreferredWidth(220)
            ctable.columnModel.getColumn(3).setPreferredWidth(60)
            ctable.columnModel.getColumn(4).setPreferredWidth(60)
            btable.columnModel.getColumn(0).setPreferredWidth(20)
            btable.columnModel.getColumn(1).setPreferredWidth(220)
            btable.columnModel.getColumn(2).setPreferredWidth(60)
        }
    }
}

