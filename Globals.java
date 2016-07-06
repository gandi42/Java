package w5.w5t1.datenbankzugriffe;

public class Globals
{
  private Globals()
  {}

  public static String quote( String what )
  {
    return "'" + what + "'";
  }

  public static boolean insertPLZ( long pk, String plz, String ort )
  {
    String sql = "INSERT INTO postleitzahlen ("
        + "primarykey, plz, ort"
        + ") VALUES ("
        + pk + ", "
        + quote( plz ) + ", "
        + quote( ort )
        + ")";

    return DBConnection.executeNonQuery( sql ) > 0;
  }

  public static boolean istPLZOrtVorhanden( String plz, String ort )
  {
    String sql = "SELECT 0 FROM postleitzahlen "
        + "WHERE plz = " + quote( plz )
        + "  AND ort = " + quote( ort );

    Object obj = DBConnection.executeScalar( sql );
    if ( obj != null ) return true;
    else return false;
  }

  public static long getNextKey()
  {
    String sql = "SELECT MAX(primarykey) FROM postleitzahlen ";
    Object resultSet = DBConnection.executeScalar( sql );

    if ( resultSet != null )
    //      return ((int) resultSet) + 1;
    return ( (Number) resultSet ).longValue() + 1;
    else return 1;
  }
}
