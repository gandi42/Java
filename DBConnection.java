package w5.w5t1.datenbankzugriffe;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

public abstract class DBConnection
{

  private static Connection dbConn;
  private static String     connectionString;

  private static Component  parent = null;

  private DBConnection()
  {}

  public static boolean connectToDatabase( String classForName, String connectionString, String username,
      String password )
  {
    boolean retValue = false;
    DBConnection.connectionString = null;

    try
    {
      Class.forName( classForName ); // Treiber bei Java anmelden (registrieren)

      //  alternativ:
      //      Driver dbDriver = new com.mysql.jdbc.Driver();
      //      DriverManager.registerDriver( dbDriver );

      dbConn = DriverManager.getConnection( connectionString, username, password );
      DBConnection.connectionString = connectionString;

      retValue = true;
    }
    catch ( Exception e )
    {
      DBConnection.dbConn = null;
      DBConnection.connectionString = null;

      JOptionPane.showMessageDialog(
          DBConnection.parent,
          "Fehler bei Datenbankanmeldung",
          "DB-ERROR",
          JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  public static void closeConnection()
  {
    if ( DBConnection.dbConn == null )
      return;

    try
    {
      DBConnection.dbConn.close();
    }
    catch ( Exception e )
    {
      JOptionPane.showMessageDialog(
          DBConnection.parent,
          "Fehler beim Schließen der Datenbankverbindung: " + e.getMessage(),
          "DB-ERROR",
          JOptionPane.ERROR_MESSAGE );
    }

    DBConnection.dbConn = null;
    DBConnection.connectionString = null;
  }

  public static String getCatalog()
  {
    String retValue = "";

    if ( DBConnection.dbConn == null )
      return retValue;

    try
    {
      retValue = DBConnection.dbConn.getCatalog();
    }
    catch ( SQLException e )
    {
      JOptionPane.showMessageDialog(
          DBConnection.parent,
          "Fehler bei Abfrage des Datenbankkatalogs: " + e.getMessage(),
          "DB-ERROR",
          JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  public static int executeNonQuery( String sql )
  {
    int retValue = 0;
    Statement stmt;

    if ( DBConnection.dbConn == null )
      return retValue;

    try
    {
      stmt = dbConn.createStatement();
      retValue = stmt.executeUpdate( sql );
      stmt.close();
    }
    catch ( Exception e )
    {
      JOptionPane.showMessageDialog(
          parent, "Fehler beim Ausführen des DML " + sql + ": " + e.getMessage(),
          "DB-ERROR", JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  public static Object executeScalar( String sql )
  {
    Object retValue = null;
    Statement stmt;

    if ( DBConnection.dbConn == null )
      return retValue;

    try
    {
      stmt = dbConn.createStatement();
      ResultSet res = stmt.executeQuery( sql );

      if ( res != null && !res.wasNull() && res.next() )
        retValue = res.getObject( 1 );
      res.close();
      stmt.close();
    }
    catch ( Exception e )
    {
      JOptionPane.showMessageDialog(
          parent, "Fehler beim Ausführen von " + sql + ": " + e.getMessage(),
          "DB-ERROR", JOptionPane.ERROR_MESSAGE );
    }

    return retValue;
  }

  public static ResultSet executeQuery( String sql )
  {
    ResultSet resultSet = null;
    Statement stmt;

    if ( DBConnection.dbConn == null )
      return resultSet;

    try
    {
      stmt = dbConn.createStatement();
      resultSet = stmt.executeQuery( sql );
    }
    catch ( Exception e )
    {
      JOptionPane.showMessageDialog(
          parent, "Fehler beim Ausführen der Abfrage " + sql + ": " + e.getMessage(),
          "DB-ERROR", JOptionPane.ERROR_MESSAGE );
    }

    return resultSet;
  }

  public static void setParent( Component parent )
  {
    DBConnection.parent = parent;
  }

  public static Component getParent()
  {
    return parent;
  }

}
