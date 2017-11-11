/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.intro.app.web.document.decomment;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.intro.app.model.document.decomment.parts.DfDecoMapTablePart;
import org.lastaflute.web.validation.Required;

/**
 * The pickup result of decomment as virtual pickup.
 * @author hakiba
 * @author jflute
 * @author cabos
 */
public class DecommentPickupResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** list of table part */
    @Valid @NotNull // can be empty first
    public List<TablePart> tables;

    // done hakiba move it under tables by jflute (2017/08/17)
    // done hakiba validator annotation (Required only) by jflute (2017/08/17)
    public static class TablePart {

        /**
         * table name
         * e.g. "MEMBER"
         */
        @Required
        public String tableName;

        /** list of column part, contains saved comments */
        @Valid
        @Required
        public List<ColumnPart> columns;

        public static class ColumnPart {

            /**
             * column name
             * e.g. "MEMBER_NAME"
             */
            @Required
            public String columnName;

            /** list of decomment properties associated column  */
            @Valid
            @Required
            public List<PropertyPart> properties;

            public static class PropertyPart {

                /**
                 * decomment saved as decomment piece map
                 * e.g. "decomment means 'deco' + 'database comment'"
                 */
                @Required
                public String decomment;

                /**
                 * column comment on table definition
                 * The comments on database may be blank so null allowed.
                 * e.g. "let's cabos" (NullAllowed)
                 */
                public String databaseComment;

                /**
                 * column comment version
                 * The comment version will update when the decomment.dfprop file is picked up.
                 * e.g. 3
                 */
                @Required
                public Long commentVersion;

                /**
                 * the list of ancestor authors
                 * e.g. ["cabos", "hakiba", "deco"]
                 */
                @Valid
                @Required
                public List<String> authorList;

                // =======================================================================
                //                                                             Constructor
                //                                                             ===========
                public PropertyPart(DfDecoMapPropertyPart property) {
                    this.decomment = property.getDecomment();
                    this.databaseComment = property.getDatabaseComment();
                    this.commentVersion = property.getCommentVersion();
                    this.authorList = property.getAuthorList();
                }
            }

            public ColumnPart(DfDecoMapColumnPart columnPart) {
                this.columnName = columnPart.getColumnName();
                this.properties =
                    columnPart.getPropertyList().stream().map(property -> new PropertyPart(property)).collect(Collectors.toList());
            }
        }

        public TablePart(DfDecoMapTablePart tablePart) {
            this.tableName = tablePart.getTableName();
            this.columns = tablePart.getColumnList().stream().map(columnPart -> new ColumnPart(columnPart)).collect(Collectors.toList());
        }
    }

    public DecommentPickupResult(List<DfDecoMapTablePart> tableParts) {
        this.tables = tableParts.stream().map(tablePart -> new TablePart(tablePart)).collect(Collectors.toList());
    }
}
