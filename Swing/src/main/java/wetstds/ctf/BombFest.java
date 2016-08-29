//-----------------------------------------------------------------------------
// BombFest.java
//
// Author: Eric Wetmiller
// Release 1.1
//-----------------------------------------------------------------------------
package wetstds.ctf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import wetstds.ctf.data.Defaults;
import wetstds.ctf.data.ImageData;
import wetstds.ctf.data.Maps;

//-----------------------------------------------------------------------------
/*
 * class BombFest implements a version of the Bombfest Game.  This
 *       application was developed solely as a learning tool.  Its purpose is
 *       to demonstrate the capabilities and uses of the swing set and the awt
 *       in the java language version 1.3 (7/2003).
 * 
 *       Updated in 8/2016 for Java 1.8
**/
//-----------------------------------------------------------------------------
public class BombFest extends JFrame implements ActionListener, Runnable
{
    /**
    * 
    */
   private static final long serialVersionUID = -7163049757753906108L;

   public static void main( String args[] )
    {
        new BombFest();
    }

    JMenuBar mBar = new JMenuBar();

    // used for timer.
    private Thread clockThread = null;
    private int secondsSinceStart = 0;

    // First three objects in the main panel.
    private JTextField timeField;
    private JButton newButton;
    private JTextField bombsRemaining;

    // The main panel to hold the bomb boxes and the associated layout
    private JPanel bombPanel = new JPanel( true );

    // Everything in this frame goes into this panel.
    private JPanel mainPanel = new JPanel();

    // All the pull down menu items.
    private JMenuItem newItem = new JMenuItem( "New" );

    private JMenuItem topTimes = new JMenuItem( "Top Times" );
    private JRadioButtonMenuItem sensitivityOn =
        new JRadioButtonMenuItem( "On" );

    private JRadioButtonMenuItem sensitivityOff =
        new JRadioButtonMenuItem( "Off", true );

    private JRadioButtonMenuItem questionMarkOn =
        new JRadioButtonMenuItem( "Enabled", true );

    private JRadioButtonMenuItem questionMarkOff =
        new JRadioButtonMenuItem( "Disabled" );

    private JMenuItem quitItem = new JMenuItem( "Exit" );

    private Difficulty level = Difficulty.BEGINNER;

    private boolean sensitive = false;
    private boolean questionMarkEnabled = true;
    private boolean paused = false;

    private static Map<Difficulty, Defaults> defaults = Stream.of(
                Maps.entry(Difficulty.BEGINNER, new Defaults(9, 9, 10, Difficulty.BEGINNER)),
                Maps.entry(Difficulty.INTERMEDIATE, new Defaults(16, 16, 40, Difficulty.INTERMEDIATE)),
                Maps.entry(Difficulty.EXPERT, new Defaults(30,16, 99, Difficulty.EXPERT)),
                Maps.entry(Difficulty.CUSTOM, new Defaults(9, 9, 10, Difficulty.CUSTOM))).
             collect(Maps.entriesToMap());

    private BombBox bombBoxes[][];

    private JDialog nameWindow = new JDialog( this, "New Best Time", true );
    private JTextField nameField = new JTextField( "" );
    private JButton nameOkay = new JButton( "OK" );

    // These objects are for storing the high scores and the user interface
    //    to retrieve the name of the high scorer.
    private HighScores bestTimes = new HighScores();
    private JDialog scores = new JDialog( this, "Best Times", true );

    private JButton scoresOkay = new JButton( "OK" );

    // These objects are to retrieve info from the user for a custom game.
    private JDialog customWindow = new JDialog( this, "Customize", true );
    private JTextField widthField =
        new JTextField( Integer.toString(defaults.get(Difficulty.CUSTOM).getColumns()), 2 );
    private JTextField heightField =
        new JTextField( Integer.toString(defaults.get(Difficulty.CUSTOM).getRows()), 2 );
    private JTextField bombField =
        new JTextField( Integer.toString(defaults.get(Difficulty.CUSTOM).getNumBombs()), 2 );
    private JButton customOkay = new JButton( "OK" );

