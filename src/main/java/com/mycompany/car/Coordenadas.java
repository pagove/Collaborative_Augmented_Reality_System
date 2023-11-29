/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.car;

/**
 *
 * @author pablo
 */
public class Coordenadas {
    private int x; // Coordenada x
    private int y; // Coordenada y
    private int z; // Coordenada z

    public Coordenadas() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public String generarCoordenadas(){
        // int r = (int) (Math.random() * (upper - lower)) + lower;
        
        int x = (int) (Math.random() * (500 + 500) - 500);
        int y = (int) (Math.random() * (500 + 500) - 500);
        int z = (int) (Math.random() * (500 + 500) - 500);
        /*int x = (int) (Math.random()*1000 +1);
        int y = (int) (Math.random()*1000 +1);
        int z = (int) (Math.random()*1000 +1);*/
        
        String mensaje = "coordenadas -> (" + x + "," + y + "," + z + ")";
        
        return mensaje;
    }

    
    /**
     * Getters y setters
     * 
     */
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }
    
    
    
    
    
}
