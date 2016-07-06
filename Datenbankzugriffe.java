package w5.w5t1.datenbankzugriffe;

// mysql.de/downloads/connector/j/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.StatusBar;
import util.WinUtil;
import util.WinUtil.MenuItemType;

public class Datenbankzugriffe extends JFrame implements WindowListener, ActionListener
{

  private File         fcFile = new File( "/eclipseLuna/workspace/kurs/src" );

  private JMenuBar     menuBar;
  private JMenu        menuDatei, menuStammdaten, menuExtras;
  private JMenuItem    miBeenden, miPostleitzahlen, miPostleitzahlenImportieren;

  private JLabel       lblSearch;
  private JTextField   tfSearch;

  private StatusBar    statusBar;
  private JProgressBar progressBar;

  private Thread       tImport;

  public static void main( String[] args )
  {
    Datenbankzugriffe dz = new Datenbankzugriffe();
    dz.showFrame();
  }

  public Datenbankzugriffe()
  {
    initComponents();
  }

  private void initComponents()
  {
    this.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    this.addWindowListener( this );

    this.setTitle( "Datenbanktool" );
    this.setSize( 800, 480 );

    this.setLayout( new BorderLayout() );

    menuBar = new JMenuBar();
    this.setJMenuBar( menuBar );

    menuDatei = WinUtil.createMenu( menuBar, "Datei", "file", KeyEvent.VK_D );
    miBeenden = WinUtil.createMenuItem(
        menuDatei, "Beenden", "exit", MenuItemType.ITEM_PLAIN, this, null, 'e',
        "Von der DB trennen und das Programm beenden" );

    menuStammdaten = WinUtil.createMenu( menuBar, "Stammdaten", "masterdata", KeyEvent.VK_S );
    miPostleitzahlen = WinUtil.createMenuItem(
        menuStammdaten, "Postleitzahlen anzeigen", "showplz", MenuItemType.ITEM_PLAIN, this, null, 's',
        "" );

    menuExtras = WinUtil.createMenu( menuBar, "Extras", "extras", KeyEvent.VK_X );
    miPostleitzahlenImportieren = WinUtil.createMenuItem(
        menuExtras, "Postleitzahlen importieren", "import", MenuItemType.ITEM_PLAIN, this, null, 'i',
        "Importieren (neuer) Postleitzahlen" );

    //    lblSearch = new JLabel( "Suche" );
    //    menuBar.add( lblSearch );
    //
    //    tfSearch = new JTextField();
    //    //    tfSearch.setSize( 80, 20 );
    //    menuBar.add( tfSearch );

    statusBar = new StatusBar( "Meine Statuszeile" );
    this.add( statusBar, BorderLayout.PAGE_END );

    progressBar = new JProgressBar( JProgressBar.HORIZONTAL );
    progressBar.setMinimum( 0 );
    progressBar.setPreferredSize( new Dimension( 300, 25 ) );
    progressBar.setForeground( Color.GREEN ); // Balkenfarbe
    progressBar.setStringPainted( true ); // Prozente anzeigen
    progressBar.setVisible( false );
    statusBar.add( progressBar, BorderLayout.LINE_END );
  }

  private void showFrame()
  {
    initFrame();
    setVisible( true );
    setLocationRelativeTo( null );
  }

  private void initFrame()
  {
    openMySQLDatabase();
  }

  private void openMySQLDatabase()
  {
    String connectionString, classForName;

    String server = "localhost";
    server = "127.0.0.1";

    String database = "alfatraining";

    classForName = "com.mysql.jdbc.Driver";
    connectionString = "jdbc:mysql://" + server + ":3306/" + database;

    statusBar.setText( connectionString );

    DBConnection.setParent( this );

    dbEnabled( DBConnection.connectToDatabase( classForName, connectionString, "root", null ) );
  }

  private void dbEnabled( boolean b )
  {
    menuStammdaten.setEnabled( b );
    menuExtras.setEnabled( b );

    if ( b ) statusBar.setText( "Datenbank verbunden: " + DBConnection.getCatalog() );
    else statusBar.setText( "Keine Datenbankverbindung" );
  }

  private void openFileDialog()
  {
    JFileChooser fc = new JFileChooser();
    fc.setCurrentDirectory( fcFile );

    fc.setFileFilter( new FileNameExtensionFilter( "Textdateien (*.txt)", "txt" ) );
    fc.addChoosableFileFilter( new FileNameExtensionFilter( "CSV-Dateien (*.csv)", "csv" ) );

    if ( fc.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION )
      return;

    fcFile = fc.getSelectedFile();

    // File[] files = fc.getSelectedFiles();

    Object[] options = { "Ja", "Nein" };
    int op = JOptionPane.showOptionDialog( this,
        "Sollen alte Daten aus der Tabelle gelöscht werden?",
        "PLZ Import",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null, options, options[1] );

    if ( op == JOptionPane.YES_OPTION )
      deletePLZEntries();

    readFile( fcFile.toString() );
  }