    // These are objects to read and write to and from a file to keep track of
    //    the high scores.
    private FileOutputStream ostream;
    private FileInputStream istream;
    private ObjectOutputStream scoreOStream;
    private ObjectInputStream scoreIStream;

    //-------------------------------------------------------------------------
    /**
     * BombFest sets up the game with all of the GUI objects and sets the
     *             initial level to BEINNER.  It sets up the best times and
     *             creates all the buttons and textFields that are needed.
     */
    //-------------------------------------------------------------------------
    public BombFest()
    {
        setUpBestTimes();
        createHighScoreWindow();
        createCustomWindow();

        scores.setResizable( false );
        scores.setSize( 220, 160 );

        nameWindow.setResizable( false );
        nameWindow.setSize( 300, 130 );

        setTitle( "Clear the Field" );
        setResizable( false );

        setLocation( new Point( 500, 250 ) );

        setJMenuBar( mBar );
        initFileMenu();

        getContentPane().add( mainPanel );

        timeField = new JTextField( " " + "000", 3 );
        mainPanel.add( timeField );
        timeField.setEditable(false);
        timeField.setBackground( Color.white );
        timeField.setForeground( Color.blue );
        timeField.setFont( new Font( "serif", Font.BOLD, 18 ) );

        newButton = new JButton( ImageData.HAPPY_FACE_ICON );
        newButton.addActionListener( this );
        mainPanel.add( newButton );

        bombsRemaining = new JTextField( "000", 3 );
        mainPanel.add( bombsRemaining );
        bombsRemaining.setEditable(false);
        bombsRemaining.setBackground( Color.white );
        bombsRemaining.setForeground( Color.blue );
        bombsRemaining.setFont( new Font( "serif", Font.BOLD, 18 ) );

        resetLevel(Difficulty.BEGINNER);

        setVisible( true );

        addWindowListener( 
           new WindowAdapter()
               {
                   public void windowClosing( WindowEvent e )
                   {
                       System.exit( 0 );
                   }

                   public void windowDeiconified( WindowEvent e )
                   {
                       paused = false;
                   }

                   public void windowIconified( WindowEvent e )
                   {
                       paused = true;
                   }
               } );
    } // BombFest()

    //-------------------------------------------------------------------------
    /**
     * resetLevel stops the clock and creates a new bomb panel along with all
     *            needed bombboxes.  Then, this frame is validated and resized.
     *            Finally, all the bombs are randomly inserted and the
     *            adjacent bombs are calculated.
     */
    //-------------------------------------------------------------------------
    public void resetLevel( Difficulty expertise )
    {
        stopClock();
        secondsSinceStart = 0;

        level = expertise;

        int rows = defaults.get(expertise).getRows();
        int columns = defaults.get(expertise).getColumns();
        int numBombs = defaults.get(expertise).getNumBombs();
        setSize( columns*22 + 25, rows*24 + 85 );

        if(numBombs < 100 )
        {
            bombsRemaining.setText( " 0" +
                                    Integer.toString( numBombs ) );
        }
        else
        {
            bombsRemaining.setText( " " +
                                    Integer.toString( numBombs ) );
        }

        timeField.setText( " " + "000" );
        newButton.setIcon( ImageData.HAPPY_FACE_ICON );

        mainPanel.remove( bombPanel );
        synchronized(getTreeLock())
        {
        validateTree();
        }

        bombBoxes = new BombBox[ columns ][ rows ];
        bombPanel = new JPanel( true );
        bombPanel.setLayout( new GridLayout(
              rows, columns ) );

        mainPanel.add( bombPanel );
        bombPanel.setPreferredSize(
              new Dimension( columns*22, rows*23 ) );

        for( int h = 0; h < rows; h++)
        {
            for( int w = 0; w < columns; w++)
            {
                bombBoxes[w][h] = new BombBox(this, w, h);
                bombBoxes[w][h].add( bombPanel );
            }
        }

        scatterBombs();
        calcAdjBombs();
    }

