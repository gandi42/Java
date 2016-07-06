package w5.w5t1.datenbankzugriffe;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel
{

  private int               anzahlZeilen;
  private int               anzahlSpalten;
  private ArrayList<String> columnNames;
  private Object[][]        data;

  private String            sql;

  public MyTableModel( String sql )
  {
    this.sql = sql;

    // Ausführen des SQL-Statements und entgegennehmen der Resultate
    //
    ResultSet rSet = DBConnection.executeQuery( sql );

    if ( rSet == null )
      return;

    // Lesen der Metadata aus dem ResultSet
    //
    ResultSetMetaData rsMetaData = getMetaData( rSet );

    // Anzahl der Spalten aus den MetaData auslesen
    //
    anzahlSpalten = getColumnCount( rsMetaData );

    // Auslesen der Zeilen aus dem ResultSet (!)
    //
    anzahlZeilen = getRowCount( rSet );

    // Überschriften der Spalten aus den MetaDaten
    //
    setHeader( rsMetaData );

    // Alles ausgelesenen Daten von der
    // Datenbank in das 2-Dim-Array data schreiben
    //
    getData( rSet );

  }

  private ResultSetMetaData getMetaData( ResultSet rSet )
  {
    ResultSetMetaData rsMD = null;

    try
    {
      rsMD = rSet.getMetaData();
    }
    catch ( SQLException ex )
    {
      JOptionPane.showMessageDialog(
          null, "getMetaData: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }

    return rsMD;
  }

  private int getColumnCount( ResultSetMetaData rsMetaData )
  {
    int retValue = 0;

    try
    {
      retValue = rsMetaData.getColumnCount();
    }
    catch ( SQLException ex )
    {
      JOptionPane.showMessageDialog(
          null, "getColumnCount: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  private int getRowCount( ResultSet rs )
  {
    int retValue = 0;

    try
    {
      // Zum letzten Datensatz springen
      rs.last();

      // RowId auslesen = anzahlZeilen
      retValue = rs.getRow();

      // den Zeiger vor den ersten Datensatz stellen
      //
      // ACHTUNG! Das könnte bei SQLite schief gehen !!!
      //
      rs.beforeFirst();
    }
    catch ( Exception ex )
    {
      JOptionPane.showMessageDialog(
          null, "getRowCount: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  private int getRowCountBySQL( String tabellenName )
  {
    String sql = "SELECT COUNT(*) FROM " + tabellenName;
    Object o = DBConnection.executeScalar( sql );

    return Integer.parseInt( o.toString() );
  }

  private void setHeader( ResultSetMetaData rsMetaData )
  {
    columnNames = new ArrayList<String>();

    for( int column = 1; column <= anzahlSpalten; column++ )
      columnNames.add( getColumnLabel( rsMetaData, column ) );
  }

  // Rückgabe der Aliases ( selbst gewählte Spaltennamen in der SQL-Anweisung )
  private String getColumnLabel( ResultSetMetaData rsMetaData, int column )
  {
    String colLabel = "";

    try
    {
      colLabel = rsMetaData.getColumnLabel( column );
    }
    catch ( Exception ex )
    {
      JOptionPane.showMessageDialog(
          null, "getColumLabel: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }

    return colLabel;
  }

  // Rückgabe des 'echten' Feldnamen
  private String getColumnName( ResultSetMetaData rsMetaData, int column )
  {
    String colName = "";

    try
    {
      colName = rsMetaData.getColumnName( column );
    }
    catch ( Exception ex )
    {
      JOptionPane.showMessageDialog(
          null, "getColumnName: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }

    return colName;
  }

  private void getData( ResultSet rSet )
  {
    data = new Object[anzahlZeilen][anzahlSpalten];

    try
    {

      for( int zeile = 1; zeile <= anzahlZeilen; zeile++ )
      {

        rSet.next();

        for( int spalte = 1; spalte <= anzahlSpalten; spalte++ )
        {
          data[zeile - 1][spalte - 1] = rSet.getObject( spalte );
        }

      }

    }
    catch ( Exception ex )
    {
      JOptionPane.showMessageDialog(
          null, "getData: " + ex.getMessage(),
          "Fehler", JOptionPane.ERROR_MESSAGE );
    }
  }

  @Override
  public int getRowCount()
  {
    return anzahlZeilen;
  }

  @Override
  public int getColumnCount()
  {
    return anzahlSpalten;
  }

  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    return data[rowIndex][columnIndex];
  }

  // Keine Abstrakte Methode! Diese überschreiben wir manuell
  //
  @Override
  public String getColumnName( int column )
  {
    return columnNames.get( column );
  }

}
