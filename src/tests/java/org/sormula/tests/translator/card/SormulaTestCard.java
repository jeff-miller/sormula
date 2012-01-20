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

import org.sormula.annotation.Column;
import org.sormula.annotation.ImplicitType;


/**
 * Row class for {@link CardTranslatorTest}.
 * 
 * @author Jeff Miller
 * @since 1.6
 */
public class SormulaTestCard
{
    @Column(primaryKey=true)
    @ImplicitType(translator=RankTranslator.class)
    Rank rank; 
    
    @Column(primaryKey=true)
    Suit suit; // SuitTranslator defined in Suit class
    
    public SormulaTestCard()
    {
    }
    
    public SormulaTestCard(Rank rank, Suit suit)
    {
        this.rank = rank;
        this.suit = suit;
    }
    
    public Rank getRank()
    {
        return rank;
    }
    public void setRank(Rank rank)
    {
        this.rank = rank;
    }
    
    public Suit getSuit()
    {
        return suit;
    }
    public void setSuit(Suit suit)
    {
        this.suit = suit;
    }
}
