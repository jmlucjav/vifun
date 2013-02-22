# Vifun: a GUI to help visually tweak Solr boosting


Description
---------------

Did you ever spend lots of time trying to tweak all numbers in a edismax handler qf, bf, etc params so docs get scored to your liking?
Imagine you have the params below, is 20 the right boosting for `name` or is it too much? Is `population` being boosted too much versus distance? What about new documents?

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

Features
------------

- Can tweak numeric valuesl in the following params: qf, pf, bf, bq, boost, mm (others can be easily added) even in &lt;appends&gt; or &lt;invariants&gt;
- View side by side a Baseline query result and how it changes when you gradually change each value in the params
- Colorized values, color depends on how the document does related to baseline query
- Tooltips give you Explain info 
- Works on remote Solr installations
- Tested with Solr 3.6, 4.0 and 4.1 (other versions would work too, as long as wt=javabin format is compatible)
- Developed using Groovy/Griffon

Requirements
-------------------

- /select handler should be available, and not have any **&lt;appends&gt; or &lt;invariants&gt;**, as it could interfere with how vifun works.
- Java6 is needed (maybe it runs on Java5 too). A JRE should be enough.

Getting started
-------------------

- Download the zip bundle http://code.google.com/p/vifun/downloads/detail?name=vifun-0.3.zip and unzip
- Run `vifun-0.4\bin\vifun.bat` or `vifun-04\bin\vifun` if on linux/OSX 
- Edit `Solr URL` to match yours (in Sol4.1 default is http://localhost:8983/solr/collection1 for example)
![hander selection](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-handlers.jpg)
- `Show Handerls`, and select the handler you wish to tweak from `Handerls` dropdown. The text area below shows the parameters of the handler.
- Modify the values to run a baseline query:
    - `q`: query string you want to use
    - `rows`: as in Solr
    - `fl`: comma separated list of fields you want to show for each doc, keep it short (other fields needed will be added, like the id, score) 
    - `rest`: in case you need to add more params, for example: sfield, fq etc)
![query params](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-qparams.jpg)
- `Run Query`. The two panels on the right will show the same result, sorted by score. 
![results](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-results.jpg)
- Use the mouse to select the number you want to tweak in Score params (select all the digits). Note the label of the field is highlighted with current value.
![target selection](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-selecttarget.jpg)
- Move the slider, release and see how a new query is run, and you can compare how result changes with the current value. In the Current
table, you can see current position/score and also delta relative to the baseline. The colour of the row reflects how much the doc gained/lost. 
![tweaking a value](https://github.com/jmlucjav/vifun/raw/master/img/screenshot-baseline.jpg)
- You can increase the limits of the slider, and if you are satisfied with a value, set it, so it will be set to current value. 
- Tweak another number...
- Tooltips exist in Current (Explain info) and Baseline (pos/score of that doc in Current, and Explain in Score column)


### If you want to run from source (griffon1.1.0 required):

- Clone the project
- `cd griffon-app`
- `griffon run-app`
- point to your Solr instance and proceed as above

How it works
----------------

It's very simple really, after taking all parameters from the selected handler, and overwriting them when needed (user changed them in the UI or with the slider), a
request is made to /select, this way any param can be tweaked, even invariants. Baseline result is kept around and compared with Current one, and rows are colored based
on how much its position/score changed. 

Contributing
----------------

This is released under Apache 2.0 License. If you want to contribute, issues, pull requests, issues etc are welcome. I already
opened issues for things I want to implement in the future.

**Contact**: jmlucjav@gmail.com

