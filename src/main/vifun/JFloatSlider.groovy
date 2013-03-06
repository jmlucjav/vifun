import javax.swing.*

//starting point taken from http://www.developpez.net/forums/d314533/java/interfaces-graphiques-java/awt-swing/jslider-float/
public class JFloatSlider extends JSlider
{
    private int SCALE;
    private String param
    private float min
    private float max
    private float tick

    public JFloatSlider(){}

    public void initValues(String tparam, float selasint, int scale)
    {
        SCALE = scale
        param = tparam
        this.setPaintLabels(true);
        //this.setPaintTicks(true);
        if (param.equals('mm')) {
            setMinimum(0);
            setMaximum((int)(100*SCALE));
        } else if (param.equals('tie')) {
            setMinimum(0);
            setMaximum((int)(1*SCALE));
        } else {
            if (selasint > 0) {
                setMinimum(0);
                setMaximum((int)(10*selasint*SCALE));
            } else if (selasint == 0) {
                setMinimum(-10*SCALE);
                setMaximum(10*SCALE);
            } else {
                setMaximum(0);
                setMinimum((int)(10*selasint*SCALE));
            }
        }
        min = getMinimum()/SCALE
        max = getMaximum()/SCALE
        tick = Math.abs(max-min)/5
        this.setValue((int)(selasint*SCALE));
        setLabels()
    }

    public void increaseLimits() { 
        if (!param.equals('mm') && !param.equals('tie')) {
            setMinimum((10*getMinimum()) as Integer);
            min *= 10
            setMaximum((10*getMaximum()) as Integer);
            max *= 10
            tick *= 10
            setLabels()
        }
    }

    public void setLabels() { 
        Hashtable ht = new Hashtable();
        for (float i = min; i <= max; i+=tick)
        {
            JLabel l = new JLabel(SCALE>1 ? ""+i : ""+(i as Integer));
            ht.put(new Integer((int)Math.rint(i*SCALE)), l);
        }
        this.setLabelTable(ht);
        def ticksp = (int)((getMaximum()-getMinimum())/20)
        if (!ticksp){
            ticksp = 1
        }
        this.setMinorTickSpacing(ticksp);
        this.setPaintTicks(true);
    }

    public String getFloatValue() { 
        if (SCALE>1){
            return (float)getValue()/(float)SCALE;
        }else{
            return getValue()
        }
    }
    
}
