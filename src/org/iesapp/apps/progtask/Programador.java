/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.apps.progtask;


 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.iesapp.clients.iesdigital.fitxes.SGDImporter;
import org.iesapp.clients.sgd7.mensajes.MensajesProfesores;
import org.iesapp.framework.db.Lleu2Hist;
import org.iesapp.framework.util.CoreCfg;
import org.iesapp.util.DataCtrl;
import org.iesapp.util.StringUtils;

/**
 *
 * @author Josep
 */
public class Programador {
    private final Cfg cfg;
    
    public Programador(Cfg cfg)
    {
       this.cfg = cfg;
    }
    
     public void check()
     {
            DataCtrl cd = new DataCtrl();
            int intDiaSetmana = cd.getIntDia();
         
            //agafa l'hora actual
            String hora = cd.getHoraReduida();
            for(int i=0; i<Cfg.listTasques.size(); i++)
            {
                int dia = Cfg.listTasques.get(i).getDia();
                java.sql.Time st = Cfg.listTasques.get(i).getHora();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");           
                String horaProgramada = formatter.format(st);
                String tasca = Cfg.listTasques.get(i).getTasca();
                
                //System.out.println("compara"+horaProgramada+"hora"+hora);
                
                if( ( (dia==0 &&  intDiaSetmana<6) || dia == intDiaSetmana) && (hora.equals(horaProgramada)) )
                {
                    if(tasca.equalsIgnoreCase("IMPORTSGD-FITXES"))
                    {
                             doTaskImportSGD();    
                    }
                    else if(tasca.equalsIgnoreCase("PRESENCIA"))
                    {     
                            doTaskPresencia();
                    }
                    else if(tasca.equalsIgnoreCase("BACKUP"))
                    {
                            backup("fitxes");
                    }
                    else if(tasca.equalsIgnoreCase("BACKUPSGD"))
                    {
                            backup("sgd");
                    }
                    else if(tasca.equalsIgnoreCase("LLEU2HIST"))
                    {
                            doLleu2hist();
                    }
                    else if(tasca.equalsIgnoreCase("REMAINDERS"))
                    {
                            doRemainders();
                    }
                    else if(tasca.equalsIgnoreCase("SGDFALTASCHECK-ALL"))
                    {
                           // doSgdFaltasCheckAll();
                    }
                    else if(tasca.equalsIgnoreCase("SGDFALTASCHECK-FROMLAST"))
                    {
                           // doSgdFaltasCheckFromLast();
                    }
                }
            }
  
        }

    public void doTaskPresencia() {
        //System.out.println("fent run start"+GuardiesGUI.sp+GuardiesGUI.sp.isRunning());
        SensorPresencia sp= new SensorPresencia(cfg);
        if(sp!=null && !sp.isRunning()) {
            sp.start();
        }
    }

    public void doTaskImportSGD() {
        //Importa dades de la base sgd
        //String any = StringUtils.anyAcademic_primer();
        String any = cfg.getCoreCfg().anyAcademic+"";
        SGDImporter updatesgd = new SGDImporter("curso"+any, any, "PROGRAMADA", -1, null, cfg.getCoreCfg().getIesClient());
        updatesgd.start();
    }

