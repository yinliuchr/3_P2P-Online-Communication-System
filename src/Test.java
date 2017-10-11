import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Test extends JFrame{

        private JTextField studentID = new JTextField("2014011858");              //用户名
//    private JTextField studentID = new JTextField("2014011538");                    //用户名
//    private JTextField studentID = new JTextField("2014011547");                  //用户名

    private JPasswordField pwd = new JPasswordField("net2016");     //密码

    private JTextField serverIP = new JTextField("166.111.140.14");    //服务器IP地址

    private JTextField jServerPort = new JTextField("8000");            //服务器端口号

    /**
     * 登陆按钮的监听器类
     */
    private class LoginActionListener implements ActionListener{
        Socket socket;
        public void actionPerformed(ActionEvent e){

            String ID = studentID.getText();
            String password = new String(pwd.getPassword());
            String messageSent = ID + "_" + password;
            String serverip = serverIP.getText();
            int serverPort = Integer.parseInt(jServerPort.getText());
            try{
                socket = new Socket(serverip, serverPort);

                OutputStream toServer = socket.getOutputStream();   //输出流
                toServer.write(messageSent.getBytes());
                toServer.flush();

                InputStream in = socket.getInputStream();           //输入流
                String strIn;
                byte[] answer;
                while(true){
                    if(in.available() != 0){                        //读到信息
                        answer = new byte[in.available()];
                        in.read(answer);
                        strIn = new String(answer);
                        break;
                    }
                }

                System.out.println(strIn);
                if(!strIn.equals("lol")){                             //不是lol
                    System.out.println(strIn);
                    JOptionPane.showMessageDialog(null, "用户名或密码错误",
                            "ERROR", JOptionPane.INFORMATION_MESSAGE);
                }
                else{                                              //是lol
                    MainBoard mainboard = new MainBoard(ID, serverip, serverPort);
                    dispose();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }finally{
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    /**
     * 构造函数中初始化界面
     */
    private Test(){
        Color a = new Color(237, 249, 156);

        this.setSize(1000, 800);
        this.setTitle("Log In");
        JPanel panel = new JPanel();
        this.setContentPane(panel);

        panel.setLayout(new BorderLayout(50,50));
        panel.setBackground(a);

        String currentDir = System.getProperty("user.dir");
        ImageIcon icon0 = new ImageIcon(currentDir + File.separator + "pictures" + File.separator + "background.jpg");
        JLabel label0 = new JLabel(icon0);
        label0.setSize(this.getSize());
        label0.setLocation(0,0);
        label0.setBackground(Color.YELLOW);
        panel.add(label0,BorderLayout.NORTH);


        ImageIcon icon1 = new ImageIcon(currentDir + File.separator + "pictures" + File.separator + "ai.jpg");
        JLabel lb2 = new JLabel(icon1);

        panel.add(lb2, BorderLayout.WEST);


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4,2));
        centerPanel.setBackground(Color.YELLOW);

        studentID.setBackground(Color.YELLOW);
        pwd.setBackground(Color.YELLOW);
        serverIP.setBackground(Color.YELLOW);
        jServerPort.setBackground(Color.YELLOW);
        JButton log_in = new JButton("登陆");
        log_in.setBackground(Color.CYAN);

        JLabel jj1 = new JLabel("UserName");
        JLabel jj3 = new JLabel("UserPWD");
        JLabel jj5 = new JLabel("ServerIp");
        JLabel jj7 = new JLabel("ServerPort");
        jj1.setFont(new Font("楷体",Font.BOLD,30));
        jj3.setFont(new Font("楷体",Font.BOLD,30));
        jj5.setFont(new Font("楷体",Font.BOLD,30));
        jj7.setFont(new Font("楷体",Font.BOLD,30));
        studentID.setFont(new Font("楷体",Font.BOLD,30));
        pwd.setFont(new Font("宋体", Font.PLAIN, 30));
        serverIP.setFont(new Font("楷体",Font.BOLD,30));
        jServerPort.setFont(new Font("楷体",Font.BOLD,30));

        centerPanel.add(jj1);
        centerPanel.add(studentID);
        centerPanel.add(jj3);
        centerPanel.add(pwd);
        centerPanel.add(jj5);
        centerPanel.add(serverIP);
        centerPanel.add(jj7);
        centerPanel.add(jServerPort);

        panel.add(centerPanel, BorderLayout.CENTER);


//        panel.add(null,BorderLayout.EAST);

        log_in.setSize(200,50);
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(log_in,CENTER_ALIGNMENT);
        log_in.setFont(new Font("楷体", Font.BOLD,50));
        panel.add(southPanel, BorderLayout.SOUTH);

        LoginActionListener listener = new LoginActionListener();      //为按钮添加监听器
        log_in.addActionListener(listener);
    }


    /**
     * 主函数入口
     * @param args
     */
    public static void main(String[] args) {
        Test frame = new Test();
//        frame.setBackground(Color.GREEN);
//        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}