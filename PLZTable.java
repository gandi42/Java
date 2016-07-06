package w5.w5t1.datenbankzugriffe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import util.StatusBar;
import util.WinUtil;
import util.WinUtil.MenuItemType;

public class PLZTable extends JDialog implements WindowListener, ActionListener
{

  private JMenuBar    menuBar;
  private JMenu       menuDatei;
  private JMenuItem   miBeenden;

  private JTable      tabelle;
  private JScrollPane jspTabelle;

  private StatusBar   statusBar;

  private Component   owner;

  public PLZTable()
  {
    initComponents();
  }

  private void initComponents()
  {
    this.setTitle( "Postleitzahlen" );
    this.setBounds( 10, 10, 800, 480 );

    this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    this.setLayout( new BorderLayout() );

    menuBar = new JMenuBar();
    menuDatei = WinUtil.createMenu( menuBar, "Datei", "", 'd' );
    miBeenden = WinUtil.createMenuItem(
        menuDatei, "Schließen", "close", MenuItemType.ITEM_PLAIN,
        this, null, 'e', "" );
    this.setJMenuBar( menuBar );

    tabelle = new JTable();
    tabelle.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

    jspTabelle = new JScrollPane( tabelle );
    jspTabelle.setViewportView( tabelle );
    this.add( jspTabelle );

    statusBar = new StatusBar();
  }

  public void showDialog( Component owner )
  {
    initDialog();
    this.owner = owner;
    setVisible( true );
  }

  private void initDialog()
  {
    showDataThread();
  }

  private void showDataThread()
  {
    Thread t = new Thread( new ShowData() );
    t.start();
  }

  private class ShowData implements Runnable
  {

    @Override
    public void run()
    {
      String sql = "SELECT primarykey AS ID, plz AS Postleitzahl, ort AS Wohnort "
          + "FROM postleitzahlen ORDER BY plz, ort";

      tabelle.setModel( new MyTableModel( sql ) );

      Font font = tabelle.getTableHeader().getFont().deriveFont( Font.BOLD, 14f );
      tabelle.getTableHeader().setFont( font );

      DefaultTableCellRenderer tableHeaderRenderer =
          (DefaultTableCellRenderer) tabelle.getTableHeader().getDefaultRenderer();
      tableHeaderRenderer.setHorizontalAlignment( SwingConstants.LEFT );
      tabelle.getTableHeader().setDefaultRenderer( tableHeaderRenderer );

      tabelle.setRowHeight( 21 );
      tabelle.setIntercellSpacing( new Dimension( 5, 2 ) );

      hideTableColumn( tabelle, 0 );
      setTableColumnWidth( tabelle, 1, 120 );

      if ( tabelle.getRowCount() > 0 ) selectRow( 1 );
      else
      {
        // TODO
      }
    }

  }

  public void hideTableColumn( JTable t, int c )
  {
    TableColumn col = t.getColumnModel().getColumn( c );
    col.setWidth( 0 );
    col.setMinWidth( 0 );
    col.setMaxWidth( 0 );
    col.setPreferredWidth( 0 );
    col.setResizable( false );
    //    col.setCellRenderer( null );
    //    col.setHeaderRenderer( null );
    // t.getColumnModel().removeColumn( c );
  }

  public void selectRow( int row )
  {
    tabelle.changeSelection( row - 1, 0, false, true );
  }

  public void setTableColumnWidth( JTable t, int c, int width )
  {
    TableColumn col = t.getColumnModel().getColumn( c );
    col.setWidth( width );
    col.setMaxWidth( width * 2 );
    col.setPreferredWidth( width );
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {}

  @Override
  public void windowOpened( WindowEvent e )
  {}

  @Override
  public void windowClosing( WindowEvent e )
  {}

  @Override
  public void windowClosed( WindowEvent e )
  {}

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

}