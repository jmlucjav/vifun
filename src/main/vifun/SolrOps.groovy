package vifun

import org.slf4j.*
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.common.params.*
import org.apache.solr.client.solrj.response.*
import com.google.common.collect.*

class SolrOps {
    private static final Logger log = LoggerFactory.getLogger(SolrOps)

    def server
    String url = "http://localhost:8983/solr/core0"
    //use this instaed of hardcoded "id"
    String idfield

    def prelev = []
    def handlers = [:]

    def init(solrurl) {
        this.url = solrurl
        this.server = new HttpSolrServer(url)
        //get id field
        String orightml = getSolrschema()
        def tbl = new XmlSlurper().parseText(orightml)
        def idelem = tbl.uniqueKey
        idfield = idelem.text()
    }

    def getSolrschema(){
        return new URL(url+"/admin/file?file=schema.xml&contentType=text/xml;charset=utf-8")?.text
    }

    def getSolrconfig(){
        return new URL(url+"/admin/file?file=solrconfig.xml&contentType=text/xml;charset=utf-8")?.text
    }

    //jm return map with keys: id, pos, score, fields (add more if neccessary)
    def search(params) {
        //log.info "Search Params: ${params.join('|')}"
        QueryResponse rsp = getServer().query(params);
        def results = []
        int i = 0
        def res = rsp.getResults()
        res.each { doc ->
            def map = [:]
            map.pos = i++
            map.id = doc.getFieldValue(idfield)
            map.score = doc.get("score")
            map.explain = formatExplain(rsp.explainMap.get(map.id))
            def fmap = [:]
            doc.getFieldNames().each { it ->
                fmap."${it}" = doc.getFieldValue(it)
            }
            map.fields = fmap
            // Add the SolrDocument to the map as well
            // http://lucene.apache.org/solr/api/org/apache/solr/common/SolrDocument.html
            map.solrDocument = doc
            results << map
        }
        return results
    }

    // move to prev line this: ), product of:
    def formatExplain(String ex){
        def list = ex.readLines()
        for (i in 0..list.size()-1){
            if ('), product of:'.equals(list[i])){
                list[i-1]+=list[i]
                list[i]=''
            }
        }
        //another pass to write ruler
        def vertlines = []
        list.eachWithIndex() { obj, i ->
            def oneline = formatExplainLine(i, obj, vertlines)
            list[i] = oneline
        }
        return list.collect{it}.findAll{it}.join('\n')
    }

    def formatExplainLine(int k, String ex, vertlines){
        def LINK = '|'
        if (!k || !ex || !ex.trim()) return ex
        StringBuffer r = new StringBuffer(ex)
        def space =0
        while (' '.equals(ex[space]) && space<ex.size()){
            if (vertlines.contains(space)){
                r.replace(space, space+1, LINK)
            }
            space++
        }
        if (space>2 && space<ex.size()){
            r.replace(space-1, space, LINK)
            vertlines << space-1
        }
        return r.toString()
    }

    //take prev and cur result list and display curr+changes
    def compareResults(cur, prev) {
        cur.each { d ->
            def p = prev ? prev.find { e -> e.id == d.id } : null
            log.debug "\t${d.id}: ${d.pos} (${d.score}) ${d.fields.name} --- " + p?.pos
        }
    }

    def getServer() {
        return server
    }

    def defineQueryParams(VifunModel model, ListMultimap defp, boolean tweaking){
        ModifiableSolrParams params = new ModifiableSolrParams();
        for (String key : defp.keySet()) {
            def v = defp.get(key)
            v.each{ onev ->
                if (!model.qset.contains(key) && !model.fset.contains(key) && !model.fmultiple.contains(key)){
                    //skip facet, spellcheck, mlt, hl they are useless and slow thigns down
                    if (model.fignore.intersect(key.tokenize('.')).isEmpty()){
                        params.add(key, onev)
                    }
                }
            }
        }
        model.fset.each{
            if (model."$it"){
                def pval = model."$it" 
                // if tweaking change selected f
                if (tweaking && it.equals(model.tweakedFName)){
                    //take care just to replace the selected instance of that string!
                    pval = model.tweakedFFormula.substring(0, model.tweakedFValuePos) + model.tweakedFFormula.substring(model.tweakedFValuePos).replaceFirst(model.tweakedFValue, model.tweakedFValueNew)
                }
                if (it.contains('_')){
                    params.add(it.substring(0,it.indexOf('_')), pval);
                }else {
                    params.set(it, pval);
                }
            }
        }
        model.qset.each{
            if ("rows".equals(it)){
                if (tweaking){
                    params.set(it, model."$it");
                }else{
                    //overask rows
                    int nbrows = griffon.util.ApplicationHolder.application.config.vifun.baseline.rows.multiplier * (model."$it" as Integer)
                    params.set(it, nbrows);
                }
            }else if (model."$it"){
                params.set(it, model."$it");
            }
        }
        // fl special case, add id,score to fields
        params.set("fl", "${model.fl},${idfield},score");
        // rest
        if (model.rest){
            def rest = model.rest.tokenize('&')
            rest.each{
                params.set(it.substring(0, it.indexOf('=')), it.substring(it.indexOf('=')+1))
            }
        }
        // needed by vifun
        params.set("wt", "javabin");
        params.set("debugQuery", "true");
        params.set(CommonParams.QT, griffon.util.ApplicationHolder.application.config.vifun.handler);
        return params
    }

}
