package domain;

import java.awt.*;


public enum LocationType {
    COMPONENTS,LEISURE,EDUCATION,DOMESTIC,TRANSPORT,WORKPLACE;
    private Color initColor;


    public Color getInitColor() {
        return initColor;
    }

    public void setInitColor(Color initColor) {
        this.initColor = initColor;
    }


}
