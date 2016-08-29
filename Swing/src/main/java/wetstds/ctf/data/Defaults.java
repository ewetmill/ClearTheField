package wetstds.ctf.data;

import javax.swing.JLabel;
import javax.swing.JRadioButtonMenuItem;
import wetstds.ctf.Difficulty;

public class Defaults
{
   private int columns;
   private int rows;
   private int numBombs;
   private JLabel scoreLabel = new JLabel();
   private JRadioButtonMenuItem menuItem;
   
   public Defaults(int columns, int rows, int numBombs, Difficulty difficulty)
   {
      this.columns = columns;
      this.rows = rows;
      this.numBombs = numBombs;

      // Capitalize the first letter and make the rest lower case
      String label = difficulty.name().toUpperCase().substring(0,1) + difficulty.name().toLowerCase().substring(1);
      menuItem = new JRadioButtonMenuItem(label);
   }

   public void setColumns(int columns)
   {
      this.columns = columns;
   }

   public void setRows(int rows)
   {
      this.rows = rows;
   }

   public void setNumBombs(int numBombs)
   {
      this.numBombs = numBombs;
   }

   public int getColumns()
   {
      return columns;
   }

   public int getRows()
   {
      return rows;
   }

   public int getNumBombs()
   {
      return numBombs;
   }

   public JLabel getScoreLabel()
   {
      return scoreLabel;
   }

   public JRadioButtonMenuItem getMenuItem()
   {
      return menuItem;
   }
}
