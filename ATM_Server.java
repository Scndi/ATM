import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ATM_Server {
    static String clientSentence; //客户端传来的消息
    static String serverSentence; //服务器段返回的消息
    static String userid;
    static String passwd;
    static double balance;
    static ServerSocket serverSocket;
    static Socket connectionSocket;
    static String url = "jdbc:mysql://localhost:3306/ATM";
    static String user = "root";
    static String dbPassword = "20040619yl...";
    static String driverName = "com.mysql.cj.jdbc.Driver";

    static Connection conn;



    //第一个大循环每次都会等待一个新用户连接，等到连接后才会进行下面的操作
    public static void main(String args[]) throws Exception {
        serverSocket = new ServerSocket(2525);
        while (true) {
            connectionSocket = serverSocket.accept();
            System.out.println("有客户端连接！");
            while (true) {
                //阶段一：等待并接收客户端的请求连接
                //初始化各个变量

                clientSentence = null;
                serverSentence = null;
                userid = null;
                passwd = null;
                balance = 0;

                //连接数据库
                Class clazz = Class.forName(driverName);
                Driver driver = (Driver) clazz.newInstance();
                DriverManager.registerDriver(driver);
                conn = DriverManager.getConnection(url, user, dbPassword);
                write_Log(get_Now());
                //阶段二：进行打招呼阶段
                while (true) {

                    //读取用户端传来的信息
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromClient.readLine();
                    write_Log(clientSentence);
                    //如果输入是BYE，则直接断开连接,跳到阶段一
                    if (clientSentence.equals("BYE")) {
                        break;
                    } else if (clientSentence.matches("^HELO\\s\\d+$")) {
                        userid = clientSentence.substring(5);
                        //从数据库中读取对应的账号密码
                        try {
                            String query = "SELECT passwd, balance FROM user WHERE userid = ?";
                            PreparedStatement statement = conn.prepareStatement(query);
                            statement.setString(1, userid);

                            ResultSet res = statement.executeQuery();

                            if (!res.next()) {
                                System.out.println("找不到账号！");
                                say_Error();
                                continue;
                            } else {
                                System.out.println("收到用户申请,申请的账号为" + userid);
                                serverSentence = "500 AUTH REQUIRE" + '\n';
                                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                                outToClient.writeBytes(serverSentence);
                                write_Log(serverSentence);
                                System.out.println("已向其请求密码或其他指令");

                                // 获取密码和余额
                                passwd = res.getString("passwd");
                                balance = res.getDouble("balance");

                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        say_Error();
                        continue;
                    }
                }
                //如果跳出打招呼阶段时的客户信息时BYE，则断开连接并回复给客户端BYE
                if (clientSentence.equals("BYE")) {
                    say_Bye();
                    break;
                }
                //阶段三:验证密码
                while (true) {
                    //读取用户端传来的信息
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromClient.readLine();
                    write_Log(clientSentence);
                    //如果输入是BYE，则直接断开连接,跳到阶段一
                    if (clientSentence.equals("BYE")) {
                        break;
                    } else if (clientSentence.matches("^PASS\\s\\d+$")) {
                        String in_passwd = clientSentence.substring(5);
                        if (!in_passwd.equals(passwd)) {
                            System.out.println("密码错误!");
                            say_Error();
                            continue;
                        } else {
                            serverSentence = "525 OK!" + '\n';
                            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(serverSentence);
                            System.out.println("已经完成打招呼阶段!");
                            write_Log(serverSentence);
                            break;
                        }
                    } else {
                        say_Error();
                        continue;
                    }
                }
                if (clientSentence.equals("BYE")) {
                    say_Bye();
                    break;
                }
                //阶段四:服务阶段
                while (true) {
                    //读取用户端传来的信息
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromClient.readLine();
                    write_Log(clientSentence);
                    //如果输入是BYE，则直接断开连接,跳到阶段一
                    if (clientSentence.equals("BYE")) {
                        break;
                    } else if (clientSentence.equals("BALA")) {
                        System.out.println("用户请求查询余额!");
                        serverSentence = "AMNT:" + balance + '\n';
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                        outToClient.writeBytes(serverSentence);
                        write_Log(serverSentence);
                        System.out.println("已回复用户的余额");
                    } else if (clientSentence.matches("^WDRA\\s\\d+(\\.\\d+)?$")) {
                        String amnt = clientSentence.substring(5);
                        double amnt1 = Double.parseDouble(amnt);
                        if (amnt1 > balance) {
                            System.out.println("余额不足!");
                            say_Error();
                            System.out.println("已向其重新请求消息!");
                        } else {
                            balance -= amnt1;
                            serverSentence = "525 OK!" + '\n';
                            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(serverSentence);
                            write_Log(serverSentence);
                            System.out.println("取款成功，余额为" + balance);
                            continue;
                        }
                    } else {
                        say_Error();
                    }

                }
                say_Bye();
                break;
            }
        }


    }

    public static void say_Bye() throws IOException, SQLException {
        serverSentence = "BYE" + '\n';
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        outToClient.writeBytes(serverSentence);
        serverSocket.close();
        write_Log(serverSentence);
        System.out.println("已断开连接跟该客户的连接");
        try {
            String query = "update user SET balance = ? where userid = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setDouble(1, balance);
            statement.setString(2, userid);
            int rowsAffected = statement.executeUpdate();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void say_Error() throws IOException {
        serverSentence = "401 ERROR!" + '\n';
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        outToClient.writeBytes(serverSentence);
        write_Log(serverSentence);
    }
    public static  void write_Log(String sentence) throws IOException {
        FileWriter fileWriter = new FileWriter("log.txt",true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(sentence+'\n');
        bufferedWriter.close();
    }
    public static String get_Now()
    {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化时间并输出
        String formattedTime = currentTime.format(formatter);

        return formattedTime;
    }
}
