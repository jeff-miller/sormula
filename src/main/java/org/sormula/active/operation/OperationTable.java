package org.sormula.active.operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveRecord;
import org.sormula.annotation.Row;
import org.sormula.annotation.cache.Cached;
import org.sormula.annotation.cache.CachedAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;


/**
 * A {@link Table} for use by one transaction within an {@link ActiveOperation}. Uses a custom
 * {@link RowTranslator} that ensures all records are attached to the active database for 
 * this table.
 * <p> 
 * Intended for use by {@link ActiveOperation} only.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 * 
 * @param <R> record type of table
 */
public class OperationTable<R extends ActiveRecord> extends Table<R>
{
    ActiveDatabase activeDatabase;
    
    
    /**
     * Constructs for an operation database and class or record.
     * 
     * @param operationDatabase source of tables to use in active record operation
     * @param rowClass type of record
     * @throws SormulaException if error
     */
    public OperationTable(OperationDatabase operationDatabase, Class<R> rowClass) throws SormulaException
    {
        super(operationDatabase, rowClass);
        this.activeDatabase = operationDatabase.getActiveDatabase();
    }
    
    
    /**
     * Looks for {@link Cache} annotation in record, {@link OperationDatabase}, and {@link ActiveDatabase}.
     * 
     * {@inheritDoc}
     */
    protected Cached initCachedAnnotation()
    {
        // must get active database from operation database since activeDatabase not yet initialized
        OperationDatabase odb = (OperationDatabase)getDatabase();
        
        // row, table, database
        return new CachedAnnotationReader(getRowClass(), getClass(), odb.getActiveDatabase().getClass()).getAnnotation();
    }
    
    
    /**
     * {@inheritDoc}
     * Creates a {@link RowTranslator} where {@link RowTranslator#read(ResultSet, int, Object)}
     * and {@link RowTranslator#write(PreparedStatement, int, Object)} attach the active
     * database to the row using {@link ActiveRecord#attach(ActiveDatabase)}.
     */
    @Override
    protected RowTranslator<R> initRowTranslator(Row rowAnnotation) throws TranslatorException
    {
        // row translator attaches each row to active database
        return new RowTranslator<R>(this, rowAnnotation)
        {
            @Override
            public int read(ResultSet resultSet, int columnIndex, R row) throws TranslatorException
            {
                row.attach(activeDatabase);
                return super.read(resultSet, columnIndex, row);
            }

            @Override
            public int write(PreparedStatement preparedStatement, int parameterIndex, R row) throws TranslatorException
            {
                row.attach(activeDatabase);    
                return super.write(preparedStatement, parameterIndex, row);
            }
        };
    }
}                

