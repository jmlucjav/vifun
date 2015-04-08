# Vifun: a GUI to help visually tweak Solr scoring
![full window](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-win-small.jpg)

Description
---------------

Did you ever spend lots of time trying to tweak all numbers in a **edismax** handler **qf**, **bf**, etc params so docs get scored to your liking?
Imagine you have the params below, is 20 the right boosting for *name* or is it too much? Is *population* being boosted too much versus distance? What about *moddate*?

			<!-- fields, boost some -->
			<str name="qf">name^20 textsuggest^10 edge^5 ngram^2 phonetic^1</str>
            <str name="mm">33%</str>
			<!-- boost closest hits -->
            <str name="bf">recip(geodist(),1,500,0)</str>
			<!-- boost by population -->
			<str name="bf">product(log(sum(population,1)),100)</str>
			<!-- boost newest docs -->
			<str name="bf">recip(rord(moddate),1,1000,1000)</str>

This tool was developed in order to help me tweak the values of boosting functions etc in Solr, typically when using edismax
handler. If you are fed up of: change a number a bit, restart Solr, run the same query to see how documents are scored now...then this tool is for you.

You can watch a [screencast](https://www.youtube.com/watch?v=QGgM76HWIcA) showing how it works, and you can also read more abou it [in this post] (https://medium.com/@jmlucjav/tweaking-solr-edismax-relevancy-24b541c79bc9)

Features
------------

- Can tweak numeric values (int or float) in the following params: **qf, pf, pf2, pf3, ps, ps2, ps3, bf, bq, boost, mm, tie** (others can be easily added) even in **&lt;appends&gt; or &lt;invariants&gt;**
- View side by side a Baseline query result and how it changes when you gradually change each value in the params
- Colorized values, color depends on how the document does related to baseline query. Also colorized side by side comparison of both Explain info
- Works on remote Solr installations
- Tested with Solr 3.6, 4.0, 4.X and 5.0 (other versions would work too, as long as wt=javabin format is compatible)
- Developed using Groovy/Griffon

Requirements
-------------------

- **/select** handler should be available (other handler can be used, see in Advanced Configuration), and not have any **&lt;appends&gt; or &lt;invariants&gt;**, as it could interfere with how vifun works.
- **jdk1.6** is needed (maybe it runs on jdk1.5 too, but I didn't test). A JRE should be enough. 
- Last test I did was with Groovy 2.3.6 and JDK1.8


Getting started
-------------------

### Click [here to download latest version](https://github.com/jmlucjav/vifun/releases) and unzip
- Fix permssions first if on linux/OSX `chmod +x bin/vifun` 
- Run `bin\vifun.bat` or `bin/vifun` 
- Edit **Solr URL** to match yours (in Sol4.1 default is http://localhost:8983/solr/collection1 for example)
- **Show Handerls**, and select the handler you wish to tweak from **Handerls** dropdown. The text area below shows the parameters of the handler.
- Modify the values to run a baseline query:
    - **q**: query string you want to use
    - **rows**: as in Solr, don't choose a number too small, so you can see more documents, I typically use 200/400
    - **fl**: comma separated list of fields you want to show for each doc, keep it short (other fields needed will be added, like the id, score) 
    - **rest**: in case you need to add more params, for example: sfield, fq etc)
![query params](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-qparams.jpg)
- **Create Baseline**. The two panels on the right will show the same result, sorted by score. 
- Use the mouse to select the number you want to tweak in Score params (select all the digits). Note the label of the field is highlighted with current value. Notice that you can also manually edit the param contents.
![target selection](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-selecttarget.jpg)
- Move the slider, release and see how a new query is run, and you can compare how result changes with the current value. In the Current
table, you can see current position/score and also delta relative to the baseline. The colour of the row reflects how much the doc gained/lost. A + means the maximum gain (the doc was not in Baseline).
![tweaking a value](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-baseline.jpg)
- A doubleclick in any cell in Current table will show a window with a side by side comparison of explain info from the document, differences are highlighted
![tweaking a value](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-explain-comparison.jpg)
- You can increase the limits of the slider, and if you are satisfied with a value, set it, so it will be set to current value. 
- Tweak another number...
- Tooltips exist in Current (Explain info) and Baseline (pos/score of that doc in Current, and Explain in Score column)


### If you want to run from source:

- Clone the project
- if you don't have griffon-1.2.0 installed, the following command will set it up for you: `griffonw.bat` or `chmod +x griffonw; ./griffonw`
- `cd vifun`
- `griffon run-app`
- point to your Solr instance and proceed as above

How it works
----------------

It's very simple really, after taking all parameters from the selected handler, and overwriting them when needed (user changed them in the UI or with the slider), a
request is made to /select, this way any param can be tweaked, even invariants. Baseline result is kept around and compared with Current one, and rows are colored based
on how much its position/score changed. 

Advanced Configuration (only available when running from source)
----------------------

There are a couple of things that can be customized:
- griffon-app\conf\Config.groovy
    - vifun.baseline.rows.multiplier: when running the Baseline query, *rows* param will be set to: *rows* * *this param*. The intent of this is to have a buffer of docs in order to have more info about how they moved up in Current. When a doc was not present at all in Baseline will show up with a delta value + in Current.
    - vifun.handler: in case you prefer to use a different handler than /select to run our queries.
- griffon-app\conf\BuildConfig.groovy
    - if you want to change memory settings, search for *memory* and customize as desired

Related tools
----------------

- **LucidWorks Relevancy Workbench** looks pretty similar, tough web based, maybe inspired by vifun? http://www.lucidworks.com/market_app/lucidworks-relevancy-workbench/
- **Open Enterprise Search Platform (OpenESP)** has integrated vifun https://github.com/openesp/openesp

Contributing
----------------

This is released under Apache 2.0 License. If you want to contribute, issues, pull requests, issues etc are welcome. I already
opened issues for things I want to implement in the future.

**Contact**: jmlucjav AT gmail DOT com
