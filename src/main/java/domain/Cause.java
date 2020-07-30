package domain;


import lombok.Builder;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;

/**
 * Create by Aviv POS
 * User: olgats
 * Date: 26/07/2020
 * Time: 09:24
 */
@Data
@Builder
public class Cause {
    private HorizontalLocation rowHeader;
    private ArrayList<Entry> data;




    @Data
    @Builder
    public static class Entry {
        private VerticalLocation columnHeader;
        private int total;
        private transient Color color;
        private  String file;
    }

    public Entry entryAt(int index) {
        return index >= 0 && index < data.size() ? data.get(index) : null;
    }

}
