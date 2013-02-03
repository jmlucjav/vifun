application {
    title = 'Vifun'
    startupGroups = ['vifun']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "vifun"
    'vifun' {
        model      = 'vifun.VifunModel'
        view       = 'vifun.VifunView'
        controller = 'vifun.VifunController'
    }

}
