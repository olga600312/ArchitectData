package domain;

import lombok.Builder;
import lombok.Data;

/**
 * Create by Aviv POS
 * User: olgats
 * Date: 26/07/2020
 * Time: 12:24
 */
@Data
@Builder
public class VerticalLocation {
    private String name;
    private LocationType type;
}
