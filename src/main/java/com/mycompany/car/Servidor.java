/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.car;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pablo
 */
public class Servidor {

    //ArrayList que contiene todos los sockets separados por grupos
    public static ArrayList<ArrayList<Socket>> clientes = new ArrayList<>();

    //ArrayList que contiene todos los tiempos de todos los clientes separados por grupos
    public static ArrayList<ArrayList<Long>> tiempoDatosInicio = new ArrayList<>();
    public static ArrayList<ArrayList<Long>> tiempoEnvioMensajes = new ArrayList<>();
    public static ArrayList<ArrayList<Long>> tiempoTotal = new ArrayList<>();

    //ArrayList que contiene la media de tiempos por grupo
    public static ArrayList<Long> AVGTiempoDatosInicio = new ArrayList<>();
    public static ArrayList<Long> AVGTiempoEnvioMensajes = new ArrayList<>();
    public static ArrayList<Long> AVGTiempoTotal = new ArrayList<>();
    
    //ArrayList que almacena las coordenadas enviadas por lo clientes por grupos y por cliente
    //public static ArrayList<ArrayList<ArrayList<Coordenadas>>> coordenadasClientes = new ArrayList<>(); // No se ha implementado
    
    public static int numClientes = 0; // Numero total de clientes que se van a conectar
    public static int grupos = 0; // Numero de grupos que habra
    public static int iteraciones = 0; // Cantidad de ciclos de intercambio de mensajes que se realizaran
    public static int numVecinos = 0; // Numero de clientes por grupo
    
    //Contiene valores de tipo boolean que nos sirve para pausar los hiloServidor antes de empezar el intercambio de mensajes.
    public static ArrayList<Boolean> sincronizacion = new ArrayList<>();
    
