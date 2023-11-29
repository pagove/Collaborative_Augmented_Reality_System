/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author pablo
 */
public class HiloServidor extends Thread {

    int id; // Identificador del hilo y del cliente
    int grupo; // Grupo al que pertenece
    int iteraciones; // Numero de iteraciones que hay que realizar
    Socket socket; // Socket cliente
    
    public HiloServidor(int id, int grupo, int iteraciones, Socket socket) {

        this.id = id;
        this.iteraciones = iteraciones;
        this.grupo = grupo;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            //int numMensajes = 0; // Contador numero mensajes intercambiados (uso estadistico)
            int numVecinos; // Numero de vecinos por grupo
            ArrayList<Socket> vecinos; // Sockets de los vecinos.
            
            
            vecinos = Servidor.cogerVeicnos(grupo);
            numVecinos = vecinos.size();

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            /**
             * Asignacion de datos
             */
            JSONObject mensaje = new JSONObject();
            mensaje.put("id", id);
            mensaje.put("numVecinos", numVecinos);
            mensaje.put("iteraciones", iteraciones);

            dataOutputStream.writeUTF(mensaje.toString());
            //numMensajes++;
            dataOutputStream.flush();
            
            int puertoCliente = Integer.parseInt(dataInputStream.readUTF());
            
            Servidor.cargarSincronizacion(id, true); // Informa al servidor que el intercambio de datos iniciales se realizado correctamente
            
            while(!Servidor.empezar()){
                try {
                    sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } // Espera
            
            socket.setSoTimeout(20000);
            
            for (int j = 0; j < iteraciones; j++) {
                //Lee coordenadas cliente
                /**
                 * Esta parte la implementamos para cargar las coordenadas de cada cliente
                 */
                Coordenadas coordenada = new Coordenadas();
                
                mensaje = new JSONObject(dataInputStream.readUTF());
                coordenada.setX(Integer.parseInt(String.valueOf(mensaje.get("coordenadaX"))));
                coordenada.setY(Integer.parseInt(String.valueOf(mensaje.get("coordenadaY"))));
                coordenada.setZ(Integer.parseInt(String.valueOf(mensaje.get("coordenadaZ"))));
                
                //numMensajes++;
                String mensajeCoordenadas = mensaje.getString("mensajeCoordenadas");
                //System.out.println("Mensaje entrante  " + id + " " + mensajeCoordenadas);
                
                //reenvio coordenadas a sus vecinos
                DataOutputStream dataOutputStreamVecinos;
                for (int i = 0; i < vecinos.size(); i++) {

                    if (vecinos.get(i).getPort() != puertoCliente) {

                        dataOutputStreamVecinos = new DataOutputStream(vecinos.get(i).getOutputStream());
                        dataOutputStreamVecinos.writeUTF(mensajeCoordenadas);
                        //System.out.println("Mensaje saliente "  + id + " " + mensajeCoordenadas);

                        dataOutputStreamVecinos.flush();
                        //numMensajes++;
                    }
                }
                dataOutputStream.flush();
                //El cliente confirma que ha recibido todas las coordenadas
                dataInputStream.readUTF();
                //numMensajes++;
                //El servidor informa al cliente de que sus vecinos han recibido las coordenadas
                dataOutputStream.writeUTF("Tus vecinos han actualizado tus coordenadas");
                //numMensajes++;
                dataOutputStream.flush();
            }
            
            long tiempoDatosInicio, tiempoEnvioMensajes, tiempoTotal;
            
            JSONObject datosTiempo = new JSONObject(dataInputStream.readUTF());
            tiempoDatosInicio = Long.parseLong(String.valueOf(datosTiempo.get("tiempoDatosInicio")));
            tiempoEnvioMensajes = Long.parseLong(String.valueOf(datosTiempo.get("tiempoEnvioMensajes")));
            tiempoTotal = Long.parseLong(String.valueOf(datosTiempo.get("tiempoTotal")));
            /**
             * Cierre de la comunicacion
             */
            socket.close();
            //numMensajes++;
           
            Servidor.cargarDatosTiempo(tiempoDatosInicio, tiempoEnvioMensajes, tiempoTotal, grupo);
            //System.out.println("Numero de mensajes por cliente " + numMensajes);
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