    public void backup(String database) {
       
        DataCtrl cd = new DataCtrl();
        
        String path= "\"" + Cfg.mysqldump_path;
        String pwd1 = "";
        String pwd2 = "";
        
        
      
        if(!CoreCfg.core_mysqlPasswd.isEmpty()) {
            pwd1="-p";
        }
        if(!CoreCfg.coreDB_sgdPasswd.isEmpty()) {
            pwd2="-p";
        }
        
        ArrayList<String> cmd = new ArrayList<String>();
        String zipFile="";
        String[] outputfiles = null;
                
        if(database.equalsIgnoreCase("fitxes"))
        {      
                //we must split host and port
                String realHost = CoreCfg.core_mysqlHost;
                String realPort = " ";
                if(CoreCfg.core_mysqlHost.contains(":"))
                {
                    realHost = StringUtils.BeforeFirst(CoreCfg.core_mysqlHost, ":");
                    realPort = " -P"+StringUtils.AfterFirst(CoreCfg.core_mysqlHost, ":");
                }
                //Ara hi ha dues bases de per fer backup
                outputfiles = new String[2];
                        
                String ext = cd.getDataSQL()+"_"+cd.getHoraPunt();
                outputfiles[0] = cfg.output_path+cfg.getCoreCfg().core_mysqlDB+"_"+ext+".sql";
                cmd.add(path+"mysqldump.exe\" -h"+realHost + realPort+" -u"+cfg.getCoreCfg().core_mysqlUser+" "+pwd1+cfg.getCoreCfg().core_mysqlPasswd
                + " "+cfg.getCoreCfg().core_mysqlDB+" > "+outputfiles[0]);
                
                outputfiles[1] = cfg.output_path+cfg.getCoreCfg().core_mysqlDBPrefix+"_"+ext+".sql";
                cmd.add(path+"mysqldump.exe\" -h"+realHost+ realPort + " -u"+cfg.getCoreCfg().core_mysqlUser+" "+pwd1+cfg.getCoreCfg().core_mysqlPasswd
                + " "+CoreCfg.core_mysqlDBPrefix+" > "+outputfiles[1]);
                
                zipFile = Cfg.output_path+cfg.getCoreCfg().core_mysqlDBPrefix+"_"+ext+".zip";
                
                                
        }
        else if(database.equalsIgnoreCase("sgd"))
        { 
                 //we must split host and port
                String realHost = CoreCfg.coreDB_sgdHost;
                String realPort = " ";
                if(CoreCfg.coreDB_sgdHost.contains(":"))
                {
                    realHost = StringUtils.BeforeFirst(CoreCfg.coreDB_sgdHost, ":");
                    realPort = " -P"+StringUtils.AfterFirst(CoreCfg.coreDB_sgdHost, ":");
                }
                outputfiles = new String[1];
                String ext = cfg.output_path+cfg.getCoreCfg().coreDB_sgdDB+"_"+cd.getDataSQL()+"_"+cd.getHoraPunt();
                outputfiles[0] = ext+".sql";
                cmd.add(path+"mysqldump.exe\" -h"+realHost+ realPort+" -u"+cfg.getCoreCfg().coreDB_sgdUser+" "+pwd2+cfg.getCoreCfg().coreDB_sgdPasswd
                + " "+cfg.getCoreCfg().coreDB_sgdDB+" > "+outputfiles[0]);
                
                zipFile = ext+".zip";
        }
        
        //System.out.println("Command:: "+ cmd);
        String output_error = "";
        Runtime rt = Runtime.getRuntime();
        try {
            for(int i=0; i<cmd.size(); i++)
            {
                String currentError =  "";
                Process p = rt.exec("cmd /c " + cmd.get(i));
                p.waitFor();
        
                String s="";
                BufferedReader stdError = new BufferedReader(new 
                     InputStreamReader(p.getErrorStream()));

                while ((s = stdError.readLine()) != null) {
                    currentError += s + " ";
                }

                p.getInputStream().close();
                p.getOutputStream().close();
                p.getErrorStream().close();
                
                if(!currentError.isEmpty())
                {
                    output_error += "Error when processing cmd /c " + cmd.get(i)+"\n"+currentError;
                }
           }
        } catch (IOException ex) {
            Logger.getLogger(Programador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Programador.class.getName()).log(Level.SEVERE, null, ex);
        }

       
        //Els agrupa dins d'un zip
        String output = Programador.zipFiles(zipFile, outputfiles);
        //Si el zip es correcte, esborra els fitxers .sql
        if (!output.contains("IOException")) {
            for(int i=0; i<outputfiles.length; i++)
            {
               new File(outputfiles[i]).delete();
            }
        }

      
        File file = new File(zipFile);
        if(file.exists())
        {
            output_error += "\nBackup generated "+file.getName()+" ("+file.length()/1000.+" KB)";
        }
    
        String SQL1 = "INSERT INTO sig_log (usua,ip,netbios,domain,tasca,inici,fi,resultat) VALUES(?,'"+cfg.getCoreCfg().ip+"','"+cfg.getCoreCfg().netbios+"','"+cfg.getCoreCfg().core_PRODUCTID+"',?,NOW(),NOW(),?)";
        Object[] obj = new Object[]{"PROGRAMAT", "BACKUP", output_error};
        int nup = cfg.getCoreCfg().getMysql().preparedUpdate(SQL1, obj);
        
  
    }

//Realitza les conversions de lleus a històriques
    public void doLleu2hist() {     
        Lleu2Hist task = new Lleu2Hist("PROGRAMAT", cfg.getCoreCfg());
        //Abans de fer conversió s'assegura la integritat de la base
        //Cerca i elimina inconsistencies
        task.checkConsistencia(Lleu2Hist.MODE_ACTUALITZA);
        task.convert(Lleu2Hist.MODE_ACTUALITZA);
    }

//Recorda als professors que tenen solicituds de tutors a punt de caducar    
    public void doRemainders() {

        String log = "";
        String SQL0 = "INSERT INTO sig_log (usua,ip,netbios,domain,tasca,inici,resultat) VALUES('PROGRAMAT','"+cfg.getCoreCfg().ip+"','"+ cfg.getCoreCfg().netbios+"','"+  cfg.getCoreCfg().core_PRODUCTID+"','REMAINDERS',NOW(),'')";
        int idTask = cfg.getCoreCfg().getMysql().executeUpdateID(SQL0);
        
        //check
        String SQL1 = "SELECT DISTINCT mis.destinatari, idMensajeProfesor FROM sig_missatgeria as mis INNER JOIN"
                + " tuta_entrevistes as tenv ON tenv.id = mis.idEntrevista WHERE dataContestat IS NULL AND "
                + " DATEDIFF(NOW(), tenv.dia) =-2  AND DATEDIFF(NOW(),tenv.dataEnviat)>1 ";
               
         try {
             Statement st = cfg.getCoreCfg().getMysql().createStatement();
             ResultSet rs1 = cfg.getCoreCfg().getMysql().getResultSet(SQL1,st);
            while(rs1!=null && rs1.next())
            {
                int idMensajes = rs1.getInt("idMensajeProfesor");
                String desti = rs1.getString("destinatari");
                
                if(idMensajes>0)
                {
                    MensajesProfesores mp = new MensajesProfesores(idMensajes);
                    mp.setBorradoUp(false);
                    mp.setFechaLeido(null);
                    java.util.Date ara = new java.util.Date();
                    mp.setFechaEnviado(new java.sql.Date(ara.getTime()));
                    int saved = mp.save();
                    if(saved>0){
                        log += desti + "("+idMensajes+"); ";
                    }
                   
                }
            }
            if(rs1!=null){
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Programador.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
      SQL0 = "UPDATE sig_log SET resultat='"+log+"', fi=NOW() WHERE id='"+idTask+"'";
      cfg.getCoreCfg().getMysql().executeUpdate(SQL0);
    
    }


/*
        Create Zip File From Multiple Files using ZipOutputStream Example
        This Java example shows how create zip file containing multiple files
        using Java ZipOutputStream class.
*/    
    public static String zipFiles(String zipFile, String[] sourceFiles)
    {

        StringBuilder sb = new StringBuilder();
                try
                {
                        
                        //create byte buffer
                        byte[] buffer = new byte[1024];
                       
                        /*
                         * To create a zip file, use
                         *
                         * ZipOutputStream(OutputStream out)
                         * constructor of ZipOutputStream class.
                        */
                         
                         //create object of FileOutputStream
                         FileOutputStream fout = new FileOutputStream(zipFile);
                         
                         //create object of ZipOutputStream from FileOutputStream
                         ZipOutputStream zout = new ZipOutputStream(fout);
                         
                         for(int i=0; i < sourceFiles.length; i++)
                         {
                               
                                sb.append("Adding ").append(sourceFiles[i]);
                                //create object of FileInputStream for source file
                                FileInputStream fin = new FileInputStream(sourceFiles[i]);
 
                                /*
                                 * To begin writing ZipEntry in the zip file, use
                                 *
                                 * void putNextEntry(ZipEntry entry)
                                 * method of ZipOutputStream class.
                                 *
                                 * This method begins writing a new Zip entry to
                                 * the zip file and positions the stream to the start
                                 * of the entry data.
                                 */
 
                                zout.putNextEntry(new ZipEntry(sourceFiles[i]));
 
                                /*
                                 * After creating entry in the zip file, actually
                                 * write the file.
                                 */
                                int length;
 
                                while((length = fin.read(buffer)) > 0)
                                {
                                   zout.write(buffer, 0, length);
                                }
 
                                /*
                                 * After writing the file to ZipOutputStream, use
                                 *
                                 * void closeEntry() method of ZipOutputStream class to
                                 * close the current entry and position the stream to
                                 * write the next entry.
                                 */
 
                                 zout.closeEntry();
 
                                 //close the InputStream
                                 fin.close();
                               
                         }
                       
                         
                          //close the ZipOutputStream
                          zout.close();
                         
                          sb.append("Zip file has been created!");
               
                }
                catch(IOException ioe)
                {
                        sb.append("IOException : ").append(ioe);
                }
            
                return sb.toString();           
        }

//    public void doSgdFaltasCheckAll() {
//       String SQL0 = "INSERT INTO sig_log (usua,ip,netbios,domain,tasca,inici,resultat) VALUES('PROGRAMAT','"+Corecfg.getCoreCfg().ip+"','"+Corecfg.getCoreCfg().netbios+"','"+Corecfg.getCoreCfg().core_PRODUCTID+"','SGDFALTASCHECK-ALL',NOW(),'')";
//       int pid  = Corecfg.getCoreCfg().getMysql().executeUpdateID(SQL0);
//       int idTask = cfg.getCoreCfg().getMysql().executeUpdateID(SQL0);
//        
//       checksgd.ThreadAnalitzaPerLog thread = new checksgd.ThreadAnalitzaPerLog(null, null, pid);
//       thread.start();
//    }
//
//    public void doSgdFaltasCheckFromLast() {
//        String SQL0 = "INSERT INTO sig_log (usua,ip,netbios,domain,tasca,inici,resultat) VALUES('PROGRAMAT','"+Corecfg.getCoreCfg().ip+"','"+Corecfg.getCoreCfg().netbios+"','"+Corecfg.getCoreCfg().core_PRODUCTID+"','SGDFALTASCHECK-FROMLAST',NOW(),'')";
//        int pid  = Corecfg.getCoreCfg().getMysql().executeUpdateID(SQL0);
//        java.util.Date date = new checksgd.SgdFaltasCheck().getLastDate();
//        checksgd.ThreadAnalitzaPerLog thread = new checksgd.ThreadAnalitzaPerLog(date, null, pid);
//        thread.start(); 
//    }
    
   
    
    
}