    //-------------------------------------------------------------------------
    /**
     * setUpBestTimes
    **/
    //-------------------------------------------------------------------------
    private void setUpBestTimes()
    {
        try
        {
            try
            {
               String homeDir = System.getProperty("user.home");
               Path scorePath = Paths.get(homeDir, ".ctf", "scores.dat");
               scorePath.getParent().toFile().mkdirs();
               istream = new FileInputStream(scorePath.toFile());
               scoreIStream = new ObjectInputStream( istream );
            }
            catch( FileNotFoundException e )
            {
                bestTimes = new HighScores();
                String homeDir = System.getProperty("user.home");
                Path scorePath = Paths.get(homeDir, ".ctf", "scores.dat");
                ostream = new FileOutputStream( scorePath.toFile() );
                scoreOStream = new ObjectOutputStream( ostream );
                scoreOStream.close();

                istream = new FileInputStream(scorePath.toFile() );
                scoreIStream = new ObjectInputStream( istream );
            }
        }
        catch( IOException e )
        {
            System.exit( 0 );
        }

        try
        {
            bestTimes = (HighScores)scoreIStream.readObject();
            scoreIStream.close();
        }
        catch(ClassNotFoundException e)
        {}
        catch(OptionalDataException e)
        {}
        catch(IOException e)
        {}

        defaults.get(Difficulty.BEGINNER).getScoreLabel().setText(
                 bestTimes.getScore(Difficulty.BEGINNER).getName() + ' ' +
                 bestTimes.getScore(Difficulty.BEGINNER).getTime() );
        defaults.get(Difficulty.INTERMEDIATE).getScoreLabel().setText(
                 bestTimes.getScore(Difficulty.INTERMEDIATE).getName() + ' ' +
                 bestTimes.getScore(Difficulty.INTERMEDIATE).getTime() );
        defaults.get(Difficulty.EXPERT).getScoreLabel().setText(
                 bestTimes.getScore(Difficulty.EXPERT).getName() + ' ' +
                 bestTimes.getScore(Difficulty.EXPERT).getTime() );
    }

    private void createHighScoreWindow()
    {
        JPanel mainScorePanel = new JPanel();
        scores.getContentPane().add( mainScorePanel );
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout( new GridLayout( 4, 2 ) );
        mainScorePanel.add( scorePanel );
        scorePanel.add( new JLabel("Beginner:     ") );
        scorePanel.add( defaults.get(Difficulty.BEGINNER).getScoreLabel() );
        scorePanel.add( new JLabel("Intermediate: ") );
        scorePanel.add( defaults.get(Difficulty.INTERMEDIATE).getScoreLabel() );
        scorePanel.add( new JLabel( "Expert:       ") );
        scorePanel.add( defaults.get(Difficulty.EXPERT).getScoreLabel() );
        JPanel okayPanel = new JPanel();
        okayPanel.add( scoresOkay );
        mainScorePanel.add( okayPanel );
        scoresOkay.addActionListener(this);

        JPanel newWinPanel = new JPanel();
        
        nameWindow.getContentPane().add( newWinPanel );
        newWinPanel.add( new JLabel( "You have a new best time!!!\n Enter your Name." ) );
        newWinPanel.add( nameField );
        newWinPanel.add( nameOkay );
        nameOkay.addActionListener( this );
        nameField.setColumns(7);
    }

    private void createCustomWindow()
    {
        customWindow.setResizable( false );
        customWindow.setSize( 100, 140 );

        JPanel customPanel = new JPanel();
        customWindow.getContentPane().add( customPanel );

        JTextField enterWidth = new JTextField( "Width:" );
        enterWidth.setEditable( false );
        customPanel.add( enterWidth );
        customPanel.add( widthField );

        JTextField enterHeight = new JTextField( "Height:" );
        enterHeight.setEditable( false );
        customPanel.add( enterHeight );
        customPanel.add( heightField );

        JTextField enterBombs = new JTextField( "Bombs:" );
        enterBombs.setEditable( false );
        customPanel.add( enterBombs );
        customPanel.add( bombField );
        customOkay.addActionListener(this);
        customPanel.add( customOkay );

        customWindow.addWindowListener( 
           new WindowAdapter()
               {
                   public void windowClosing( WindowEvent e )
                   {
                       defaults.get(level).getMenuItem().setSelected(true);
                   }
               } );

    }

