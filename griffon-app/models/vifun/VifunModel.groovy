package vifun

import groovy.beans.Bindable
import ca.odell.glazedlists.*
import ca.odell.glazedlists.gui.*
import ca.odell.glazedlists.swing.*


class VifunModel {
    SolrOps solr = new SolrOps()
    @Bindable String solrurl = "http://localhost:8983/solr/core0"
    Map handlers = [:]

    //selected handler
    Map handlerm = [:]
    @Bindable String handler
    @Bindable String handlerText
    //EventList handlersList = new BasicEventList()

    //current
    List<Map> currentMap = []
    @Bindable String currentParam
    //baseline
    List<Map> baselineMap = []
    @Bindable String baselineParam

    //buttons enabled?
    @Bindable boolean enabledQuery
    //@Bindable boolean enabledTake
    @Bindable boolean enabledBind
    @Bindable boolean enabledSlider
    //buttons enabled?
    @Bindable boolean enabledHandlerText
    @Bindable boolean enabledCurrentParam
    @Bindable boolean enabledBaselineParam

    //params
    @Bindable String q = 'm'
    @Bindable String rows
    @Bindable String fl
    List<String> qset = ['q','rows', 'fl']
    @Bindable String rest
    //edismax score related vars
    @Bindable String qf
    @Bindable String bf
    @Bindable String pf
    @Bindable String mm
    //@Bindable String tie
    List<String> fset = ['qf','pf', 'bf', 'mm']

    //change selected bf...
    @Bindable String tweakedFName
    @Bindable String tweakedFFormula
    @Bindable String tweakedFValue
    @Bindable String tweakedFValueNew

    //glazedlist stuff
    def columns = [[name: 'pos', title: 'Rank'],[name: 'posdelta', title: 'Delta'],[name: 'docfields', title: 'Doc'], [name: 'score', title: 'Score'],[name: 'scoredelta', title: 'Delta']]
    def columnsbaseline = [[name: 'pos', title: 'Rank'],[name: 'docfields', title: 'Doc'], [name: 'score', title: 'Score']]
    def rowComparator ={a,b ->
        if ('-'.equals(a) && '-'.equals(b)) return 0
        if ('-'.equals(a)) return -1
        if ('-'.equals(b)) return 1
        return a.pos<=> b.pos
    }
    EventList ctable = new SortedList( new BasicEventList(), rowComparator as Comparator)
    EventList btable = new SortedList( new BasicEventList(), rowComparator as Comparator)
}
