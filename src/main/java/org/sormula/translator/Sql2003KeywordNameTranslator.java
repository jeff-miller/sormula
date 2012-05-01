/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2012 Jeff Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sormula.translator;

import java.util.HashSet;
import java.util.Set;


/**
 * Converts keyword from SQL 2003 to keyword with delimiters. For example:
 * <ul>
 * <li>alter to "alter"</li>
 * <li>between to [between]</li>
 * <li>date to `date`</li>
 * </ul>
 * Delimiters can be set with {@link #setOpeningDelimiter(String)} and {@link #setClosingDelimiter(String)}.
 * They are double quotes by default since they are typical SQL standard.
 * <p>
 * Case is preserved. Use {@link UpperCaseNameTranslator} or {@link LowerCaseNameTranslator}
 * in addition to this class to force a specific case. With most datatabases a delimited identifier
 * is case-sensitive.
 * 
 * @since 1.8
 * @author Jeff Miller
 */
public class Sql2003KeywordNameTranslator extends DelimitedNameTranslator
{
    static final Set<String> keywords;
    static
    {
        keywords = new HashSet<>(500);
        keywords.add("ADD");
        keywords.add("ALL");
        keywords.add("ALLOCATE");
        keywords.add("ALTER");
        keywords.add("AND");
        keywords.add("ANY");
        keywords.add("ARE");
        keywords.add("ARRAY");
        keywords.add("AS");
        keywords.add("ASENSITIVE");
        keywords.add("ASYMMETRIC");
        keywords.add("AT");
        keywords.add("ATOMIC");
        keywords.add("AUTHORIZATION");
        keywords.add("BEGIN");
        keywords.add("BETWEEN");
        keywords.add("BIGINT");
        keywords.add("BINARY");
        keywords.add("BLOB");
        keywords.add("BOOLEAN");
        keywords.add("BOTH");
        keywords.add("BY");
        keywords.add("CALL");
        keywords.add("CALLED");
        keywords.add("CASCADED");
        keywords.add("CASE");
        keywords.add("CAST");
        keywords.add("CHAR");
        keywords.add("CHARACTER");
        keywords.add("CHECK");
        keywords.add("CLOB");
        keywords.add("CLOSE");
        keywords.add("COLLATE");
        keywords.add("COLUMN");
        keywords.add("COMMIT");
        keywords.add("CONDITION");
        keywords.add("CONNECT");
        keywords.add("CONSTRAINT");
        keywords.add("CONTINUE");
        keywords.add("CORRESPONDING");
        keywords.add("CREATE");
        keywords.add("CROSS");
        keywords.add("CUBE");
        keywords.add("CURRENT");
        keywords.add("CURRENT_DATE");
        keywords.add("CURRENT_DEFAULT_TRANSFORM_GROUP");
        keywords.add("CURRENT_PATH");
        keywords.add("CURRENT_ROLE");
        keywords.add("CURRENT_TIME");
        keywords.add("CURRENT_TIMESTAMP");
        keywords.add("CURRENT_TRANSFORM_GROUP_FOR_TYPE");
        keywords.add("CURRENT_USER");
        keywords.add("CURSOR");
        keywords.add("CYCLE");
        keywords.add("DATE");
        keywords.add("DAY");
        keywords.add("DEALLOCATE");
        keywords.add("DEC");
        keywords.add("DECIMAL");
        keywords.add("DECLARE");
        keywords.add("DEFAULT");
        keywords.add("DELETE");
        keywords.add("DEREF");
        keywords.add("DESCRIBE");
        keywords.add("DETERMINISTIC");
        keywords.add("DISCONNECT");
        keywords.add("DISTINCT");
        keywords.add("DO");
        keywords.add("DOUBLE");
        keywords.add("DROP");
        keywords.add("DYNAMIC");
        keywords.add("EACH");
        keywords.add("ELEMENT");
        keywords.add("ELSE");
        keywords.add("ELSEIF");
        keywords.add("END");
        keywords.add("ESCAPE");
        keywords.add("EXCEPT");
        keywords.add("EXEC");
        keywords.add("EXECUTE");
        keywords.add("EXISTS");
        keywords.add("EXIT");
        keywords.add("EXTERNAL");
        keywords.add("FALSE");
        keywords.add("FETCH");
        keywords.add("FILTER");
        keywords.add("FLOAT");
        keywords.add("FOR");
        keywords.add("FOREIGN");
        keywords.add("FREE");
        keywords.add("FROM");
        keywords.add("FULL");
        keywords.add("FUNCTION");
        keywords.add("GET");
        keywords.add("GLOBAL");
        keywords.add("GRANT");
        keywords.add("GROUP");
        keywords.add("GROUPING");
        keywords.add("HANDLER");
        keywords.add("HAVING");
        keywords.add("HOLD");
        keywords.add("HOUR");
        keywords.add("IDENTITY");
        keywords.add("IF");
        keywords.add("IMMEDIATE");
        keywords.add("IN");
        keywords.add("INDICATOR");
        keywords.add("INNER");
        keywords.add("INOUT");
        keywords.add("INPUT");
        keywords.add("INSENSITIVE");
        keywords.add("INSERT");
        keywords.add("INT");
        keywords.add("INTEGER");
        keywords.add("INTERSECT");
        keywords.add("INTERVAL");
        keywords.add("INTO");
        keywords.add("IS");
        keywords.add("ITERATE");
        keywords.add("JOIN");
        keywords.add("LANGUAGE");
        keywords.add("LARGE");
        keywords.add("LATERAL");
        keywords.add("LEADING");
        keywords.add("LEAVE");
        keywords.add("LEFT");
        keywords.add("LIKE");
        keywords.add("LOCAL");
        keywords.add("LOCALTIME");
        keywords.add("LOCALTIMESTAMP");
        keywords.add("LOOP");
        keywords.add("MATCH");
        keywords.add("MEMBER");
        keywords.add("MERGE");
        keywords.add("METHOD");
        keywords.add("MINUTE");
        keywords.add("MODIFIES");
        keywords.add("MODULE");
        keywords.add("MONTH");
        keywords.add("MULTISET");
        keywords.add("NATIONAL");
        keywords.add("NATURAL");
        keywords.add("NCHAR");
        keywords.add("NCLOB");
        keywords.add("NEW");
        keywords.add("NO");
        keywords.add("NONE");
        keywords.add("NOT");
        keywords.add("NULL");
        keywords.add("NUMERIC");
        keywords.add("OF");
        keywords.add("OLD");
        keywords.add("ON");
        keywords.add("ONLY");
        keywords.add("OPEN");
        keywords.add("OR");
        keywords.add("ORDER");
        keywords.add("OUT");
        keywords.add("OUTER");
        keywords.add("OUTPUT");
        keywords.add("OVER");
        keywords.add("OVERLAPS");
        keywords.add("PARAMETER");
        keywords.add("PARTITION");
        keywords.add("PRECISION");
        keywords.add("PREPARE");
        keywords.add("PRIMARY");
        keywords.add("PROCEDURE");
        keywords.add("RANGE");
        keywords.add("READS");
        keywords.add("REAL");
        keywords.add("RECURSIVE");
        keywords.add("REF");
        keywords.add("REFERENCES");
        keywords.add("REFERENCING");
        keywords.add("RELEASE");
        keywords.add("REPEAT");
        keywords.add("RESIGNAL");
        keywords.add("RESULT");
        keywords.add("RETURN");
        keywords.add("RETURNS");
        keywords.add("REVOKE");
        keywords.add("RIGHT");
        keywords.add("ROLLBACK");
        keywords.add("ROLLUP");
        keywords.add("ROW");
        keywords.add("ROWS");
        keywords.add("SAVEPOINT");
        keywords.add("SCOPE");
        keywords.add("SCROLL");
        keywords.add("SEARCH");
        keywords.add("SECOND");
        keywords.add("SELECT");
        keywords.add("SENSITIVE");
        keywords.add("SESSION_USER");
        keywords.add("SET");
        keywords.add("SIGNAL");
        keywords.add("SIMILAR");
        keywords.add("SMALLINT");
        keywords.add("SOME");
        keywords.add("SPECIFIC");
        keywords.add("SPECIFICTYPE");
        keywords.add("SQL");
        keywords.add("SQLEXCEPTION");
        keywords.add("SQLSTATE");
        keywords.add("SQLWARNING");
        keywords.add("START");
        keywords.add("STATIC");
        keywords.add("SUBMULTISET");
        keywords.add("SYMMETRIC");
        keywords.add("SYSTEM");
        keywords.add("SYSTEM_USER");
        keywords.add("TABLE");
        keywords.add("TABLESAMPLE");
        keywords.add("THEN");
        keywords.add("TIME");
        keywords.add("TIMESTAMP");
        keywords.add("TIMEZONE_HOUR");
        keywords.add("TIMEZONE_MINUTE");
        keywords.add("TO");
        keywords.add("TRAILING");
        keywords.add("TRANSLATION");
        keywords.add("TREAT");
        keywords.add("TRIGGER");
        keywords.add("TRUE");
        keywords.add("UNDO");
        keywords.add("UNION");
        keywords.add("UNIQUE");
        keywords.add("UNKNOWN");
        keywords.add("UNNEST");
        keywords.add("UNTIL");
        keywords.add("UPDATE");
        keywords.add("USER");
        keywords.add("USING");
        keywords.add("VALUE");
        keywords.add("VALUES");
        keywords.add("VARCHAR");
        keywords.add("VARYING");
        keywords.add("WHEN");
        keywords.add("WHENEVER");
        keywords.add("WHERE");
        keywords.add("WHILE");
        keywords.add("WINDOW");
        keywords.add("WITH");
        keywords.add("WITHIN");
        keywords.add("WITHOUT");
        keywords.add("YEAR");
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String translate(String name, Class rowClass)
    {
        if (keywords.contains(name.toUpperCase())) return super.translate(name, rowClass);
        else return name;
    }
}
