import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;

/**
 * Sormula does not require DAO's but they are trivial to implement if you want to use them.
 * <p>
 * These are some of the methods that the DAO will inherit:
 * <pre>
    public int delete(Inventory row)
    public int delete(Object... parameters)
    public int deleteAll()
    public int deleteAll(Collection<Inventory> rows)
    public int deleteAllBatch(Collection<Inventory> rows)
    public int insert(Inventory row)
    public int insertAll(Collection<Inventory> rows)
    public int insertAllBatch(Collection<Inventory> rows)
    public int save(Inventory row)
    public int saveAll(Collection<Inventory> rows)
    public Inventory select(Object... primaryKeys)
    public List<Inventory> selectAll()
    public List<Inventory> selectAllCustom(String customSql, Object... parameters)
    public List<Inventory> selectAllWhere(String whereConditionName, Object... parameters)
    public List<Inventory> selectAllWhereOrdered(String whereConditionName, String orderByName, Object... parameters)
    public <T> T selectAvg(String expression)
    public <T> T selectCount(String expression, String whereConditionName, Object... parameters)
    public <T> T selectCount(String expression)
    public Inventory selectCustom(String customSql, Object... parameters)
    public <T> T selectMax(String expression, String whereConditionName, Object... parameters)
    public <T> T selectMax(String expression)
    public <T> T selectMin(String expression, String whereConditionName, Object... parameters)
    public <T> T selectMin(String expression)
    public <T> T selectSum(String expression, String whereConditionName, Object... parameters)
    public <T> T selectSum(String expression)
    public Inventory selectWhere(String whereConditionName, Object... parameters)
    public int update(Inventory row)
    public int updateAll(Collection<Inventory> rows)
    public int updateAllBatch(Collection<Inventory> rows)
 * </pre>
 * 
 * @author Jeff Miller
 */
public class InventoryDAO extends Table<Inventory>
{
    public InventoryDAO(Database database) throws SormulaException
    {
        super(database, Inventory.class);
    }
}
