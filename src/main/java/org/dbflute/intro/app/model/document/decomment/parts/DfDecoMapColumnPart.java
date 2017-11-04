package org.dbflute.intro.app.model.document.decomment.parts;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hakiba
 * @author cabos
 */
public class DfDecoMapColumnPart {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Long MINIMUM_COMMENT_VERSION = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String columnName;
    protected List<DfDecoMapPropertyPart> propertyList;

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    @SuppressWarnings("unchecked")
    public static DfDecoMapColumnPart createColumnPart(Map<String, Object> columnPartMap) {
        DfDecoMapColumnPart column = new DfDecoMapColumnPart();
        column.setColumnName((String) columnPartMap.get("columnName"));
        List<DfDecoMapPropertyPart> propertyList = ((List<Map<String, Object>>) columnPartMap.get("propertyList")).stream()
            .map(DfDecoMapPropertyPart::new)
            .collect(Collectors.toList());
        column.setPropertyList(propertyList);
        return column;
    }

    public static DfDecoMapColumnPart createPickupColumnPart(Map.Entry<String, List<Map<String, Object>>> columnEntry) {
        final List<DfDecoMapPropertyPart> properties = columnEntry.getValue().stream().map(propertiesMap -> {
            return new DfDecoMapPropertyPart(propertiesMap);
        }).collect(Collectors.toList());
        DfDecoMapColumnPart column = new DfDecoMapColumnPart();
        column.setColumnName(columnEntry.getKey());
        column.setPropertyList(properties);
        return column;
    }

    public Map<String, Object> convertMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("columnName", columnName);
        map.put("propertyList", propertyList.stream().map(property -> property.convertMap()).collect(Collectors.toList()));
        return map;
    }

    public List<Map<String, Object>> convertPickupMap() {
        return propertyList.stream().map(property -> property.convertMap()).collect(Collectors.toList());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public List<DfDecoMapPropertyPart> getPropertyList() {
        return this.propertyList;
    }

    public void setPropertyList(List<DfDecoMapPropertyPart> properties) {
        this.propertyList = properties;
    }

    public long getLatestCommentVersion() {
        return this.propertyList.stream()
            .map(part -> part.getCommentVersion())
            .max(Comparator.naturalOrder())
            .orElse(MINIMUM_COMMENT_VERSION);
    }
}
