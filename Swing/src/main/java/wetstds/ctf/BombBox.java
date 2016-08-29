package wetstds.ctf;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import wetstds.ctf.data.ImageData;

public class BombBox extends Container implements MouseListener
{
   /**
    * 
    */
   private static final long serialVersionUID = -6753494512742762284L;

   private enum ButtonStatus
   {
      MARKED,
      CLEARED,
      TAGGED,
      CLICKED
   }

   private int             xCoordinate;
   private int             yCoordinate;

   private JButton         box;
   private boolean         bomb;
   private ButtonStatus    buttonStatus   = ButtonStatus.CLEARED;
   private int             numAdjBombs;
   private BombFest        game;

   public BombBox(BombFest ms,
            int x,
            int y)
   {
      game = ms;
      xCoordinate = x;
      yCoordinate = y;

      box = new JButton();
      box.setFocusPainted(false);
      box.setMargin(new Insets(0,
               0,
               0,
               0));
      box.setFont(new Font("serif",
               Font.PLAIN,
               6));
      box.setVerticalTextPosition(AbstractButton.CENTER);
      box.setHorizontalTextPosition(AbstractButton.CENTER);

      box.addMouseListener(this);
   }

   public void add(JPanel panel)
   {
      panel.add(box);
   }

   public void winGame()
   {
      box.removeMouseListener(this);
   }

   public void removeButton()
   {
      box.setIcon(ImageData.BLANK_ICON);

      if (numAdjBombs != 0 && !isBomb())
         box.setText(Integer.toString(numAdjBombs));
      else if (numAdjBombs == 0 && !isBomb())
         box.setText("");

      box.setBackground(Color.white);
      buttonStatus = ButtonStatus.CLICKED;
   }

   public void showAll()
   {
      if (!isBomb() && !isClicked())
      {
         // Marked incorrectly
         if (!isBomb() && buttonStatus == ButtonStatus.MARKED)
         {
            box.setIcon(ImageData.WRONG_CHECK_ICON);
         }
      }
      else if (isBomb() && buttonStatus != ButtonStatus.MARKED)
      {
         box.setIcon(new ImageIcon("images/bomb.gif"));
      }

      box.removeMouseListener(this);
   }

   public void makeBomb()
   {
      bomb = true;
   }

   public void setCheck()
   {
      box.setIcon(ImageData.CHECK_ICON);
   }

   public final boolean isBomb()
   {
      return bomb;
   }

   public final boolean isCleared()
   {
      return buttonStatus == ButtonStatus.CLEARED;
   }

   public final boolean isMarked()
   {
      return buttonStatus == ButtonStatus.MARKED;
   }

   public final boolean isClicked()
   {
      return buttonStatus == ButtonStatus.CLICKED;
   }

   public final boolean isBlank()
   {
      return numAdjBombs == 0 && !isBomb();
   }

   public void setAdjBombs(int adjBombs)
   {
      numAdjBombs = adjBombs;

      box.setFont(new Font("serif",
               Font.BOLD,
               14));

      switch (adjBombs)
      {
         case 1:
            box.setForeground(Color.blue);
            break;
         case 2:
            box.setForeground(Color.red);
            break;
         case 3:
            box.setForeground(Color.magenta);
            break;
         case 4:
            box.setForeground(Color.green);
            break;
         case 5:
            box.setForeground(Color.cyan);
            break;
         case 6:
            box.setForeground(Color.orange);
            break;
         case 7:
            box.setForeground(Color.pink);
            break;
         case 8:
            box.setForeground(Color.gray);
            break;
      }
   }

   public void mouseEntered(MouseEvent e)
   {
   }

   public void mouseExited(MouseEvent e)
   {
   }

   public void mouseReleased(MouseEvent e)
   {
   }

   public void mouseClicked(MouseEvent e)
   {
      if (!game.isSensitive())
      {
         handleMouseClick(e);
      }
   }

   public void mousePressed(MouseEvent e)
   {
      if (e.getModifiers() == MouseEvent.BUTTON1_MASK)
         game.setNewButton(ImageData.SCARED_FACE_ICON);

      if (game.isSensitive())
      {
         handleMouseClick(e);
      }
   }

   private void handleMouseClick(MouseEvent e)
   {
      // Left click Callback
      if (e.getModifiers() == MouseEvent.BUTTON1_MASK)
      {
         handleLeftMouseClick();
      }
      // Middle Click Callback
      if (e.getModifiers() == MouseEvent.BUTTON2_MASK)
      {
         handleMiddleMouseClick();
      }
      // Right click Callback
      if (e.getModifiers() == MouseEvent.BUTTON3_MASK)
      {
         handleRightMouseClick();
      }
   }

   private void handleLeftMouseClick()
   {
      game.startClock();
      // if button is not marked, click it.
      if (buttonStatus != ButtonStatus.MARKED)
      {
         if (!isBomb() && numAdjBombs > 0 && buttonStatus == ButtonStatus.CLICKED)
         {
            game.clickOneBox(xCoordinate,
                     yCoordinate);
         }

         try
         {
            handleLeftClickOnBox();
         }
         catch (Exception e)
         {
         }

         game.win();
      }
      else
         game.setNewButton(ImageData.HAPPY_FACE_ICON);
   }

   private void handleMiddleMouseClick()
   {
      if (game.questionIsEnabled())
      {
         if (buttonStatus == ButtonStatus.MARKED)
         {
            game.incrementBombs();
         }
         buttonStatus = ButtonStatus.TAGGED;
         box.setIcon(ImageData.QUESTION_ICON);
      }
   }

   private void handleRightMouseClick()
   {
      if (buttonStatus == ButtonStatus.MARKED)
      {
         if (game.questionIsEnabled())
         {
            buttonStatus = ButtonStatus.TAGGED;
            box.setIcon(ImageData.QUESTION_ICON);
            game.incrementBombs();
         }
         else
         {
            buttonStatus = ButtonStatus.CLEARED;
            box.setIcon(ImageData.BLANK_ICON);
            game.incrementBombs();
         }
      }
      else if (buttonStatus == ButtonStatus.TAGGED)
      {
         buttonStatus = ButtonStatus.CLEARED;
         box.setIcon(ImageData.BLANK_ICON);
      }
      else if (buttonStatus == ButtonStatus.CLEARED && game.decrementBombs())
      {
         buttonStatus = ButtonStatus.MARKED;
         box.setIcon(ImageData.CHECK_ICON);
         game.win();
      }
   }

   public void handleLeftClickOnBox() throws Exception
   {
      if (isBomb())
      {
         game.showAll();
         game.setNewButton(ImageData.SAD_FACE_ICON);
         box.setIcon(ImageData.BOOM_BOMB_ICON);
         throw new Exception();
      }
      else if (numAdjBombs == 0 && buttonStatus != ButtonStatus.CLICKED)
      {
         removeButton();
         game.setNewButton(ImageData.HAPPY_FACE_ICON);
         game.clearBlankBoxes(xCoordinate,
                  yCoordinate);
      }
      else if (buttonStatus != ButtonStatus.CLICKED)
      {
         game.setNewButton(ImageData.HAPPY_FACE_ICON);
         removeButton();
      }
      else
         game.setNewButton(ImageData.HAPPY_FACE_ICON);

      buttonStatus = ButtonStatus.CLICKED;
   }

}
