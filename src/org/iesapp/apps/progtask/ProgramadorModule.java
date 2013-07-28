/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.apps.progtask;

import com.l2fprod.common.swing.StatusBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import org.iesapp.apps.mysqlbrowser.DBEditorDlg;
import org.iesapp.framework.pluggable.StatusBarZone;
import org.iesapp.framework.pluggable.TopModuleWindow;
import org.iesapp.framework.table.TextAreaRenderer;
import org.iesapp.util.DataCtrl;

/**
 *
 * @author Josep
 */
public class ProgramadorModule extends TopModuleWindow {
    private DefaultTableModel modelTable1;
    private Cfg cfg;
    private Programador programador;
    private Timer timer;
    private final ResourceBundle bundle;

    /**
     * Creates new form ProgramadorModule
     */
    public ProgramadorModule() {
        bundle = java.util.ResourceBundle.getBundle("org/iesapp/apps/progtask/bundle"); 
        initComponents();
        jTable1.setIntercellSpacing( new java.awt.Dimension(2,2) );
        jTable1.setGridColor(java.awt.Color.gray);
        jTable1.setShowGrid(true);
         
        this.moduleDescription="Allows administrator to schedule tasks in the iesdigital framework";
        this.moduleDisplayName="Scheduled tasks";
        this.moduleName="progtask";
       
    }
     

    @Override
    public void postInitialize() {
        //Crea una instancia de cfg
        cfg = new Cfg(coreCfg, new String[]{});
        
        int timerInterval = 60000; //cada 1 min actualiza el rellotge
        
        String info = cfg.isProgramadorRunning();
        if(!info.isEmpty())
        {
           jLabel1.setText(bundle.getString("alert") +info);
           cfg.stampFix();
        }
         
        programador = new Programador(cfg);
        fillTable();
        DataCtrl cd = new DataCtrl();
        jLabel1.setText(cd.getDiaMesComplet()+"  "+cd.getHora());
         
        timer = new Timer(timerInterval, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //Comprova si hi ha connexió (en cas que es pogues perdre)
                if(cfg.getCoreCfg().getMysql()==null || (cfg.getCoreCfg().getMysql()!=null && cfg.getCoreCfg().getMysql().isClosed()) )
                {
                    cfg.getCoreCfg().resetConnection();
                }
                
               programador.check();
               fillTable();
               DataCtrl cd = new DataCtrl();
               jLabel1.setText(bundle.getString("last")+" "+cd.getDiaMesComplet()+"  "+cd.getHora());
            }

            
          

        });

        cfg.getStamper().inStamp();
        timer.start();
        
        cfg.getCoreCfg().getMainHelpBroker().enableHelpKey(this, "org-iesapp-apps-progtask", null);
     
    }
    
    
    @Override
    public void refreshUI() {
        fillTable();
    }
    

    private void fillTable() {
         while(jTable1.getRowCount()>0) {
            modelTable1.removeRow(0);
        }
         
        String SQL1 = "Select * from sig_log where usua='PROGRAMAT' order by inici desc";
        
        try {
             Statement st = cfg.getCoreCfg().getMysql().createStatement();
             ResultSet rs1 = cfg.getCoreCfg().getMysql().getResultSet(SQL1,st);
            while(rs1!=null && rs1.next())
            {
                modelTable1.addRow(new Object[]{
                    rs1.getInt("id"),
                    rs1.getString("tasca"),
                    rs1.getTimestamp("inici"),
                    rs1.getTimestamp("fi"),
                    rs1.getString("resultat")});
            }
            if(rs1!=null) {
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProgramadorModule.class.getName()).log(Level.SEVERE, null, ex);
        }
   }  
   

    @Override
    public void setMenus(JMenuBar jMenuBar1, JToolBar jToolbar1, StatusBar jStatusBar1) {
       super.setMenus(jMenuBar1, jToolbar1, jStatusBar1);
       ((StatusBarZone) jStatusBar1.getZone("third")).addComponent(jLabel1);
       jMenuBar1.add(jMenuScheduled, 1);
       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jMenuScheduled = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable(){
            public boolean isCellEditable(int row, int col)
            {
                return col>3;
            }
        };

        jLabel1.setText("...");

        jMenuScheduled.setText("Tasques Programades");
        jMenuScheduled.setName("jMenuScheduled"); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItem1.setText("Refresh");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem1);

        jMenuItem2.setText("Edita les tasques");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem2);
        jMenuScheduled.add(jSeparator4);

        jMenuItem4.setText("Presència");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem4);

        jMenuItem5.setText("SGD to Fitxes");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem5);
        jMenuScheduled.add(jSeparator2);

        jMenuItem6.setText("Backup database FITXES");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem6);

        jMenuItem9.setText("Backup database SGD");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem9);
        jMenuScheduled.add(jSeparator3);

        jMenuItem7.setText("Lleu-->Històrica");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem7);

        jMenuItem8.setText("Remainders");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenuScheduled.add(jMenuItem8);

        getContentContainer().setLayout(new java.awt.BorderLayout());

        modelTable1 = new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", bundle.getString("task"), bundle.getString("start"), bundle.getString("end"), bundle.getString("result")
            }
        );
        jTable1.setModel(modelTable1);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(400);
        jTable1.getColumnModel().getColumn(4).setCellRenderer(new TextAreaRenderer());
        jScrollPane1.setViewportView(jTable1);

        getContentContainer().add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        timer.stop();

        DBEditorDlg dlg = new DBEditorDlg(javar.JRDialog.getActiveFrame(), true, "Taula: sig_progtasques", cfg.getCoreCfg().getMysql(), cfg.getCoreCfg().core_mysqlDB, "sig_progtasques");
        dlg.setVisible(true);
        cfg.loadTasques();
        timer.start();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        programador.doTaskPresencia();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        programador.doTaskImportSGD();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        programador.backup("fitxes");
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        programador.backup("sgd");
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        programador.doLleu2hist();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        programador.doRemainders();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         this.refreshUI();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JMenu jMenuScheduled;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
