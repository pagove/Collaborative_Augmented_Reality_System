/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author pablo
 */
public class HiloCliente extends Thread {
    String dirIP;
    int puerto;
  
    public HiloCliente(String dirIp, int puerto){
        this.dirIP = dirIp;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try {
            //System.out.println("dir " + dirIP);
            long tiempoInicio, tiempoDatosInicio, tiempoEnvioMensajes, tiempoTotal; // Tiempos (datos estadisticos)
            Socket socket = new Socket(dirIP, puerto);
            
            
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream()); // Envio mensajes
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream()); // Recepcion mensajes

            /**
             * Asignacion de datos
             */
            tiempoInicio = System.currentTimeMillis();
            JSONObject mensaje = new JSONObject(dataInputStream.readUTF());

            int id = Integer.parseInt(String.valueOf(mensaje.get("id"))); // Id del cliente
            int numeroVecinos = Integer.parseInt(String.valueOf(mensaje.get("numVecinos"))); // Cantidad de vecinos del cliente
            int iteraciones = Integer.parseInt(String.valueOf(mensaje.get("iteraciones"))); // Veces que se tiene que mover
            
            dataOutputStream.writeUTF(String.valueOf(socket.getLocalPort()));
            
            tiempoDatosInicio = System.currentTimeMillis();
            tiempoDatosInicio = tiempoDatosInicio - tiempoInicio;
            
            
            /**
             * Intercambio de mensajes
             */
            tiempoInicio = System.currentTimeMillis();
            for (int j = 0; j < iteraciones; j++) {
                //Envio coordenadas servidor
                if(j == 0){
                    socket.setSoTimeout(20000);
                }
                
                mensaje = new JSONObject();
                Coordenadas coordenadas = new Coordenadas();
                String mensajeCoordenadas = "Cliente con id: " + id + " " + coordenadas.generarCoordenadas();
                
                mensaje.put("coordenadaX", coordenadas.getX());
                mensaje.put("coordenadaY", coordenadas.getY());
                mensaje.put("coordenadaZ", coordenadas.getZ());
                mensaje.put("mensajeCoordenadas", mensajeCoordenadas);
                
                dataOutputStream.writeUTF(mensaje.toString());
                dataOutputStream.flush();
                //System.out.println("Mensaje SALIENTE " + mensajeCoordenadas);
                
                //lee coordenadas de sus vecinos
                for (int i = 0; i < (numeroVecinos - 1); i++) {
                    String mensajeEntrante = dataInputStream.readUTF();
                    
                    //System.out.println("Mensaje ENTRANTE " + id + " " + mensajeEntrante);
                }

                // Le notifica al servidor que ha recibido todas las coordenadas.
                dataOutputStream.writeUTF("Coordenadas de mis vecinos actualizadas");
                dataOutputStream.flush();
                // El servidor informa de que los vecinos han recibido las coordenadas
                String mensajeFinal = dataInputStream.readUTF();
                //System.out.println(mensajeFinal);

            }
            tiempoEnvioMensajes = System.currentTimeMillis();
            tiempoEnvioMensajes = tiempoEnvioMensajes - tiempoInicio;
            
            tiempoTotal = tiempoDatosInicio + tiempoEnvioMensajes;
            
            /**
             * Envio de datos al servidor
             */
            JSONObject datosTiempo = new JSONObject();
            datosTiempo.put("tiempoDatosInicio", tiempoDatosInicio);
            datosTiempo.put("tiempoEnvioMensajes", tiempoEnvioMensajes);
            datosTiempo.put("tiempoTotal", tiempoTotal);
            
            dataOutputStream.writeUTF(datosTiempo.toString());
            
            //System.out.println("FIN DE LA COMUNICACION");
            /**
             * Cierre de la comunicacion
             */
            socket.close();
            
            

        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
