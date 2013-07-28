/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.apps.progtask;

import java.sql.Time;

/**
 *
 * @author Josep
 */
public class BeanTasca {

    private Time hora;
    private int dia=0;
    private String tasca;

    public String getTasca() {
        return tasca;
    }

    public void setTasca(String tasca) {
        this.tasca = tasca;
    }


    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }


    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

}
