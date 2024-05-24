package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


public class ConnectionWindow extends JFrame {
    private JTextField ipTextField;
    private JTextField portTextField;

    public ConnectionWindow() {
        super("Connection Window");

        // 设置窗口大小和布局
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        // 添加 IP 地址标签和文本框
        add(new JLabel("IP Address:"));
        ipTextField = new JTextField();
        add(ipTextField);

        // 添加端口号标签和文本框
        add(new JLabel("Port:"));
        portTextField = new JTextField();
        add(portTextField);

        // 添加一个空标签占位，使按钮位于第三行中间
        add(new JLabel());

        // 添加连接按钮
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip = ipTextField.getText();
                String port = portTextField.getText();
                dispose();
                try {
                    TalkWindow talkWindow = new TalkWindow(ip,Integer.parseInt(port));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        add(connectButton);
        // 将窗口位置设置为屏幕中间
        setLocationRelativeTo(null);

        // 显示窗口
        setVisible(true);
    }

    public static void main(String[] args) {
        // 创建连接窗口对象
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ConnectionWindow();
            }
        });
    }
}