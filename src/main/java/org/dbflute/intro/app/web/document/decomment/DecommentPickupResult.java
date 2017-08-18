package org.dbflute.intro.app.web.document.decomment;

import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapTablePart;
import org.lastaflute.web.validation.Required;

/**
 * @author hakiba
 */
public class DecommentPickupResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    public List<TablePart> tables;

    // ===================================================================================
    //                                                                         Inner Class
    //                                                                         ===========
    // TODO done hakiba move it under tables by jflute (2017/08/17)
    // TODO done hakiba validator annotation (Required only) by jflute (2017/08/17)
    public static class TablePart {
        @Required
        public String tableName;
        @Required
        public List<ColumnPart> columns;

        public TablePart(DfDecoMapTablePart tablePart) {
            this.tableName = tablePart.getTableName();
            this.columns = tablePart.getColumns().stream().map(columnPart -> new ColumnPart(columnPart)).collect(Collectors.toList());
        }

        public static class ColumnPart {
            @Required
            public String columnName;
            @Required
            public List<PropertyPart> properties;

            public ColumnPart(DfDecoMapColumnPart columnPart) {
                this.columnName = columnPart.getColumnName();
                this.properties =
                    columnPart.getProperties().stream().map(property -> new PropertyPart(property)).collect(Collectors.toList());
            }

            // TODO done hakiba PropertyPart by jflute (2017/08/17)
            public static class PropertyPart {
                @Required
                public String decomment;
                @Required
                public String databaseComment;
                @Required
                public String previousWholeComment;
                @Required
                public Long commentVersion;
                @Required
                public List<String> authorList;

                public PropertyPart(DfDecoMapColumnPart.ColumnPropertyPart property) {
                    this.decomment = property.getDecomment();
                    this.databaseComment = property.getDatabaseComment();
                    this.previousWholeComment = property.getPreviousWholeComment();
                    this.commentVersion = property.getCommentVersion();
                    this.authorList = property.getAuthorList();
                }
            }
        }
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DecommentPickupResult(List<DfDecoMapTablePart> tableParts) {
        this.tables = tableParts.stream().map(tablePart -> new TablePart(tablePart)).collect(Collectors.toList());
    }
}
