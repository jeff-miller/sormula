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
package org.sormula.tests.translator.card;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests custom translators for rank and suit columns in   
 * {@link SormulaTestCard}. Rank and suit are stored as integers in
 * the database but are stored as objects of type {@link Rank} and
 * {@link Suit}.
 * 
 * @author Jeff Miller
 * @since 1.6 and 2.0
 */
@Test(singleThreaded=true, groups="translator")
public class CardTranslatorTest extends DatabaseTest<SormulaTestCard>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        // add RankTranslator to test programmatic configuration
        // SuitTranslator is defined with annotation in SormulaTestCard
        //getDatabase().addTypeTranslator(Rank.class, new RankTranslator());

        createTable(SormulaTestCard.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestCard.class.getSimpleName() + " (" +
                " \"rank\" INTEGER," +
                " suit INTEGER " +
                ")"
            );
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        begin();
        insert("3", "Diamonds");
        insert("A", "Spades");
        insert("K", "Spades");
        insert("Q", "Hearts");
        commit();
    }
    void insert(String rankDescription, String suitDescription) throws SormulaException
    {
        SormulaTestCard card = new SormulaTestCard(new Rank(rankDescription), new Suit(suitDescription));
        assert getTable().insert(card) == 1 : "card row was not inserted " + 
            rankDescription + " " + suitDescription;
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        Rank rank = new Rank("A");
        Suit suit = new Suit("Spades");
        
        begin();
        SormulaTestCard card = getTable().select(rank, suit);
        assert card != null : "no card was selected";
        assert card.getRank().getValue() == rank.getValue() &&
               card.getSuit().getId() == suit.getId() : "wrong card selected";
        commit();
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void deleteTest() throws SormulaException
    {
        Rank rank = new Rank("K");
        Suit suit = new Suit("Spades");
        
        begin();
        assert getTable().delete(rank, suit) == 1 : "card was not deleted";
        commit();
    }
}


