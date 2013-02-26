log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error  'org.apache.http'
    error  'org.codehaus.griffon'

    info   'griffon.util',
           'griffon.core',
           'griffon.swing',
           'griffon.app'

    debug   'org.apache.http.impl.conn.DefaultClientConnection'
}

//multliplier to ask for nb rows for baseline
vifun.baseline.rows.multiplier = 5
