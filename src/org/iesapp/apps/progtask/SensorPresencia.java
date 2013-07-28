/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.iesapp.apps.progtask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iesapp.clients.iesdigital.guardies.CellModel;
import org.iesapp.clients.iesdigital.guardies.Presencia;
import org.iesapp.clients.iesdigital.guardies.RowModel;
import org.iesapp.util.DataCtrl;

/**
 *
 * @author Josep
 */
public class SensorPresencia extends Thread {
 
    private boolean isrunning=false;
    private final Cfg cfg;
 

    public SensorPresencia(Cfg cfg)
    {
        this.cfg = cfg;
    }

    @Override
    public void run()
    {

        isrunning = true;
              
        boolean connectat1 = (cfg.getCoreCfg().getSgd()!=null && !cfg.getCoreCfg().getSgd().isClosed());
        boolean connectat2 = (cfg.getCoreCfg().getMysql()!=null && !cfg.getCoreCfg().getMysql().isClosed());
        String msg = "";
        int jobid=0;
             
        if(connectat2)
        {   
            //Deixa constància que s'ha engegat la tasca
            
            String SQL1 = "INSERT INTO sig_log (usua, tasca, inici) VALUES(?,?, NOW())";
          
            Object[] obj = new Object[]{"PROGRAMAT", "PRESENCIA"};
            jobid = cfg.getCoreCfg().getMysql().preparedUpdate(SQL1, obj);
        }        
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 32);
        cal.set(Calendar.SECOND, 0);
        //java.util.Date minDate = cal.getTime();
        
        
        if(connectat1)
        {
                       
            String SQL1 = "SELECT idProfesores, MAX(desde) AS pinchada, MAX(hasta)"
                    + " AS despinchada FROM Presencia GROUP BY idprofesores";

             try {
                Statement st = cfg.getCoreCfg().getSgd().createStatement();
                ResultSet rs1 = cfg.getCoreCfg().getSgd().getResultSet(SQL1,st);
                while (rs1 != null && rs1.next()) {
                    int idProf = rs1.getInt("idProfesores"); 
                    java.sql.Timestamp ts = rs1.getTimestamp("despinchada");
                    
                    
                    if(ts!=null)
                    {
                    //Intentam fer la conversio via calendar
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTimeInMillis(ts.getTime());
                        
//                    //Aquesta lectura no funciona al servidor
//                    //java.sql.Time desptime =  rs1.getTime("despinchada");
//                    java.sql.Time desptime =null;
//                  
//                    System.out.println("Estic intentant convertir "+ts.toString() );
//                    String horString = StringUtils.AfterFirst( ts.toString(), " ");
//                                 
//                    try {
//                    
//                          SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm:ss", new Locale("es", "ES"));
//                    
//                          desptime = new java.sql.Time(sdf.parse(horString).getTime());
//                    
//                          //System.out.println("Fecha con el formato java.sql.Time: " + desptime);
//                    
//                    } catch (Exception ex) {
//                    
//                          System.out.println("Error al obtener el formato de la fecha/hora: " + ex.getMessage());
//                    
//                    }
//
//                    
//                    
//                    
//                    java.util.Date ud_desp = new java.util.Date(ts.getTime());
                    
                    System.out.println("Ha despenjat"+idProf+", "+ts+", "+cal2.getTime()+","+cal.getTime());
                    
                    if(cal2.after(cal)) //aquest profesor ha despenjat la pda
                    {
                       // System.out.println("el profe idProf"+idProf);
                        //determina quin professor ha tret la pda
                        boolean containsValue = Cfg.abrev2sgdID.containsValue(idProf);
                        String abrev="";
                        if(containsValue)
                        {
                            for(String key: Cfg.abrev2sgdID.keySet())
                            {
                                if(Cfg.abrev2sgdID.get(key).intValue()==idProf)
                                {
                                    abrev = key;
                                    break;
                                }
                            }
                        }
                        //System.out.println("el profe abrevProf"+abrev);
                        //comprova si ha signat o no
                        // si no ha signat, procedeix a signar les hores possibles
                        Presencia pres = new Presencia(cfg.getCoreCfg().getIesClient());
                        //System.out.println("te signat"+pres.haSignat(abrev));
                        if(!pres.haSignat(abrev))  
                        {
                            msg += "{"+abrev+"=";
                            int torn = pres.getTorn(abrev);
                            //Obté l'horari del professor en questio
                            DataCtrl dataCtrl = new DataCtrl();
                            RowModel horari = cfg.getCoreCfg().getIesClient().getGuardiesClient().getGuardiesCollection()
                                    .getHorari(abrev, dataCtrl.getIntDia(), dataCtrl.getDataSQL(), torn>0);
                            
                               //Procedeix a signar les hores possibles
                                //java.sql.Time ara = pres.getServerTime();
                                int iniHora = 1;

                                
                                int offset = 0;
                                if(torn>0) {
                                    offset = 7;
                                }

                                for(int i=0; i<7; i++)
                                {
                                    Calendar cal3 = Calendar.getInstance();
                                    cal3.setTime(cfg.getCoreCfg().getIesClient().getDatesCollection().getHoresClase()[i+offset]);
                                    cal3.set(Calendar.YEAR, cal2.get(Calendar.YEAR));
                                    cal3.set(Calendar.MONTH, cal2.get(Calendar.MONTH));
                                    cal3.set(Calendar.DAY_OF_MONTH, cal2.get(Calendar.DAY_OF_MONTH));
                                    System.out.println("per poder signar comparam cal3 i cal2 "+ cal3.getTime()+ " "+cal2.getTime());
                                    if(cal3.after(cal2)) //signa a partir que despenja PDA
                                    {
                                        CellModel cm = horari.cells[i+1];
                                        if(cm.type == CellModel.TYPE_NORMAL)
                                        {
                                            cm.status = 1;
                                            msg += ""+(i+1)+",";
                                            cfg.getCoreCfg().getIesClient().getGuardiesClient().getGuardiesCollection()
                                                    .updateHorari(abrev, dataCtrl.getIntDia(), dataCtrl.getDataSQL(), i+1, 1, torn>0);
                                            
                                        }

                                    }

                                }
                                msg +="}  ";
                        }
                    }
                    
                }
                }
                if(rs1 != null) {
                    rs1.close();
                    st.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SensorPresencia.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        else
        {
            msg +="Error: no hi ha connexió amb SGD";
        }
        
         

        if(connectat2)
        {       
         //Deixa constància que s'ha executat la tasca i indica el resultat
         String SQL1 = "UPDATE sig_log SET fi=NOW(), resultat=? WHERE id=?";    
         Object[] obj = new Object[]{msg, jobid};
         int nup = cfg.getCoreCfg().getMysql().preparedUpdate(SQL1, obj);
        }
        
      
        isrunning = false;
    }

    public boolean isRunning() {
        return this.isrunning;
    }

}
