package org.sormula.tests.cache.readwrite;
import org.sormula.Table;
import org.sormula.operation.OperationException;
import org.sormula.operation.UpdateOperation;

// TODO remove or add test?
public class TestUpdateOp<R> extends UpdateOperation<R>
{
    public TestUpdateOp(Table<R> table) throws OperationException
    {
        super(table);
    }
}