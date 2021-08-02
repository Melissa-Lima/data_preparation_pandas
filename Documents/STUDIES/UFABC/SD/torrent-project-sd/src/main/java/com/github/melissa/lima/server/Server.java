package com.github.melissa.lima.server;

import com.github.melissa.lima.message.Message;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.INEG;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.melissa.lima.message.Message.Type.*;

public class Server {
    private Gson gson = new Gson(); // texto -> json | json -> texto
    private Thread receiver;
    private Thread answerer;
    private Thread checker;
    private Map<String, List<String>> peerFiles;
    int port = 10098;
    int alivePort = 10099;
    InetAddress address = InetAddress.getByName("127.0.0.1"); // localhost

    public Server() throws UnknownHostException {
        peerFiles = new HashMap<>();
        this.receiver = new Thread() {
            @Override
            public void run() {
                receive();
            }
        };

        this.answerer = new Thread() {
            @Override
            public void run() {
                answer();
            }
        };

        this.checker = new Thread() {
            @Override
            public void run() {
                try {
                    checkAlive();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void cleanBuffer(byte buff[]) {
        for (int i = 0; i < buff.length; i++) {
            buff[i] = 0;
        }
    }

    public void join(InetAddress address, int port, List<String> files) throws Exception {
        this.peerFiles.put(address.toString()+":"+port, files);
        sendMessage("JOIN_OK", address, port);
    }

    public void leave(InetAddress address, int port) throws Exception {
        this.peerFiles.remove(address.toString()+":"+port);
        sendMessage("LEAVE_OK", address, port);
    }

    public void search(InetAddress address, int port, String fileName) throws Exception {
        List<String> peers = new LinkedList<>();

        for(Map.Entry<String, List<String>> i:peerFiles.entrySet()) {
            if(i.getValue().contains(fileName)) {
                peers.add(i.getKey());
            }
        }

        String answer = "[" + String.join(", ",peers) + "]";
        sendMessage("SEARCH_OK: "+ answer, address, port);
    }

    public void update(InetAddress address, int port, String fileName) throws Exception {
        List<String> currentFiles = peerFiles.get(address.toString()+":"+port);
        currentFiles.add(fileName);
        peerFiles.put(address.toString()+":"+port, currentFiles);
        sendMessage("UPDATE_OK", address, port);
    }

    public void checkAlive() throws Exception {

        while(true) {
            Set<String> peers = peerFiles.keySet();
            for(String peer: peers) {
                InetAddress address = InetAddress.getByName(peer.split(":")[0]);
                int port = Integer.parseInt(peer.split(":")[1]);
                sendMessage("ALIVE", address, port);

                try {
                    String s = CompletableFuture.supplyAsync(() -> receiveAlive(address, port)).get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    peerFiles.remove(peer);
                }
            }

            Thread.sleep(30000);
        }
    }

    public String receiveAlive(InetAddress address, int port) {
        DatagramSocket socket;
        DatagramPacket packet;
        String jsonMessage;
        byte buffer[] = new byte[10000];

        try {
            socket = new DatagramSocket(this.alivePort);

            packet = new DatagramPacket(buffer, buffer.length, address, alivePort);

            socket.receive(packet);

            jsonMessage = new String(packet.getData()).trim();

            if(!jsonMessage.equals("ALIVE_OK")) {
                peerFiles.remove(address.toString() + ":"+ port);
            }

            cleanBuffer(buffer);

        } catch (Exception ex) {
            return "not_ok";
        }

        return "ok";
    }

    public void processMessage(Message m) throws Exception {
        switch (m.getType()) {
            case JOIN:
                join(m.getAddress(), m.getPort(), m.getPeerFiles());
                break;
            case LEAVE:
                leave(m.getAddress(), m.getPort());
                break;
            case SEARCH:
                search(m.getAddress(), m.getPort(), m.getFileName());
                break;
            case UPDATE:
                update(m.getAddress(), m.getPort(), m.getFileName());
                break;
        }
    }

    public void receive() {
        DatagramSocket socket;
        DatagramPacket packet;
        byte buff[] = new byte[10000];
        String jsonMessage;
        Message message;

        try {
            socket = new DatagramSocket(this.port);
            while (true) {
                packet = new DatagramPacket(buff, buff.length, this.address, this.port);

                socket.receive(packet);

                jsonMessage = new String(packet.getData()).trim();
                message = gson.fromJson(jsonMessage, Message.class);

                processMessage(message);

                cleanBuffer(buff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void answer() {

    }

    public void sendMessage(String text, InetAddress address, int port) throws Exception {
        byte buffer[] = text.getBytes(); // transformando o texto em bytes
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port); // cria um pacote
        DatagramSocket socket = new DatagramSocket(); // criando um socket (conexao)

        socket.send(packet); // envia o pacote
        socket.close(); // encerra conexao
    }
}
