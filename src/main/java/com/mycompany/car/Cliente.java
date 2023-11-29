package com.mycompany.car;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pablo
 */
public class Cliente {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner para obtener datos
        String dirIp = "127.0.0.1"; // Dir ip por defecto
        int puerto = 9090; // Puerto por defecto
        int numClientes; // Numero de clientes que tiene que generar
        
        if(args.length == 3){
            numClientes = Integer.parseInt(args[0]);
            dirIp = String.valueOf(args[1]);
            puerto = Integer.parseInt(args[2]);
            System.out.println(numClientes + " " + dirIp + " " + puerto);
        } else {
            System.out.println("Introduce cantidad de clientes");
            numClientes = scanner.nextInt();
        
        }
        
        ArrayList<HiloCliente> clientes = new ArrayList<>(); // ArrayList que contiene los hiloCliente
        for (int i = 0; i < numClientes; i++) {
            HiloCliente hiloCliente = new HiloCliente(dirIp, puerto);
            clientes.add(hiloCliente);
        }
        
        System.out.println("Lanzando clientes");
        for (int i = 0; i < clientes.size(); i++) {
            clientes.get(i).start();
            
            System.out.println("Cliente " + i);
        }
        System.out.println("");
        
        /**
         * Espera a que finalizen todos los clientes
         */
        for (int i = 0; i < clientes.size(); i++) {
            try {
                clientes.get(i).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("FIN");
        
    }
}
