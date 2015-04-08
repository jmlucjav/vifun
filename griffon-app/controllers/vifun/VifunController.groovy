package vifun

import org.apache.log4j.Logger;
import com.google.common.collect.*

import javax.swing.JLabel
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import java.awt.Color
import java.awt.Font
import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.FocusListener
import java.beans.PropertyChangeListener
import groovy.swing.*
import org.apache.commons.lang3.*

class VifunController {
    // these will be injected by Griffon
    def model
    def view

    GroovyShell shell = new GroovyShell()

    void mvcGroupInit(Map args) {
        model.propertyChange = {
            if (it.propertyName == 'tweakedFValueNewTemp') {
                //we need the float value from the slider
                model.tweakedFValueNew = view.sl.getFloatValue()
                doLater {
                    runQueryAndCompare()
                }
            }
            if (model.qset.contains(it.propertyName) || it.propertyName == 'rest') {
                invalidateBaseline()
            }
            //enablers
            if (!it.propertyName.startsWith('enabled')) {
                model.enabledQuery = model.handler && model.q && model.rows?.isNumber() && !model.baselineMap
                //model.enabledTake = model.handler && model.currentMap && !model.baselineMap
                model.enabledBind = model.handler && model.baselineMap && model.currentMap
                model.enabledSlider = model.handler && model.baselineMap && model.currentMap && model.tweakedFName
                model.enabledHandlerText = model.handler != null
                model.enabledCurrentParam = model.currentMap != null
                model.enabledBaselineParam = model.baselineMap != null
                model.enabledErrMsg = model.errMsg && true
                model.enabledCompare = model.bseldoc && model.cseldoc
                model.fset.each { 
                    model."enabled$it" = model."$it" && true
                }
            }
            if (it.propertyName == 'handlerText') {
                //init textboxes etc with usual values for testing if current user is me and in dev
                //I could not get it to work from the beggining (select handler etc, so at least do it here)
                if (Environment.current==Environment.DEVELOPMENT && System.getenv()['USERNAME'].equals('jm') && '/suggesti'.equals(model.handler)) {
                    model.q = 'san pal'
                    model.rows = '80'
                    model.rest = 'pt=45,-93'
                    view.brun.requestFocus()
                }
                if (Environment.current==Environment.DEVELOPMENT && System.getenv()['USERNAME'].equals('jm') && model.handlers.containsKey('/browsePaying') && '/browse'.equals(model.handler)) {
                    model.q = 'paris'    
                    model.rows = '20'    
                    view.brun.requestFocus()
                }
            }
        }
        //add listeners for target selection
        for (it in model.fset) {
            view."$it".addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent arg0) {
                    int dot = arg0.getDot();
                    int mark = arg0.getMark();
                    if (dot < mark) {
                        //allow selecting from right to left tool
                        def temp = dot
                        dot = mark
                        mark = temp
                    }
                    if (dot != mark) {
                        arg0.source.setSelectionColor(Color.BLUE)
                        def sel = arg0.source.getSelectedText()
                        def text = arg0.source.getText()
                        //if not number of not whole number reject
                        if (!sel.isNumber()) return
                        if (mark > 0 && (text[mark - 1].isNumber() || text[mark - 1].equals('.'))) return
                        if (dot < text.size() && (text[dot].isNumber() || text[dot].equals('.'))) return
                        selectTarget(arg0.source.name, arg0.source.text, sel.trim(), arg0.source, mark)
                    }
                }
            });
        }
    }


    def selectTarget(String fname, String ftext, String fsel, source, mark) {
        edt {
            if (!model.enabledBind) return
            model.tweakedFName = fname
            model.tweakedFFormula = ftext
            model.tweakedFValue = fsel
            model.tweakedFValuePos = mark
            source.setSelectionColor(Color.GREEN)
//            resetFFieldsLabels()
            model.fset.each { view."l$it".foreground = Color.BLACK }
            model.fset.each { view."l$it".text = "$it" }
            //mark param in ui (as selection is lost when focus changes)
            view."l$fname".foreground = Color.RED
            view."l$fname".text = "$fname ($fsel)"
            //init slider values too
            def selasint = fsel.toFloat()
            def scale = 1
            if (fsel.contains('.')){
                scale = 100
            }
            view.sl.initValues(model.tweakedFName, selasint, scale)
        }
    }

    def largerSlider = { view.sl.increaseLimits() }

    def resetFFieldsLabels() {
        model.fset.each { view."l$it".foreground = Color.BLACK }
        model.fset.each { view."l$it".text = "$it" }
    }

    def selectHander = {
        doLater {
            log.debug "${model.handler} selected"
            model.handlerm = model.handlers[model.handler]
            //reset 
            model.fset.each { model."$it" = '' }
            model.fmultiple.each { model."f$it" = [:] }
            model.rest = ''
            model.qset.each { model."$it" = '' }
            invalidateBaseline()
            def t = new StringBuffer()
            ['defaults', 'appends', 'invariants'].each{
                t << "${it.toUpperCase()}---------------------\n"
                for (String key : model.handlers[model.handler][it].keySet()) {
                    def v = model.handlers[model.handler][it].get(key)
                    if (model.qset.contains(key)) {
                        model."${key}" = v[0]
                        t << key + ':' + v[0] + '\n'
                    }else if (model.fmultiple.contains(key)){
                        v.each{ onev ->
                            def index = model."f${key}".size() 
                            model."${key}_$index" = onev
                            model."f${key}"["${key}_$index"]=onev
                            t << key + ':' + onev + '\n'
                        }
                    }else if (model.fset.contains(key)) {
                        model."${key}" = v[0]
                        t << key + ':' + v[0] + '\n'
                    }else{
                        t << key + ':' + v[0] + '\n'
                    }
                }
            }
            //this does not work well, not all is displyed in the UI, not sure why
            //parseParams('defaults', t)
            //parseParams('appends', t)
            //parseParams('invariants', t)
            model.handlerText = t.toString()
        }
    }
    //def parseParams(String type, StringBuffer sb){
        //sb << "${type.toUpperCase()}---------------------\n"
        //for (String key : model.handlers[model.handler][type].keySet()) {
            //def v = model.handlers[model.handler][type].get(key)
            //if (model.qset.contains(key)) {
                //model."${key}" = v[0]
                //sb << key + ':' + v[0] + '\n'
            //}else if (model.fmultiple.contains(key)){
                //v.each{ onev ->
                    //def index = model."f${key}".size() 
                    //model."${key}_$index" = onev
                    //model."f${key}"["${key}_$index"]=onev
                    //sb << key + ':' + onev + '\n'
                //}
            //}else if (model.fset.contains(key)) {
                //model."${key}" = v[0]
                //sb << key + ':' + v[0] + '\n'
            //}else{
                //sb << key + ':' + v[0] + '\n'
            //}
        //}
    //}

    //this should operate on model.handlerList, not view.handlersCombo
    def showHandlers = { evt = null ->
        doLater { ot ->
            invalidateBaseline()
            //shameful but just want to get rid of that exception...
            try{view.handlersCombo.clear()}catch(e){}
            try{view.handlersCombo.clear()}catch(e){}
            try{
                model.solr.init(model.solrurl)
                String orightml = model.solr.getSolrconfig()
                def tbl = new XmlSlurper().parseText(orightml)
                def qhandlers = tbl.requestHandler.findAll { it.@class == 'solr.SearchHandler' }
                qhandlers.each { it ->
                    view.handlersCombo.addItem it.@name
                    //we need to explicitely get toString otherwise the object is used
                    def n = (it.@name).toString()
                    def m = model.handlers
                    model.handlers.putAt(n, [:])
                    ListMultimap<String, String> amm = ArrayListMultimap.create()
                    model.handlers[n]['all'] = amm
                    ['defaults', 'appends', 'invariants'].each{ pt ->
                        ListMultimap<String, String> dmm = ArrayListMultimap.create()
                        model.handlers[n][pt] = dmm
                        def p = it.lst.find { it.@name == pt }.children()
                        p.each { pi ->
                            model.handlers[n][pt].put((pi.@name).toString().replaceAll("\\s+", " ").trim(), pi.text().replaceAll("\\s+", " ").trim())
                        }
                        model.handlers[n]['all'].putAll(model.handlers[n][pt])
                    }
                }
            }catch(Throwable e){
                doLater{
                    def errstr ="Error: ${e.getMessage()}"
                    //showError(errstr) 
                    log.error errstr
                    model.errMsg = errstr
                    view.errMsgP.invalidate()
                    view.panel.validate()
                    javax.swing.SwingUtilities.getRoot(view.panel).pack()
                    view.panel.repaint()
                }
            }
        }
    }
    //not sure why, but if I use these methods instead of inlining, it hangs eventually
    //def showError(String errstr){
        //log.error errstr
        //model.errMsg = errstr
        //view.errMsgP.invalidate()
        //view.panel.validate()
        //javax.swing.SwingUtilities.getRoot(view.panel).pack()
        //view.panel.repaint()
    //}
    //def clearError(){
        //showError('')
    //}      
    def runQuery = {
        runQuery()
    }

    def takeBaselineSnapshot = {
        model.baselineMap = model.currentMap
        model.btable.addAll(model.ctable)
        model.baselineParam = model.currentParam
        view.baselineParam.toolTipText = view.currentParam.toolTipText
    }
    def setValue = {
        model."${model.tweakedFName}" = model.tweakedFFormula.replace(model.tweakedFValue, ""+view.sl.getFloatValue())
    }

    def toggleSynch = {
        if (model.synchScroll){
            view.cscr.getHorizontalScrollBar().setModel(view.bscr.getHorizontalScrollBar().getModel());
            view.cscr.getVerticalScrollBar().setModel(view.bscr.getVerticalScrollBar().getModel());
        }else{
            view.cscr.getHorizontalScrollBar().setModel(model.origCHorScroll);
            view.cscr.getVerticalScrollBar().setModel(model.origCVerScroll);
        }
    }

    def showCompare = {
        if (model.bseldoc && model.cseldoc){
            showExplainComparison(model.cseldoc.explain, model.bseldoc.explain, "${model.cseldoc.name} / ${model.bseldoc.name}")
        }
    }

    def showExplainComparison(explcur, explbas, String docdesc){
        def swingBuilder = new SwingBuilder()
        JEditorPane msg = new JEditorPane()
        createExplainCompText(explcur, explbas, msg)
        log.debug "$msg"
        JScrollPane scrollPane = new JScrollPane(msg);  
        Object[] oarr = []
        final JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null, oarr, null);
        final JDialog dialog = new JDialog();
        dialog.setTitle(docdesc)
        dialog.setModal(false)
        dialog.setContentPane(optionPane);
        dialog.pack()
        dialog.show()
    }

    def createExplainCompText(explcur, explbas, JEditorPane editorPane){
        editorPane.contentType = "text/html"
        //font must be set here instead of the html in order to work in a JEditorPane
        editorPane.setFont(new Font("Consolas", Font.PLAIN, 11))
        editorPane.editable = false
        //build the text
        def text =''
        def lbas = explbas.readLines()
        def lcur = explcur.readLines()
        int widest = lcur*.size().max()
        int nblines = Math.max(lbas.size(), lcur.size())
        for (i in 0..nblines-1){
            text += formatDiffLine(widest, i<lcur.size()?lcur[i]:"", i<lbas.size()?lbas[i]:"")
        }
        editorPane.text = "<html>$text</html>"
    }
    def String formatDiffLine(int widest, String c, String b){
        if (c.trim().equals(b.trim()) && c.trim().size()==0) return ""
        def col = 'black'
        if (!c.equals(b)) { col = 'red' }
        def size = "3"
        def cur = StringUtils.rightPad(c, widest, " ").replaceAll(' ', '&nbsp;')
        def bas = b.replaceAll(' ', '&nbsp;')
        def temp = /<font color="$col">$cur $bas<\/font><br\/>/
        return temp
    }

    def runQuery(boolean tweaking) {
        //clear error first
        model.errMsg = ''
        view.errMsgP.invalidate()
        view.panel.validate()
        javax.swing.SwingUtilities.getRoot(view.panel).pack()
        view.panel.repaint()
        model.maxScoreDiff = 0
        //build params and search
        def params
        def result
        try {
            params = model.solr.defineQueryParams(model, model.handlerm['all'], tweaking)
            result = model.solr.search(params)
        }catch(Exception e){
            doLater{
                def errstr ="Error: ${e.getMessage()}"
                //showError(errstr)
                log.error errstr
                model.errMsg = errstr
                view.errMsgP.invalidate()
                view.panel.validate()
                javax.swing.SwingUtilities.getRoot(view.panel).pack()
                view.panel.repaint()
            }
        }
        doLater {
            model.currentMap = result
            log.debug "Results: ${model.q} ${result}"
            model.ctable.clear()
            int nbresults = Math.min(result.size(), (model.rows as Integer))
            for (i in 0..nbresults-1){
                def d = result.getAt(i)
                def line = ''
                def fstring = ''
                if (!d){
                    log.error "NULL doc in result $i"
                    break
                }
                d.solrDocument.each {
                    if (!it.key.equals(model.solr.idfield) && !it.key.equals('score')) {
                        fstring += (fstring ? ' | ' : '') + it.value
                    }
                }
                d.name = fstring
                String bdpos
                String bdscore
                if (tweaking) {
                    //find how the doc did in baseline
                    def bd = model.baselineMap.find { it.id == d.id }
                    bdpos = bd ? bd.pos.toInteger() - d.pos.toInteger() : '+'
                    //format it to make sorted col easier to read
                    bdscore = bd ? String.format("%.5f", d.score.toFloat() - bd.score.toFloat()) : '+'
                    def absdscore = bd ? Math.abs(d.score.toFloat()-bd.score.toFloat()) : 0
                    model.maxScoreDiff = Math.max(model.maxScoreDiff,absdscore)
                    line += "${d.pos}($bdpos) ${fstring}: ${d.score}($bdscore)\n"
                } else {
                    line += "${d.pos} ${fstring}: ${d.score}\n"
                }
                model.ctable.add(['pos': d.pos, 'posdelta': bdpos, 'docfields': fstring, 'score': d.score, 'scoredelta': bdscore])
            }
            model.currentParam = ""
            view.currentParam.toolTipText = "<html>"
            view.foreground = Color.BLACK
            Iterator<String> iterator = params.getParameterNamesIterator();
            while (iterator.hasNext()) {
                String key = iterator.next()
                String[] v = params.getParams(key)
                v.each{ onev ->
                    if (model.fset.contains(key) || model.fmultiple.contains(key)){
                        def orig = model.handlers[model.handler]['all'].get(key)
                        if (!orig.contains(onev)) {
                            model.currentParam += "+++ "
                        }
                        model.currentParam += "${key}:${onev}\n"
                    }
                    view.currentParam.toolTipText += "${key}:${onev}<br>"
                }
            }
            view.currentParam.toolTipText += "</html>"
            //make automatic baseline
            if (!tweaking) {
                takeBaselineSnapshot()
            }
        }
    }

    def runQuery() {
        runQuery(false)
    }

    def runQueryAndCompare() {
        runQuery(true)
    }

    def invalidateBaseline() {
        doLater {
            model.bseldoc = model.cseldoc = null
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
            //clearError()
            model.errMsg = ''
            view.errMsgP.invalidate()
            view.panel.validate()
            javax.swing.SwingUtilities.getRoot(view.panel).pack()
            view.panel.repaint()
        }
    }
}
