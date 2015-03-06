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
 * Converts keyword from SQL 92 to keyword with delimiters. For example:
 * <ul>
 * <li>alter to "alter"</li>
 * <li>between to [between]</li>
 * <li>date to `date`</li>
 * </ul>
 * Delimiters can be set with {@link #setOpeningDelimiter(String)} and {@link #setClosingDelimiter(String)}.
 * They are double quotes by default since they are typical SQL standard.
 * <p>
 * Case is preserved. Use {@link UpperCaseNameTranslator} or {@link LowerCaseNameTranslator}
 * in addition to this class to force a specific case. With most databases a delimited identifier
 * is case-sensitive.
 * 
 * @since 1.8 and 2.2
 * @author Jeff Miller
 */
public class Sql92KeywordNameTranslator extends DelimitedNameTranslator
{
    static final Set<String> keywords;
    static
    {
        keywords = new HashSet<>(500);
        keywords.add("ABSOLUTE");    
        keywords.add("ACTION");      
        keywords.add("ADD");
        keywords.add("ALL");
        keywords.add("ALLOCATE");
        keywords.add("ALTER");
        keywords.add("AND");
        keywords.add("ANY");
        keywords.add("ARE");
        keywords.add("AS");
        keywords.add("ASC");     
        keywords.add("ASSERTION");   
        keywords.add("AT");
        keywords.add("AUTHORIZATION");
        keywords.add("AVG");         
        keywords.add("BEGIN");
        keywords.add("BETWEEN");
        keywords.add("BIT");     
        keywords.add("BIT_LENGTH");      
        keywords.add("BOTH");
        keywords.add("BY");
        keywords.add("CALL");
        keywords.add("CASCADE");     
        keywords.add("CASCADED");
        keywords.add("CASE");
        keywords.add("CAST");
        keywords.add("CATALOG");     
        keywords.add("CHAR");
        keywords.add("CHAR_LENGTH");         
        keywords.add("CHARACTER");
        keywords.add("CHARACTER_LENGTH");        
        keywords.add("CHECK");
        keywords.add("CLOSE");
        keywords.add("COALESCE");        
        keywords.add("COLLATE");
        keywords.add("COLLATION");   
        keywords.add("COLUMN");
        keywords.add("COMMIT");
        keywords.add("CONDITION");
        keywords.add("CONNECT");
        keywords.add("CONNECTION");      
        keywords.add("CONSTRAINT");
        keywords.add("CONSTRAINTS");     
        keywords.add("CONTAINS");        
        keywords.add("CONTINUE");
        keywords.add("CONVERT");         
        keywords.add("CORRESPONDING");
        keywords.add("COUNT");       
        keywords.add("CREATE");
        keywords.add("CROSS");
        keywords.add("CURRENT");
        keywords.add("CURRENT_DATE");
        keywords.add("CURRENT_PATH");
        keywords.add("CURRENT_TIME");
        keywords.add("CURRENT_TIMESTAMP");
        keywords.add("CURRENT_USER");
        keywords.add("CURSOR");
        keywords.add("DATE");
        keywords.add("DAY");
        keywords.add("DEALLOCATE");
        keywords.add("DEC");
        keywords.add("DECIMAL");
        keywords.add("DECLARE");
        keywords.add("DEFAULT");
        keywords.add("DEFERRABLE");      
        keywords.add("DEFERRED");    
        keywords.add("DELETE");
        keywords.add("DESC");    
        keywords.add("DESCRIBE");
        keywords.add("DESCRIPTOR");      
        keywords.add("DETERMINISTIC");
        keywords.add("DIAGNOSTICS");     
        keywords.add("DISCONNECT");
        keywords.add("DISTINCT");
        keywords.add("DO");
        keywords.add("DOMAIN");      
        keywords.add("DOUBLE");
        keywords.add("DROP");
        keywords.add("ELSE");
        keywords.add("ELSEIF");
        keywords.add("END");
        keywords.add("ESCAPE");
        keywords.add("EXCEPT");
        keywords.add("EXCEPTION");   
        keywords.add("EXEC");
        keywords.add("EXECUTE");
        keywords.add("EXISTS");
        keywords.add("EXIT");
        keywords.add("EXTERNAL");
        keywords.add("EXTRACT");         
        keywords.add("FALSE");
        keywords.add("FETCH");
        keywords.add("FIRST");   
        keywords.add("FLOAT");
        keywords.add("FOR");
        keywords.add("FOREIGN");
        keywords.add("FOUND");   
        keywords.add("FROM");
        keywords.add("FULL");
        keywords.add("FUNCTION");
        keywords.add("GET");
        keywords.add("GLOBAL");
        keywords.add("GO");      
        keywords.add("GOTO");    
        keywords.add("GRANT");
        keywords.add("GROUP");
        keywords.add("HANDLER");
        keywords.add("HAVING");
        keywords.add("HOUR");
        keywords.add("IDENTITY");
        keywords.add("IF");
        keywords.add("IMMEDIATE");
        keywords.add("IN");
        keywords.add("INDICATOR");
        keywords.add("INITIALLY");   
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
        keywords.add("ISOLATION");   
        keywords.add("JOIN");
        keywords.add("KEY");     
        keywords.add("LANGUAGE");
        keywords.add("LAST");    
        keywords.add("LEADING");
        keywords.add("LEAVE");
        keywords.add("LEFT");
        keywords.add("LEVEL");   
        keywords.add("LIKE");
        keywords.add("LOCAL");
        keywords.add("LOOP");
        keywords.add("LOWER");       
        keywords.add("MATCH");
        keywords.add("MAX");         
        keywords.add("MIN");         
        keywords.add("MINUTE");
        keywords.add("MODULE");
        keywords.add("MONTH");
        keywords.add("NAMES");   
        keywords.add("NATIONAL");
        keywords.add("NATURAL");
        keywords.add("NCHAR");
        keywords.add("NEXT");    
        keywords.add("NO");
        keywords.add("NOT");
        keywords.add("NULL");
        keywords.add("NULLIF");      
        keywords.add("NUMERIC");
        keywords.add("OCTET_LENGTH");        
        keywords.add("OF");
        keywords.add("ON");
        keywords.add("ONLY");
        keywords.add("OPEN");
        keywords.add("OPTION");      
        keywords.add("OR");
        keywords.add("ORDER");
        keywords.add("OUT");
        keywords.add("OUTER");
        keywords.add("OUTPUT");
        keywords.add("OVERLAPS");
        keywords.add("PAD");     
        keywords.add("PARAMETER");
        keywords.add("PARTIAL");     
        keywords.add("PATH");    
        keywords.add("POSITION");        
        keywords.add("PRECISION");
        keywords.add("PREPARE");
        keywords.add("PRESERVE");    
        keywords.add("PRIMARY");
        keywords.add("PRIOR");   
        keywords.add("PRIVILEGES");      
        keywords.add("PROCEDURE");
        keywords.add("PUBLIC");      
        keywords.add("READ");    
        keywords.add("REAL");
        keywords.add("REFERENCES");
        keywords.add("RELATIVE");    
        keywords.add("REPEAT");
        keywords.add("RESIGNAL");
        keywords.add("RESTRICT");    
        keywords.add("RETURN");
        keywords.add("RETURNS");
        keywords.add("REVOKE");
        keywords.add("RIGHT");
        keywords.add("ROLLBACK");
        keywords.add("ROUTINE");     
        keywords.add("ROWS");
        keywords.add("SCHEMA");      
        keywords.add("SCROLL");
        keywords.add("SECOND");
        keywords.add("SECTION");     
        keywords.add("SELECT");
        keywords.add("SESSION");     
        keywords.add("SESSION_USER");
        keywords.add("SET");
        keywords.add("SIGNAL");
        keywords.add("SIZE");    
        keywords.add("SMALLINT");
        keywords.add("SOME");
        keywords.add("SPACE");   
        keywords.add("SPECIFIC");
        keywords.add("SQL");
        keywords.add("SQLCODE");         
        keywords.add("SQLERROR");        
        keywords.add("SQLEXCEPTION");
        keywords.add("SQLSTATE");
        keywords.add("SQLWARNING");
        keywords.add("SUBSTRING");       
        keywords.add("SUM");         
        keywords.add("SYSTEM_USER");
        keywords.add("TABLE");
        keywords.add("TEMPORARY");   
        keywords.add("THEN");
        keywords.add("TIME");
        keywords.add("TIMESTAMP");
        keywords.add("TIMEZONE_HOUR");
        keywords.add("TIMEZONE_MINUTE");
        keywords.add("TO");
        keywords.add("TRAILING");
        keywords.add("TRANSACTION");     
        keywords.add("TRANSLATE");       
        keywords.add("TRANSLATION");
        keywords.add("TRIM");        
        keywords.add("TRUE");
        keywords.add("UNDO");
        keywords.add("UNION");
        keywords.add("UNIQUE");
        keywords.add("UNKNOWN");
        keywords.add("UNTIL");
        keywords.add("UPDATE");
        keywords.add("UPPER");       
        keywords.add("USAGE");   
        keywords.add("USER");
        keywords.add("USING");
        keywords.add("VALUE");
        keywords.add("VALUES");
        keywords.add("VARCHAR");
        keywords.add("VARYING");
        keywords.add("VIEW");    
        keywords.add("WHEN");
        keywords.add("WHENEVER");
        keywords.add("WHERE");
        keywords.add("WHILE");
        keywords.add("WITH");
        keywords.add("WORK");    
        keywords.add("WRITE");   
        keywords.add("YEAR");
        keywords.add("ZONE");
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