    //-------------------------------------------------------------------------
    /**
     * startClock
     */
    //-------------------------------------------------------------------------
    public void startClock()
    {
        if (clockThread == null)
        {
            clockThread = new Thread(this, "Clock");
            clockThread.start();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * run
     */
    //-------------------------------------------------------------------------
    public void run()
    {
        Thread myThread = Thread.currentThread();
        while (clockThread == myThread)
        {
            if(!paused)
            {
                if( secondsSinceStart > 999 )
                    secondsSinceStart = 999;
                else if( secondsSinceStart < 10)
                    timeField.setText( " 00" +
                                       Integer.toString( secondsSinceStart++ ) );
                else if(secondsSinceStart >= 10 && secondsSinceStart < 100)
                    timeField.setText( " 0" +
                                       Integer.toString( secondsSinceStart++ ) );
                else if(secondsSinceStart >= 100 && secondsSinceStart < 1000)
                    timeField.setText( " " +
                                       Integer.toString( secondsSinceStart++ ) );
                else
                    timeField.setText( Integer.toString( secondsSinceStart++ ) );
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // the VM doesn't want us to sleep anymore,
                // so get back to work
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * stopClock
     */
    //-------------------------------------------------------------------------
    public void stopClock()
    {
        clockThread = null;
    }

    //-------------------------------------------------------------------------
    /**
     * initFileMenu
     */
    //-------------------------------------------------------------------------
    private void initFileMenu()
    {
        JMenu fileMenu = new JMenu( "File" );
        JMenu optionMenu = new JMenu( "Options" );

        JMenu sensitivityMenu = new JMenu( "Sensitivity" );
        JMenu questionMarkMenu = new JMenu( "Question Mark" );

        mBar.add( fileMenu );
        mBar.add( optionMenu );

        newItem.addActionListener( this );
        fileMenu.add( newItem );

        fileMenu.insertSeparator( 2 );

        ButtonGroup difficultyLevel = new ButtonGroup();
        difficultyLevel.add( defaults.get(Difficulty.BEGINNER).getMenuItem() );
        difficultyLevel.add( defaults.get(Difficulty.INTERMEDIATE).getMenuItem() );
        difficultyLevel.add( defaults.get(Difficulty.EXPERT).getMenuItem() );
        difficultyLevel.add( defaults.get(Difficulty.CUSTOM).getMenuItem() );

        defaults.get(level).getMenuItem().setSelected( true );

        for (Difficulty difficulty : Difficulty.values())
        {
           defaults.get(difficulty).getMenuItem().addActionListener( this );
           optionMenu.add( defaults.get(difficulty).getMenuItem() );
        }

        optionMenu.insertSeparator( Difficulty.values().length );

        optionMenu.add( sensitivityMenu );

        ButtonGroup SenOnOff = new ButtonGroup();
        SenOnOff.add( sensitivityOn );
        SenOnOff.add( sensitivityOff );

        sensitivityOn.addActionListener( this );
        sensitivityOff.addActionListener( this );
        sensitivityMenu.add( sensitivityOn );
        sensitivityMenu.add( sensitivityOff );

        optionMenu.add( questionMarkMenu );

        ButtonGroup questionOnOff = new ButtonGroup();
        questionOnOff.add( questionMarkOn );
        questionOnOff.add( questionMarkOff );

        questionMarkOn.addActionListener( this );
        questionMarkOff.addActionListener( this );
        questionMarkMenu.add( questionMarkOn );
        questionMarkMenu.add( questionMarkOff );

        topTimes.addActionListener( this );
        fileMenu.add( topTimes );

        fileMenu.insertSeparator( 3 );

        quitItem.addActionListener( this );
        fileMenu.add( quitItem );
    }

    //-------------------------------------------------------------------------
    /**
     * actionPerformed
     */
    //-------------------------------------------------------------------------
    public void actionPerformed( ActionEvent e )
    {
        AbstractButton buttonClicked = (AbstractButton)e.getSource();

        for (Difficulty difficulty : Difficulty.values())
        {
           Defaults d = defaults.get(difficulty);
           if (difficulty == Difficulty.CUSTOM &&
               buttonClicked == d.getMenuItem())
           {
              customWindow.setLocation( getLocation() );
              customWindow.setVisible( true );
           }
           else if (buttonClicked == d.getMenuItem())
           {
              resetLevel(difficulty);
           }
        }

        if( buttonClicked == customOkay )
        {
            resetToCustomConfiguration();
        }
        else if( buttonClicked == newButton ||
                 buttonClicked == newItem )
        {
            resetLevel(level);
        }
        else if( buttonClicked == quitItem )
        {
            System.exit( 0 );
        }
        else if( buttonClicked == topTimes )
        {
            scores.setLocation( getLocation() );
            scores.setVisible( true );
        }
        else if( buttonClicked == scoresOkay )
        {
            scores.setVisible( false );
        }
        else if( buttonClicked == nameOkay )
        {
            resetBestTime();
        }
        else if( buttonClicked == sensitivityOn )
        {
            sensitive = true;
        }
        else if( buttonClicked == sensitivityOff )
        {
            sensitive = false;
        }
        else if( buttonClicked == questionMarkOn )
        {
            questionMarkEnabled = true;
        }
        else if( buttonClicked == questionMarkOff )
        {
            questionMarkEnabled = false;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * 
    **/
    //-------------------------------------------------------------------------
    private void writeBestTime()
    {
        try
        {
            String homeDir = System.getProperty("user.home");
            Path ctfPath = Paths.get(homeDir, ".ctf");
            Files.createDirectories(ctfPath);
            Path scorePath = Paths.get(homeDir, ".ctf", "scores.dat");
            ostream = new FileOutputStream(scorePath.toFile());
            scoreOStream = new ObjectOutputStream( ostream );

            scoreOStream.writeObject( bestTimes );
            scoreOStream.flush();
            scoreOStream.close();
        }
        catch(IOException IOE){
           IOE.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * 
    **/
    //-------------------------------------------------------------------------
    private void resetBestTime()
    {
        HighScores.HighScore score =
                 bestTimes.setHighScore(nameField.getText(), level, Integer.valueOf(timeField.getText().trim()));

        defaults.get(level).getScoreLabel().setText( score.getName() + ' ' +
                                                     score.getTime() );

        nameWindow.setVisible( false );

        writeBestTime();
    }

    //-------------------------------------------------------------------------
    /**
     * 
    **/
    //-------------------------------------------------------------------------
    private void resetToCustomConfiguration()
    {
        try
        {
            int customWidth = Integer.parseInt(widthField.getText().trim());
            int customHeight = Integer.parseInt(heightField.getText().trim());
            int customNumBombs = Integer.parseInt(bombField.getText().trim());

            if( customWidth > 0  && customWidth <= 50 &&
                customHeight > 0 && customHeight <= 35 &&
                customNumBombs > 0 &&
                customNumBombs < customWidth*customHeight )
            {
                defaults.get(Difficulty.CUSTOM).setColumns(customWidth);
                defaults.get(Difficulty.CUSTOM).setRows(customHeight);
                defaults.get(Difficulty.CUSTOM).setNumBombs(customNumBombs);

                customWindow.setVisible( false );

                resetLevel(Difficulty.CUSTOM);
            }
        }
        catch( NumberFormatException NE )
        {}
    }

    //-------------------------------------------------------------------------
    /**
     * 
    **/
    //-------------------------------------------------------------------------
    public boolean isSensitive()
    {
        return sensitive;
    }

    //-------------------------------------------------------------------------
    /**
     * getListOfBombs
     */
    //-------------------------------------------------------------------------
    private void scatterBombs()
    {
        Defaults d = defaults.get(level);
        for( int i = 0; i < d.getNumBombs(); i++ )
        {
            Integer widthRandNum = new Integer(
                 (int)((double)(d.getColumns() * Math.random()) ) );

            Integer heightRandNum = new Integer(
                 (int)((double)(d.getRows() * Math.random()) ) );

            while( bombBoxes[widthRandNum.intValue() ]
                            [heightRandNum.intValue()].isBomb() )
            {
                widthRandNum = new Integer(
                   (int)((double)(d.getColumns() * Math.random()) ) );

                heightRandNum = new Integer(
                   (int)((double)(d.getRows() * Math.random()) ) );
            }

            bombBoxes[widthRandNum.intValue() ]
                     [heightRandNum.intValue()].makeBomb();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * calcAdjBombs
     */
    //-------------------------------------------------------------------------
    public void calcAdjBombs()
    {
        Defaults d = defaults.get(level);
        for( int h = 0; h < d.getRows(); h++ )
        {
            for( int w = 0; w < d.getColumns(); w++ )
            {
                int numBombs = 0;
                if( !bombBoxes[w][h].isBomb() )
                {
                    if( (w - 1) >= 0 &&
                        ( h - 1 ) >= 0 &&
                        bombBoxes[w - 1][h - 1].isBomb() )
                        numBombs++;
                    if( ( h - 1 ) >= 0 && bombBoxes[w][h - 1].isBomb() )
                        numBombs++;
                    if( (w + 1) < d.getColumns() &&
                        ( h - 1 ) >= 0 &&
                        bombBoxes[w+1][h - 1].isBomb())
                        numBombs++;
                    if( (w + 1) < d.getColumns() &&
                        bombBoxes[w+1][h].isBomb())
                        numBombs++;
                    if( (w + 1) < d.getColumns() &&
                        ( h + 1 ) < d.getRows() &&
                        bombBoxes[w+1][h+1].isBomb())
                        numBombs++;
                    if( ( h + 1 ) < d.getRows() &&
                        bombBoxes[w][h+1].isBomb())
                        numBombs++;
                    if( (w - 1) >= 0 &&
                        ( h + 1 ) < d.getRows() &&
                        bombBoxes[w-1][h+1].isBomb())
                        numBombs++;
                    if( (w - 1) >= 0 &&
                        bombBoxes[w-1][h].isBomb())
                        numBombs++;
                }

                bombBoxes[w][h].setAdjBombs( numBombs );
            }
        }
    }

    public void clickOneBox(int w, int h)
    {
        try
        {
            Defaults d = defaults.get(level);
            if( (w - 1) >= 0 &&
                ( h - 1 ) >= 0 && !bombBoxes[w - 1][h - 1].isMarked() )
                bombBoxes[w - 1][h - 1].handleLeftClickOnBox();

            if( ( h - 1 ) >= 0 &&
                !bombBoxes[w][h - 1].isMarked() )
                bombBoxes[w][h - 1].handleLeftClickOnBox();

            if( (w + 1) < d.getColumns() &&
                ( h - 1 ) >= 0 &&
                !bombBoxes[w+1][h - 1].isMarked() )
                bombBoxes[w+1][h - 1].handleLeftClickOnBox();

            if( (w + 1) < d.getColumns() &&
                !bombBoxes[w+1][h].isMarked() )
                bombBoxes[w+1][h].handleLeftClickOnBox();

            if( (w + 1) < d.getColumns() &&
                ( h + 1 ) < d.getRows() &&
                !bombBoxes[w+1][h+1].isMarked() )
                bombBoxes[w+1][h+1].handleLeftClickOnBox();

            if( ( h + 1 ) < d.getRows() &&
                !bombBoxes[w][h+1].isMarked() )
                bombBoxes[w][h+1].handleLeftClickOnBox();

            if( (w - 1) >= 0 &&
                ( h + 1 ) < d.getRows() &&
                !bombBoxes[w-1][h+1].isMarked() )
                bombBoxes[w-1][h+1].handleLeftClickOnBox();

            if( (w - 1) >= 0  &&
                !bombBoxes[w-1][h].isMarked() )
                bombBoxes[w-1][h].handleLeftClickOnBox();
        }
        catch( Exception e )
        {}
        win();
    }

    //-------------------------------------------------------------------------
    /**
     * clearBlankBoxes
     */
    //-------------------------------------------------------------------------
    public void clearBlankBoxes( int x, int y )
    {
        helpRemoveBlankButton( x+1, y   );
        helpRemoveBlankButton( x-1, y   );

        helpRemoveBlankButton( x  , y-1 );
        helpRemoveBlankButton( x+1, y-1 );
        helpRemoveBlankButton( x-1, y-1 );

        helpRemoveBlankButton( x  , y+1 );
        helpRemoveBlankButton( x+1, y+1 );
        helpRemoveBlankButton( x-1, y+1 );
    }

    //-------------------------------------------------------------------------
    /**
     * helpRemoveBlankButton
     */
    //-------------------------------------------------------------------------
    private void helpRemoveBlankButton( int x, int y )
    {
        Defaults d = defaults.get(level);
        if( x >= 0 && x < d.getColumns() &&
            y >= 0 && y < d.getRows() )
        {

            if( bombBoxes[x][y].isBlank() &&
                !bombBoxes[x][y].isClicked() )
            {
                bombBoxes[x][y].removeButton();
                clearBlankBoxes( x, y );
            }
            else
                bombBoxes[x][y].removeButton();

        }
    }

    //-------------------------------------------------------------------------
    /**
     * win
     */
    //-------------------------------------------------------------------------
    public void win()
    {
        boolean result = true;
        Defaults d = defaults.get(level);
        for( int h = 0; h < d.getRows(); h++ )
        {
            for( int w = 0; w < d.getColumns(); w++ )
            {
                if( !bombBoxes[w][h].isClicked() &&
                    !bombBoxes[w][h].isBomb() )
                    result = false;
            }
        }

        if( result )
        {
            stopClock();

            bombsRemaining.setText( "000" );

            newButton.setIcon( ImageData.WIN_FACE_ICON );
            for( int h = 0; h < d.getRows(); h++ )
            {
                for( int w = 0; w < d.getColumns(); w++ )
                {
                    bombBoxes[w][h].winGame();
                    if( bombBoxes[w][h].isBomb() &&
                        !bombBoxes[w][h].isMarked() )
                        bombBoxes[w][h].setCheck();
                }
            }

            if( level != Difficulty.CUSTOM &&
                Integer.valueOf(timeField.getText().trim()) < bestTimes.getScore(level).getTime() )
            {
                calcNewBestTime();
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * calcNewBestTime
     */
    //-------------------------------------------------------------------------
    private void calcNewBestTime()
    {
        nameField.setText( bestTimes.getScore(level).getName() );

        nameWindow.setLocation( getLocation() );
        nameWindow.setVisible( true );
    }

    //-------------------------------------------------------------------------
    /**
     * decrementBombs
     */
    //-------------------------------------------------------------------------
    public boolean decrementBombs()
    {
        int bombs = Integer.parseInt(bombsRemaining.getText().trim());
        if( bombs > 0 )
        {
            bombsRemaining.setText(Integer.toString( bombs - 1));
            return true;
        }
        else
            return false;
    }

    //-------------------------------------------------------------------------
    /**
     * incrementBombs
     */
    //-------------------------------------------------------------------------
    public void incrementBombs()
    {
        int bombs = Integer.parseInt(bombsRemaining.getText().trim());
        bombsRemaining.setText(Integer.toString( bombs + 1 ));
    }

    //-------------------------------------------------------------------------
    /**
     * showAll
     */
    //-------------------------------------------------------------------------
    public void showAll()
    {
        Defaults d = defaults.get(level);
        for( int h = 0; h < d.getRows(); h++ )
        {
            for( int w = 0; w < d.getColumns(); w++ )
                bombBoxes[w][h].showAll();
        }
        stopClock();
    }

    //-------------------------------------------------------------------------
    /**
     * setNewButton
     */
    //-------------------------------------------------------------------------
    public void setNewButton( ImageIcon icon )
    {
        newButton.setIcon( icon );
    }

    public boolean questionIsEnabled()
    {
        return questionMarkEnabled;
    }
} // BombFest