    public static void main(String[] args) {
        try {

            ArrayList<HiloServidor> hilosServidor = new ArrayList<>(); //ArrayList que almacena los hilos creados por el servidor
            Scanner scanner = new Scanner(System.in);
            final int PUERTO = 9090; // Puerto al que realizamos la conexion

            /**
             * Introduccion de la informacion
             */
            if (args.length == 3) {
                numClientes = Integer.parseInt(args[0]);
                grupos = Integer.parseInt(args[1]);
                iteraciones = Integer.parseInt(args[2]);
            } else {
                do {
                    System.out.println("Introduce el numero de clientes");
                    numClientes = scanner.nextInt();
                    System.out.println("Introduce el numero de grupos");
                    grupos = scanner.nextInt();
                } while (numClientes % grupos != 0);
                System.out.println("Introduce el numero de iteraciones");
                iteraciones = scanner.nextInt();

            }
            numVecinos = numClientes / grupos;

            InicializarVectoresDatos(grupos);

            ServerSocket serverSocket = new ServerSocket(PUERTO, numClientes);
            
            int contadorGrupo = 0; //Variable auxiliar para el conteo de grupos.
            
            ArrayList<Socket> auxiliar = new ArrayList<>(); //Contine los cliente de un grupo momentaneamente 
            for (int i = 0; i < numClientes; i++) {
                //System.out.println("Esperando...");
                Socket socket = serverSocket.accept(); 
                
                HiloServidor hiloServidor = new HiloServidor(i, contadorGrupo, iteraciones, socket);
                hilosServidor.add(hiloServidor);
                System.out.println("Conectado " + i);
                auxiliar.add(socket);

                if (auxiliar.size() == numVecinos) {
                    clientes.add(auxiliar);
                    auxiliar = new ArrayList<>();
                    contadorGrupo++;
                }
            }

            for (int i = 0; i < hilosServidor.size(); i++) {
                hilosServidor.get(i).start();
            }
            
            
            System.out.println("Sistema iniciado");
            System.out.println("");

            //Espera a que los hiloServidor finalizen para realizar los calculos
            for (HiloServidor hilo : hilosServidor) {
                try {
                    hilo.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            /**
             * Al finalizar los hilos del servidor, empiezan los calculos
             */
            calculoAVGTiempoDatosInicio();
            calculoAVGTiempoEnvioMensajes();
            calculoAVGTiempoTotal();

            for (int i = 0; i < grupos; i++) {
                System.out.println("Tiempos grupo " + i);
                System.out.println("Tiempo datos inicio " + AVGTiempoDatosInicio.get(i));
                System.out.println("Tiempo envio mensajes " + AVGTiempoEnvioMensajes.get(i));
                System.out.println("Tiempo total " + AVGTiempoTotal.get(i));
                System.out.println("");
            }

            escribirDatosEnFicheros();
            
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Inicializa los vectores de datos
    public static void InicializarVectoresDatos(int grupos) {
        for (int i = 0; i < grupos; i++) {
            tiempoDatosInicio.add(new ArrayList<>());
            tiempoEnvioMensajes.add(new ArrayList<>());
            tiempoTotal.add(new ArrayList<>());
            
            for (int j = 0; j < numVecinos; j++) {
                
                sincronizacion.add(false);
            }
        }
    }

    //Devuelve un arrayList con todos los sockets de un grupo
    public static ArrayList<Socket> cogerVeicnos(int grupo) {
        return clientes.get(grupo);
    }

    // Carga los datos de tiempo de un cliente en los arrays de tiempo correspondiente.
    public static void cargarDatosTiempo(long tiempoDatosInicioAux, long tiempoEnvioMensajesAux, long tiempoTotalAux, int grupo) {
        tiempoDatosInicio.get(grupo).add(tiempoDatosInicioAux);
        tiempoEnvioMensajes.get(grupo).add(tiempoEnvioMensajesAux);
        tiempoTotal.get(grupo).add(tiempoTotalAux);
    }

    //Calcula la media por grupos de la variable tiempoDatosInicio
    public static void calculoAVGTiempoDatosInicio() {
        long sumaTotal = 0;

        for (int i = 0; i < tiempoDatosInicio.size(); i++) {
            for (int j = 0; j < tiempoDatosInicio.get(i).size(); j++) {
                sumaTotal += tiempoDatosInicio.get(i).get(j);
            }
            AVGTiempoDatosInicio.add(sumaTotal / tiempoDatosInicio.get(i).size());
            sumaTotal = 0;
        }
    }

    //Calcula la media por grupos de la variable tiempoEnvioMensajes
    public static void calculoAVGTiempoEnvioMensajes() {
        long sumaTotal = 0;

        for (int i = 0; i < tiempoEnvioMensajes.size(); i++) {
            for (int j = 0; j < tiempoEnvioMensajes.get(i).size(); j++) {
                sumaTotal += tiempoEnvioMensajes.get(i).get(j);
            }
            AVGTiempoEnvioMensajes.add(sumaTotal / tiempoEnvioMensajes.get(i).size());
            sumaTotal = 0;
        }
    }

    //Calcula la media por grupos de la variable tiempoTotal
    public static void calculoAVGTiempoTotal() {
        long sumaTotal = 0;

        for (int i = 0; i < tiempoTotal.size(); i++) {
            for (int j = 0; j < tiempoTotal.get(i).size(); j++) {
                sumaTotal += tiempoTotal.get(i).get(j);
            }
            AVGTiempoTotal.add(sumaTotal / tiempoTotal.get(i).size());
            sumaTotal = 0;
        }
    }

    //vuelca los datos en archivos txt.
    public static void escribirDatosEnFicheros() throws IOException {
        
        File tiempoDatosInicioFile = new File("tiempoDatosInicio.txt");
        File tiempoEnvioMensajesFile = new File("tiempoDatosInicio.txt");
        File tiempoTotalFile = new File("tiempoTotal.txt");

        FileWriter tiempoDatosInicioTXT = null;
        FileWriter tiempoEnvioMensajesTXT = null;
        FileWriter tiempoTotalTXT = null;
        
        if ((!tiempoDatosInicioFile.exists()) || (!tiempoEnvioMensajesFile.exists()) || (!tiempoTotalFile.exists())) {
            tiempoDatosInicioTXT = new FileWriter("tiempoDatosInicio.txt");
            tiempoEnvioMensajesTXT = new FileWriter("tiempoEnvioMensajes.txt");
            tiempoTotalTXT = new FileWriter("tiempoTotal.txt");

        } else {
            tiempoDatosInicioTXT = new FileWriter("tiempoDatosInicio.txt", true);
            tiempoEnvioMensajesTXT = new FileWriter("tiempoEnvioMensajes.txt", true);
            tiempoTotalTXT = new FileWriter("tiempoTotal.txt", true);
        }

        PrintWriter pwTiempoDatosInicioTXT = new PrintWriter(tiempoDatosInicioTXT);
        PrintWriter pwTiempoEnvioMensajesTXT = new PrintWriter(tiempoEnvioMensajesTXT);
        PrintWriter pwTiempoTotalTXT = new PrintWriter(tiempoTotalTXT);

        pwTiempoDatosInicioTXT.println(numClientes + " " + grupos + " " + iteraciones + " " + calcularAVGGlobalTiempoDatosInicio());
        pwTiempoEnvioMensajesTXT.println(numClientes + " " + grupos + " " + iteraciones + " " + calcularAVGGlobalTiempoEnvioMensajes());
        pwTiempoTotalTXT.println(numClientes + " " + grupos + " " + iteraciones + " " + calcularAVGGlobalTiempoTotal());

        tiempoDatosInicioTXT.close();
        tiempoEnvioMensajesTXT.close();
        tiempoTotalTXT.close();

    }
    
    //calcula la media de los grupos en intercambiarse los datos de inicio
    public static long calcularAVGGlobalTiempoDatosInicio(){
        long suma = 0;
        
        for (int i = 0; i < AVGTiempoDatosInicio.size(); i++) {
            suma += AVGTiempoDatosInicio.get(i);
        }
        
        return suma / AVGTiempoDatosInicio.size();
    }
    
    //calcula la media de los grupos en intercambio de mensajes
    public static long calcularAVGGlobalTiempoEnvioMensajes(){
        long suma = 0;
        
        for (int i = 0; i < AVGTiempoEnvioMensajes.size(); i++) {
            suma += AVGTiempoEnvioMensajes.get(i);
        }
        
        return suma / AVGTiempoEnvioMensajes.size();
    }
    
    //calcula la media de los grupos en el timepo total
    public static long calcularAVGGlobalTiempoTotal(){
        long suma = 0;
        
        for (int i = 0; i < AVGTiempoTotal.size(); i++) {
            suma += AVGTiempoTotal.get(i);
        }
        
        return suma / AVGTiempoTotal.size();
    }

    //Carga el estado del hiloServidor al arrayList de sincronizacion para empezar el intercambio de mensajes a la vez.
    public static void cargarSincronizacion(int posicion, boolean valor){
        sincronizacion.set(posicion, valor);
    }
    
    //Hace la operacion logica AND con los valores del vector sincronizacion
    public static boolean empezar(){
        if(sincronizacion.contains(false)){
            //System.out.println("No empezar");
            return false;
        } else {
            //System.out.println("EMPEZAR!");
            return true;
        }
    }
}