  private void deletePLZEntries()
  {
    DBConnection.executeNonQuery( "DELETE FROM postleitzahlen" );
  }

  private void readFile( String dateiname )
  {
    tImport = new Thread( new ReadFileIntoDatabase( dateiname ) );
    tImport.start();
  }

  private void showPLZTable()
  {
    PLZTable plzTable = new PLZTable();
    plzTable.showDialog( this );
  }

  private class ReadFileIntoDatabase implements Runnable
  {
    String   filename;
    Scanner  scanner       = null;
    String   line;
    String[] split;

    String   oldText;

    int      linesRead     = 0;
    int      linesInserted = 0;

    long     lngPK         = 1;

    public ReadFileIntoDatabase( String filename )
    {
      this.filename = filename;
    }

    @Override
    public void run()
    {
      oldText = statusBar.getText();

      // Menüs deaktivieren
      for( int i = 0; i < menuBar.getMenuCount(); i++ )
        menuBar.getMenu( i ).setEnabled( false );

      lngPK = Globals.getNextKey();

      progressBar.setMaximum( (int) new File( this.filename ).length() );
      progressBar.setVisible( true );

      try
      {
        scanner = new Scanner( new FileInputStream( this.filename ) );

        while ( scanner.hasNext() )
        {
          line = scanner.nextLine();
          line = line.replaceAll( "'", "''" );
          split = line.split( ";", 2 );
          linesRead++;

          if ( linesRead % 100 == 0 )
          {
            statusBar.setText(
                String.format( "Datensätze werden gelesen... [%s]",
                    NumberFormat.getInstance().format( linesRead ) ) );
            statusBar.paintImmediately( statusBar.getBounds() );
          }

          progressBar.setValue( progressBar.getValue() + line.length() + System.lineSeparator().length() );

          if ( split.length >= 2 )
          {
            if ( Globals.istPLZOrtVorhanden( split[0], split[1] ) )
              continue;

            if ( Globals.insertPLZ( lngPK, split[0], split[1] ) )
            {
              lngPK++;
              linesInserted++;
            }
            else JOptionPane.showMessageDialog(
                Datenbankzugriffe.this,
                String.format( "Postleitzahl %s konnte nicht importiert werden", split[0] ),
                "Import",
                JOptionPane.ERROR_MESSAGE );
          }
        }
      }
      catch ( FileNotFoundException e )
      {
        JOptionPane.showMessageDialog(
            Datenbankzugriffe.this,
            "Fehler beim Import der Postleitzahlen: " + e.getMessage(),
            "Import",
            JOptionPane.ERROR_MESSAGE );
      }

      if ( scanner != null )
        scanner.close();

      JOptionPane.showMessageDialog(
          Datenbankzugriffe.this,
          String.format( "Von %d gelesenen Zeilen wurden\n%d Datensätze in die Tabelle importiert", linesRead,
              linesInserted ),
              "Import erfolgreich",
              JOptionPane.INFORMATION_MESSAGE );

      progressBar.setVisible( false );

      // Menüs wieder aktivieren
      for( int i = 0; i < menuBar.getMenuCount(); i++ )
        menuBar.getMenu( i ).setEnabled( true );

      statusBar.setText( oldText );
    }
  }

  /*
   * WindowListener
   */
  @Override
  public void windowOpened( WindowEvent e )
  {}

  @Override
  public void windowClosing( WindowEvent e )
  {
    // TODO Ggf. Anwendereingaben speichern
    if ( tImport != null && tImport.isAlive() )
    {
      Object[] options = { "Ja", "Nein" };
      int op = JOptionPane.showOptionDialog( this,
          "Solle der laufende Import abgebrochen und das Programm beendet werden?",
          "Programm beenden",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null, options, options[2] );
      if ( op == JOptionPane.YES_OPTION )
      {
        tImport.interrupt();
        dispose();
      }
    }
    else dispose();
  }

  @Override
  public void windowClosed( WindowEvent e )
  {
    DBConnection.closeConnection();
  }

  @Override
  public void windowIconified( WindowEvent e )
  {}

  @Override
  public void windowDeiconified( WindowEvent e )
  {}

  @Override
  public void windowActivated( WindowEvent e )
  {}

  @Override
  public void windowDeactivated( WindowEvent e )
  {}

  /*
   * ActionListener
   */
  @Override
  public void actionPerformed( ActionEvent e )
  {
    if ( e.getSource() == miBeenden ) windowClosing( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
    else if ( e.getSource() == miPostleitzahlenImportieren ) openFileDialog();
    else if ( e.getSource() == miPostleitzahlen ) showPLZTable();
  }

}
