/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.apps.progtask;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iesapp.framework.pluggable.Closable;
import org.iesapp.framework.pluggable.Stamp;
import org.iesapp.framework.util.CoreCfg;

/**
 *
 * @author Josep
 */
public class Cfg {
    public static ArrayList<BeanTasca> listTasques;
    public static HashMap<String, String> abrev2prof;
    public static HashMap<String, Number> abrev2sgdID;
    public static HashMap<String, Number> torn2prof;
    public static String mysqldump_path="C:\\Program Files\\MySQL\\MySQL Server 5.1\\bin\\";
    public static String output_path="C:\\";
    private static int id = 0;
    protected final Stamp stamper;
    protected final CoreCfg coreCfg;
    
    
    public Cfg(final CoreCfg coreCfg, final String args[])
    {
        this.coreCfg = coreCfg;
        start();
        stamper = new Stamp();
        stamper.initialize("PROGRAMADOR", "ADMIN", coreCfg);
    }
    
    private void start()
    {
        readIniFile();
        listTasques = new ArrayList<BeanTasca>();
        
       
        if(getCoreCfg().getMysql()==null || getCoreCfg().getMysql().isClosed()) 
        {
            System.out.println("ERROR No hi ha connexió amb el servidor");
            return;
        }
        
        loadTasques();
         
         abrev2prof = new HashMap<String, String>();
         abrev2sgdID = new HashMap<String, Number>();
         torn2prof = new HashMap<String, Number>();

        try{
        //consulta de tot el professorat
        String SQL1 = "SELECT * FROM sig_professorat ORDER BY NOMBRE";
        Statement st = getCoreCfg().getMysql().createStatement();
        ResultSet rs1 = getCoreCfg().getMysql().getResultSet(SQL1,st);
        while( rs1!=null && rs1.next() )
        {
            String nombre =rs1.getString("NOMBRE");
            String abrev = rs1.getString("ABREV");
            int  turno = rs1.getInt("TORN");
            int  sgdID = rs1.getInt("idSGD");

            abrev2prof.put(abrev, nombre);
            abrev2sgdID.put(abrev, sgdID);
            torn2prof.put(abrev, turno);

        }
        if(rs1!=null) {
            rs1.close();
            st.close();
        }
        } catch (SQLException ex) {
                System.out.println("Error llegint la taula sig_professorat :"+ex);
         }

              
    }
    
      private static void readIniFile() {

       
        Properties props = new Properties();
        //try retrieve data from file
        try {
              FileInputStream filestream = new FileInputStream(CoreCfg.contextRoot+File.separator+"config/progtask.ini");
              props.load(filestream);

              mysqldump_path = props.getProperty("mysqldump_path");
              output_path = props.getProperty("output_path");
              
              if(!mysqldump_path.endsWith("\\") && !mysqldump_path.endsWith("/"))
              {
                  mysqldump_path += "\\";
              }
              if(!output_path.endsWith("\\") && !output_path.endsWith("/"))
              {
                  output_path += "\\";
              }
              
              
              filestream.close();
            }
            catch(IOException ex)
            {
                 Logger.getLogger(Cfg.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
    }


    public void loadTasques() {
        
        listTasques = new ArrayList<BeanTasca>();
        String SQL = "SELECT * FROM sig_progtasques";
        try {
              Statement st = getCoreCfg().getMysql().createStatement();
              ResultSet rs = getCoreCfg().getMysql().getResultSet(SQL,st);
                while (rs!=null && rs.next()) {
                    BeanTasca bt = new BeanTasca();
                    bt.setDia(rs.getInt("dia"));
                    bt.setHora(rs.getTime("hora"));
                    bt.setTasca(rs.getString("tipo"));
                    listTasques.add( bt );
                }
                if(rs!=null) {
                 rs.close();
                 st.close();
             }
         } catch (SQLException ex) {
                System.out.println("Error llegint la taula sig_progtasques :"+ex);
         }
           
    }
    
    
   public String isProgramadorRunning()
   {
        String isrun = "";
        String SQL1 = "SELECT * FROM sig_log WHERE fi IS NULL AND tasca='PROGRAMADOR'";
        try {
             Statement st = getCoreCfg().getMysql().createStatement();
             ResultSet rs1 = getCoreCfg().getMysql().getResultSet(SQL1,st);
            if(rs1!=null && rs1.next())
            {
                isrun = rs1.getString("ip")+" / "+rs1.getString("netbios");                
            }
            if(rs1!=null) {
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Cfg.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return isrun;
   }

    public void stampFix() {
        String SQL1 = "UPDATE sig_log SET fi=NOW() WHERE fi IS NULL AND tasca='PROGRAMADOR'";
        getCoreCfg().getMysql().executeUpdate(SQL1);
    }

    public Stamp getStamper() {
        return stamper;
    }

    public CoreCfg getCoreCfg() {
        return coreCfg;
    }
}
