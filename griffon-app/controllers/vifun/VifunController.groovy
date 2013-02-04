package vifun

import name.fraser.neil.plaintext.diff_match_patch
import org.fusesource.jansi.Ansi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.JLabel
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import java.awt.Color
import java.awt.Font
import java.lang.invoke.MethodHandles

import java.awt.event.ActionEvent
import java.awt.event.FocusListener
import java.beans.PropertyChangeListener

class VifunController {
    // these will be injected by Griffon
    def model
    def view
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    GroovyShell shell = new GroovyShell()

    void mvcGroupInit(Map args) {
        model.propertyChange = {
            if (it.propertyName == 'tweakedFValueNew') {
                doLater{
                    runQueryAndCompare()
                }
            }
            if (model.qset.contains(it.propertyName)||it.propertyName == 'rest') {
                invalidateBaseline()
            }
            //enablers
            if(!it.propertyName.startsWith('enabled')) {
                model.enabledQuery = model.handler && model.q && model.rows?.isNumber()&& !model.baselineMap
                //model.enabledTake = model.handler && model.currentMap && !model.baselineMap
                model.enabledBind = model.handler && model.baselineMap && model.currentMap
                model.enabledSlider = model.handler && model.baselineMap && model.currentMap && model.tweakedFName
                model.enabledHandlerText = model.handler!=null
                model.enabledCurrentParam = model.currentMap!=null
                model.enabledBaselineParam = model.baselineMap!=null
            }
        }
        //add listeners for target selection
        for (it in model.fset){
            view."$it".addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent arg0) {
                    int dot = arg0.getDot();
                    int mark = arg0.getMark();
                    if (dot != mark) {
                        arg0.source.setSelectionColor(Color.BLUE)
                        def sel = arg0.source.getSelectedText()
                        def text = arg0.source.getText()
                        //if not number of not whole number reject
                        if (!sel.isNumber()) return
                        if (mark>0 && text[mark-1].isNumber()) return
                        if (dot<text.size() && text[dot].isNumber()) return
                        selectTarget(arg0.source.name, arg0.source.text, sel.trim(), arg0.source)
                    }
                }
            });
        }
    }

    def selectTarget(String fname, String ftext, String fsel, source){
        edt {
            if (!model.enabledBind) return
            model.tweakedFName = fname
            model.tweakedFFormula = ftext
            model.tweakedFValue = fsel
            source.setSelectionColor(Color.GREEN)
//            resetFFieldsLabels()
            //view.lqf.foreground = view.lbf.foreground = view.lpf.foreground = view.lmm.foreground = view.ltie.foreground = Color.BLACK
            view.lqf.foreground = view.lbf.foreground = view.lpf.foreground = view.lmm.foreground = Color.BLACK
            model.fset.each{view."l$it".text = "$it"}
            //mark param in ui (as selection is lost when focus changes)
            view."l$fname".foreground = Color.RED
            view."l$fname".text = "$fname ($fsel)"
            //init slider values too
            def selasint = fsel.toFloat().toInteger()
            if (selasint>0){
                view.sl.minimum = 0
                view.sl.maximum = 10 * selasint
                view.sl.majorTickSpacing = selasint
            }else if (selasint==0){
                view.sl.minimum = -10
                view.sl.maximum = 10 
                view.sl.majorTickSpacing = 2
            }else{
                view.sl.minimum = 10 * selasint
                view.sl.maximum = 0
                view.sl.majorTickSpacing = -selasint
            }
            view.sl.value = selasint
            view.sl.setLabelTable(setSliderLabels());
            //Hashtable labelTable = new Hashtable();
            //labelTable.put( new Integer( 0 ), new JLabel("0") );
            //labelTable.put( new Integer(view.sl.minimum), new JLabel(view.sl.minimum as String) );
            //labelTable.put( new Integer(view.sl.maximum), new JLabel(view.sl.maximum as String) );
            //view.sl.setLabelTable(labelTable);
            view.sl.setPaintLabels(true);
        }
    }
    //using this method is not enought, somthing to do with edt etc
    def setSliderLabels(){
        //customize labels
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 0 ), new JLabel("0") );
        labelTable.put( new Integer(view.sl.minimum), new JLabel(view.sl.minimum as String) );
        labelTable.put( new Integer(view.sl.maximum), new JLabel(view.sl.maximum as String) );
        return labelTable
    }
    def largerSlider={
        view.sl.minimum *= 10
        view.sl.maximum *= 10
        view.sl.majorTickSpacing = (view.sl.maximum-view.sl.minimum)/2
        view.sl.setLabelTable(setSliderLabels());
        //Hashtable labelTable = new Hashtable();
        //labelTable.put( new Integer( 0 ), new JLabel("0") );
        //labelTable.put( new Integer(view.sl.minimum), new JLabel(view.sl.minimum as String) );
        //labelTable.put( new Integer(view.sl.maximum), new JLabel(view.sl.maximum as String) );
        //view.sl.setLabelTable(labelTable);
        view.sl.setPaintLabels(true);
    }

    def resetFFieldsLabels(){
        view.lqf.foreground = view.lbf.foreground = view.lpf.foreground = view.lmm.foreground = Color.BLACK
        model.fset.each{view."l$it".text = "$it"}
    }

    def selectHander = {
       doLater  {
            log.debug "${model.handler} selected"
            model.handlerm = model.handlers[model.handler]
            //reset 
            model.fset.each{ model."$it" = ''}
            model.rest=''
            model.qset.each{ model."$it" = ''}
            invalidateBaseline()
            def t= new StringBuffer("DEFAULTS---------------------\n")
            model.handlers[model.handler]['defaults'].each{
                if (model.qset.contains(it.key)) { model."${it.key}" = it.value}
                if (model.fset.contains(it.key)) { model."${it.key}" = it.value}
                t << it.key + ':' + it.value + '\n'
            }
            t << 'APPENDS------------------\n'
            model.handlers[model.handler]['appends'].each{
                t << it.key + ':' + it.value + '\n'
            }
            model.handlerText = t.toString()
        }
    }


    //this should operate on model.handlerList, not view.handlersCombo
    def showHandlers = { evt = null ->
        doLater { ot ->
            view.handlersCombo.clear()
            model.solr.init(model.solrurl)
            String orightml = model.solr.getSolrconfig()
            def tbl = new XmlSlurper().parseText(orightml)
            def qhandlers = tbl.requestHandler.findAll { it.@class == 'solr.SearchHandler' }
            qhandlers.each { it ->
                view.handlersCombo.addItem it.@name
                //we need to explicitely get toString otherwise the object is used
                def n = (it.@name).toString()
                def m = model.handlers
                model.handlers.putAt(n,[:])
                model.handlers[n]['defaults']=[:]
                model.handlers[n]['appends']=[:]
                //no idea why these f calls dont work, closures work though.
                addHandlerParams(model.handlers[n], it, 'defaults')
                addHandlerParams(model.handlers[n], it, 'appends')
                def p = it.lst.find{it.@name=='defaults'}.children()
                p.each { pi ->
                    model.handlers[n]['defaults'][(pi.@name).toString()]=pi.text()
                }
                p = it.lst.find{it.@name=='appends'}.children()
                p.each { pi ->
                    model.handlers[n]['appends'][(pi.@name).toString()]=pi.text()
                }
            }
            invalidateBaseline()
        }
    }

    def runQuery={
        runQuery()
    }

    def takeBaselineSnapshot={
        model.baselineMap = model.currentMap
        model.btable.addAll(model.ctable)
        model.baselineParam = model.currentParam
        view.baselineParam.toolTipText = view.currentParam.toolTipText
    }
    def setValue={
        model."${model.tweakedFName}" = model.tweakedFFormula.replace(model.tweakedFValue, model.tweakedFValueNew)
    }
    def addHandlerParams(Map handl, it, String key){
        if (!it) return
        def p = it.lst.find{it.@name==key}.children()
        p.each { pi ->
            log.debug "\t${pi.@name}: ${pi.text()}"
            handl[key][(pi.@name).toString]=pi.text()
        }
    }

    def runQuery(boolean tweaking){
        //build params and search
        def params = model.solr.defineQueryParams(model, model.handlerm['defaults'], tweaking)
        def result = model.solr.search(params)
        doLater{
            model.currentMap = result
            log.debug "Results: ${model.q} ${result}"
            model.ctable.clear()
            result.each { d ->
                def line = ''
                def fstring = ''
                d.solrDocument.each{
                    if (!it.key.equals(model.solr.idfield) && !it.key.equals('score')){
                        fstring += (fstring ? '|':'')+it.value
                    }
                }
                String bdpos
                String bdscore
                if (tweaking){
                    //find how the doc did in baseline
                    def bd = model.baselineMap.find{it.id==d.id}
                    bdpos = bd ? bd.pos.toInteger()-d.pos.toInteger() : '-'
                    bdscore = bd ? bd.score.toFloat()-d.score.toFloat() : '-'
                    line += "${d.pos}($bdpos) ${fstring}: ${d.score}($bdscore)\n"
                }else{
                    line += "${d.pos} ${fstring}: ${d.score}\n"
                }
                model.ctable.add(['pos':d.pos,'posdelta':bdpos, 'docfields':fstring, 'score':d.score, 'scoredelta':bdscore])
            }
            model.currentParam = ""
            view.currentParam.toolTipText = "<html>"
            Iterator<String> iterator = params.getParameterNamesIterator();
            while (iterator.hasNext()) {
                String k = iterator.next()
                if (model.fset.contains(k)){
                    if (!params.get(k).equals(model.handlers[model.handler]['defaults'][k])){
                        model.currentParam += "+++ "
                    }
                    model.currentParam += "${k}:${params.get(k)}\n"
                }
                view.currentParam.toolTipText += "${k}:${params.get(k)}<br>"
            }
            view.currentParam.toolTipText += "</html>"
            //make automatic baseline
            if (!tweaking){
                takeBaselineSnapshot()
            }
        }
    }
    def runQuery(){
        runQuery(false)
    }
    def runQueryAndCompare(){
        runQuery(true)
    }
    def invalidateBaseline(){
        doLater{
       model.currentMap = null
        model.ctable.clear()
        model.currentParam = ''
        view.currentParam.toolTipText = ''
       model.baselineMap = null
        model.btable.clear()
        model.baselineParam = ''
        view.baselineParam.toolTipText = ''
        model.tweakedFName = ''
        model.tweakedFFormula = ''
        model.tweakedFValue = ''
        resetFFieldsLabels()
            }
    }
}
