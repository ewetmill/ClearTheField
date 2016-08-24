package wetstds.ctf.data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageData
{
    public static final ImageIcon HAPPY_FACE_ICON = createIcon("/images/happyFace.gif");
    public static final ImageIcon WIN_FACE_ICON = createIcon("/images/winFace.gif");
    public static final ImageIcon SAD_FACE_ICON = createIcon("/images/sadFace.gif");
    public static final ImageIcon SCARED_FACE_ICON = createIcon("/images/scaredFace.gif");
    public static final ImageIcon CHECK_ICON = createIcon("/images/check.gif");
    public static final ImageIcon WRONG_CHECK_ICON = createIcon("/images/wrongCheck.gif");
    public static final ImageIcon QUESTION_ICON = createIcon("/images/question.gif");
    public static final ImageIcon BLANK_ICON = createIcon("/images/none.gif");
    public static final ImageIcon BOMB_ICON = createIcon("/images/bomb.gif");
    public static final ImageIcon BOOM_BOMB_ICON = createIcon("/images/boomBomb.gif");

    private static ImageIcon createIcon(String path)
    {
       ImageIcon result = null;
       BufferedImage image;
       try
       {
          image = ImageIO.read(ImageData.class.getResourceAsStream(path));
          if (image != null)
             result = new ImageIcon(image);
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
       return result;
    }
}
