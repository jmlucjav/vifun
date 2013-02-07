package vifun

import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.common.params.*
import org.apache.solr.client.solrj.*
import org.apache.solr.client.solrj.response.*
import org.apache.solr.common.SolrInputDocument
import groovy.util.slurpersupport.NodeChild
import groovy.xml.StreamingMarkupBuilder
import groovy.text.SimpleTemplateEngine

class SolrOps {

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
        QueryResponse rsp = getServer().query(params);
        def results = []
        int i = 0
        def res = rsp.getResults()
        res.each { doc ->
            def map = [:]
            map.pos = i++
            map.id = doc.getFieldValue(idfield)
            map.score = doc.get("score")
            map.explain = rsp.explainMap.get(map.id)
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

    def defineQueryParams(VifunModel model, Map defp, boolean tweaking){
        ModifiableSolrParams params = new ModifiableSolrParams();
        defp.each() { key, value ->
            params.add(key.toString(), value)
        }
        model.fset.each{
            if (model."$it"){
                params.set(it, model."$it");
            }
        }
        model.qset.each{
            if (model."$it"){
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
        // if tweaking change selected f
        if (tweaking){
           def newform = model.tweakedFFormula.replace(model.tweakedFValue, model.tweakedFValueNew)
           params.set(model.tweakedFName, newform);
        }
        return params
    }

}
