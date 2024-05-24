package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TalkWindow extends JFrame {
    String ip;

    int port;
    private JTextArea textArea;
    private JTextField inputField;
    String clientsentence;
     String serverSentence ;
    Socket clientSocket;

    public TalkWindow(String ip1,int port1) throws IOException {
        super("Talk Window");
        this.ip = ip1;
        this.port = port1;

        //连接窗口
        try {
             clientSocket = new Socket(ip, port);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // 设置窗口大小和布局
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建文本区域并设置为不可编辑
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // 添加输入文本框和按钮
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("发送");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clientsentence = inputField.getText();
                DataOutputStream outToServer = null;
                try {
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    outToServer.writeBytes(clientsentence + '\n');
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                BufferedReader inFromServer = null;
                try {
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    serverSentence = inFromServer.readLine();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                textArea.append(clientsentence + "\n");
                textArea.append(serverSentence + "\n");
                inputField.setText(""); // 清空输入文本框
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // 显示窗口
        setLocationRelativeTo(null); // 将窗口位置设置为屏幕中间
        setVisible(true);
    }



}