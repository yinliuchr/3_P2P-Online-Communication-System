import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;

import javax.swing.*;


/**
 * 该类用来监听服务器套接字
 */
class MyServer extends Thread{
    private ServerSocket serverSocket;
    MyServer(ServerSocket s){
        serverSocket = s;
    }

    public void run(){
        try{
            while(true){
                //监听服务器套接字
                Socket socket = serverSocket.accept();
                //若有新的TCP连接，则开始一个新的线程处理与好友的通话
                //socket作为参数传给HandleClient类
                HandleClient task = new HandleClient(socket);
                new Thread(task).start();
            }
        }catch(IOException e){
            System.err.println(e);
        }
    }
}

/**
 * 聊天时服务器的那一端的主要功能用了HandleClient类实现
 */
class HandleClient extends JFrame implements Runnable{
    private Socket socket;
    private JTextArea textArea1 = new JTextArea();          //收到的文字信息
    private JTextArea textArea2 = new JTextArea();          //自己发送区域
    private JButton messageSendButton = new JButton("发送消息");
    private JButton fileSendButton = new JButton("传送文件");
    private JButton closeButton = new JButton("关闭窗口");
    private JScrollPane scrollPane1 = new JScrollPane(textArea1);
    private JScrollPane scrollPane2 = new JScrollPane(textArea2);
    private String current_Dir = System.getProperty("user.dir");
    private ImageIcon Icon = new ImageIcon(current_Dir + File.separator + "pictures" + File.separator + "friendIcon.jpg");

    private JLabel myLabel = new JLabel(Icon);
    private int numberOfGroupChat;         //记录群聊人数
    private String myID = "2014011858";      //我的学号
//    private String myID = "2014011538";      //我的学号
//    private String myID = "2014011547";      //我的学号

    //用于显示群聊时的所有好友
    private DefaultListModel<String> defaultModel = new DefaultListModel<>();
    private JList<String> list = new JList<>(defaultModel);
    private JScrollPane groupScrollPane = new JScrollPane(list);

    //是否断开TCP连接的标志
    private int mark = 0;

    //输入输出流
    private InputStream in;
    private BufferedReader buff;
    private OutputStream output;
    private PrintWriter printWriter;
    private int confirmP2P = -1;
    private int confirmGroup = -1;

    HandleClient(Socket socket){
        //把socket传进来，添加关闭窗口按钮相应
        this.socket = socket;
        MWindowListener windowListner = new MWindowListener();
        addWindowListener(windowListner);
    }

    /**
     * MyServer作为线程运行时的函数
     */
    public void run(){
        try{
            //作为服务器，首先获取输入流
            in = socket.getInputStream();
            buff = new BufferedReader(new InputStreamReader(in));

            //获取输入流之后再建立输出流
            output = socket.getOutputStream();
            printWriter = new PrintWriter(output);

            //对方在发起会话时首先会发送一个会话请求"single_p2p"，只有当用户同意会话请求时，才建立通话对话框
            String require = buff.readLine();                                   //接收好友的会话请求
            if(require.contains("single_p2p")){                                 //如果是单独聊天
                //弹出确认对话框，让用户选择是否接受会话邀请
                String sub = require.substring(0, 10);
                confirmP2P = JOptionPane.showConfirmDialog(null, sub + "好友向你发起会话邀请，是否接受？", "会话邀请", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            }
            else if(require.equals("group_communicate")) {  //如果是群聊邀请
                //弹出确认对话框，让用户选择是否接受群聊邀请
                confirmGroup = JOptionPane.showConfirmDialog(null, "好友向你发起群聊邀请，是否接受？", "会话邀请", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                System.out.println("confirm_group:" + confirmGroup);
            }

        }catch(IOException e){
            System.err.println(e);
        }

        /**
         * 如果用户接收邀请，则建立通话对话框
         */
        if((confirmP2P == JOptionPane.YES_OPTION)|| (confirmGroup == JOptionPane.YES_OPTION)){

            /**
             * p2p聊天
             */
            if(confirmP2P == JOptionPane.YES_OPTION){
                //同意单独通话,创建聊天界面，添加相应按钮
                printWriter.write("ok" + '\n');
                printWriter.flush();
                setLayout(null);

//                Color g1 = new Color(52, 232, 13);
//                Color g2 = new Color(28, 60, 242);
//                Color g3 = new Color(255, 119, 15);
//                Color g4 = new Color(234, 26, 211);
//                Color g5 = new Color(8, 138, 246);
//                Color g6 = new Color(248, 7, 15);

//                JPanel panel = (JPanel) this.getContentPane();
//                panel.setLayout(new BorderLayout(50,50));

                //northPanel
//                JPanel northPanel = new JPanel();
//                northPanel.add(myLabel);
//                northPanel.setSize(500,100);
//                northPanel.setBounds(10,10,200,100);
//                northPanel.setBackground(g1);
//                panel.add(northPanel, BorderLayout.NORTH);
                myLabel.setBounds(10, 10, 100, 100);

                //centerPanel
//                JPanel centerPanel = new JPanel();
                scrollPane1.setBounds(10,120,680,200);
//                centerPanel.add(scrollPane1);
//                scrollPane1.setSize(480,200);
//                centerPanel.setBounds(10, 120, 480, 200);
//                panel.add(centerPanel, BorderLayout.CENTER);

                //southPanel
//                JPanel southPanel = new JPanel(new GridLayout(2,1));
//                JPanel s1 = new JPanel(new GridLayout(1,3));
//                s1.add(messageSendButton);
//                s1.add(fileSendButton);
//                s1.add(closeButton);

//                JPanel s2 = new JPanel();
//                s2.add(scrollPane2);
//                scrollPane2.setSize(480,100);
//                s2.setBounds(10,450, 480, 100);
//                southPanel.add(s1);
//                southPanel.add(s2);
//                panel.add(southPanel, BorderLayout.SOUTH);


                scrollPane2.setBounds(10,350,420,120);
                textArea2.setFont(new Font("楷体", Font.ITALIC, 30));

                messageSendButton.setBounds(460, 350, 160, 40);
                fileSendButton.setBounds(460, 410, 160, 40);
                closeButton.setBounds(460, 470, 160, 40);
                fileSendButton.setFont(new Font("隶书", Font.ITALIC, 30));
                messageSendButton.setFont(new Font("隶书", Font.ITALIC, 30));
                closeButton.setFont(new Font("隶书", Font.ITALIC, 30));
                add(myLabel);
                add(scrollPane1);
                add(scrollPane2);
                add(messageSendButton);
                add(fileSendButton);
                add(closeButton);
//                panel.setBackground(g5);
                this.setSize(720,600);
//                this.setContentPane(panel);
                this.setTitle("P2P Chat");
                this.setLocationRelativeTo(null);
                this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                this.setVisible(true);
                //结束通话按钮添加监听器
                closeButtonListener closeListener = new closeButtonListener();
                closeButton.addActionListener(closeListener);
            }


            /**
             * 群聊
             */
            else{
                //接受群聊,创建群聊页面，添加相应按钮等
                printWriter.write("ok" + '\n');
                printWriter.flush();
                System.out.println("ok");

                initGroupList();                          //初始化群聊对话框的好友列表

                setLayout(null);
                myLabel.setBounds(10,10,100,100);
                scrollPane1.setBounds(10,120,480,200);
                scrollPane2.setBounds(10,350,420,120);
                textArea2.setFont(new Font("楷体", Font.ITALIC, 40));
                messageSendButton.setBounds(460, 350, 200, 60);
                closeButton.setBounds(460, 430, 200, 60);
                groupScrollPane.setBounds(500,120,170,200);
//                fileSendButton.setFont(new Font("隶书", Font.ITALIC, 30));
                messageSendButton.setFont(new Font("隶书", Font.ITALIC, 40));
                closeButton.setFont(new Font("隶书", Font.ITALIC, 40));
                add(myLabel);
                add(scrollPane1);
                add(scrollPane2);
                add(messageSendButton);
                add(closeButton);
                add(groupScrollPane);
                setBackground(Color.MAGENTA);
                setSize(720,600);
                setTitle("Group Chat");
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                setVisible(true);
                //结束通话按钮添加监听器
                closeButtonListener closeListener = new closeButtonListener();
                closeButton.addActionListener(closeListener);
            }

            //启动新的线程接收好友发来的信息
            Receive receive = new Receive();
            new Thread(receive).start();

            //发送消息按键添加监听器
            messageSendButtonListener sendListener = new messageSendButtonListener();
            messageSendButton.addActionListener(sendListener);

            //为“发送文件”按钮添加监听器
            fileSendButtonListener fileListener = new fileSendButtonListener();
            fileSendButton.addActionListener(fileListener);
        }

        /**
         * 如果用户拒绝了对方的聊天请求，首先向对方回复一个拒绝的信号，然后关闭输入输出流，并断开TCP连接
         */
        else{

            try{
                //回复拒绝信号
                printWriter.write("p2p_cancel" + '\n');
                printWriter.flush();

                //断开TCP连接，关闭输入输出流
                socket.close();
                buff.close();
                in.close();
                printWriter.close();
                output.close();
            }catch(IOException ex){
                System.err.println(ex);
            }
        }
    }

    /**
     * 聊天窗口监听器，作为服务器端，不区分群聊和p2p
     */
    private class  MWindowListener implements WindowListener{
        public void windowClosed(WindowEvent event){

        }
        public void windowClosing(WindowEvent event){
            try{
                printWriter.write(myID + "socket_close" + '\n');
                printWriter.flush();

                socket.close();
                buff.close();
                in.close();
                printWriter.close();
                output.close();
                dispose();
            }catch(IOException ignored){}
        }
        public void windowDeiconified(WindowEvent event){}
        public void windowIconified(WindowEvent event){}
        public void windowActivated(WindowEvent event){}
        public void windowDeactivated(WindowEvent event){}
        public void windowOpened(WindowEvent event){}
    }

    /**
     * 消息发送按钮监听器
     */
    private class messageSendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            //获取需要发送的文本，并且在末尾加上换行符
            String send = textArea2.getText() + '\n';
            //写到输出流中
            printWriter.write(send);
            printWriter.flush();
            textArea1.append("Me:" + send);
            textArea2.setText("");
            textArea2.setFont(new Font("楷体", Font.ROMAN_BASELINE, 30));
            textArea1.setFont(new Font("楷体", Font.ROMAN_BASELINE, 30));
        }
    }

    /**
     * 关闭按钮监听器
     */
    private class closeButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                mark = 1;                                       //mark=1表示断开TCP连接
                printWriter.write(myID + "socket_close" + '\n');
                printWriter.flush();
                System.out.println(mark);
                socket.close();
                buff.close();
                in.close();
                printWriter.close();
                output.close();
                dispose();
            }catch(IOException ignored){}
        }
    }

    /**
     * 文件传输按钮监听器
     */
    private class fileSendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            //首先向对方提出发送文件的请求
            printWriter.write("I_want_send_file" + '\n');
            printWriter.flush();
            JOptionPane.showMessageDialog(null, "正在等待对方确认，单击“确认”继续等待", "WAIT", JOptionPane.INFORMATION_MESSAGE);
        }
    }



    /*************************************************************************
     *
     * 下面是处理接收信息的各个内部类
     *
     *************************************************************************/
    
    /**
     * 接收输入流信息的内部类
     */
    private class Receive extends Thread{
        //默认的构造函数
        Receive(){}
        public void run(){
            try{
                while(true){
                    String receive;
                    //需要判断是否是发送文件的请求，如果不是，则视为聊天内容显示在文本域中
                    if(!((receive = buff.readLine()) == null)) {
                        if(receive.equals("I_want_send_file")){
                            //作为文件的接收方，对方请求发送文件
                            //Client在文件发送按钮响应器中发送了这个字符串
                            int confirmFile=JOptionPane.showConfirmDialog(null, "好友请求发送文件，是否接受？", "文件发送", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                            if(confirmFile == JOptionPane.YES_OPTION) {             //允许好友发送文件
                                printWriter.write("I_agree"+'\n');
                                printWriter.flush();
                            }
                            else{                                                   //拒绝好友发送文件
                                printWriter.write("let_down"+'\n');
                                printWriter.flush();
                            }

                        }
                        else if(receive.equals("I_agree")) {
                            //作为文件的发送方,对方允许发送文件
                            //好友已经允许用户发送文件的情况下，用户是否继续发送文件
                            int confirm = JOptionPane.showConfirmDialog(null, "好友已接收您的请求，是否继续？", "文件发送", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                            if(confirm == JOptionPane.YES_OPTION) {  //若用户继续发送文件,开始一个新的线程发送文件

                                FileSend fileSend = new FileSend();
                                new Thread(fileSend).start();
                            }
                        }
                        else if(receive.equals("let_down")) {
                            //作为文件的发送方,对方拒绝发送文件
                            JOptionPane.showMessageDialog(null, "好友拒绝了您的发送请求","文件发送", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else if(receive.equals("socket_close")){
                            JOptionPane.showMessageDialog(null, "好友关闭TCP连接", "连接提醒", JOptionPane.INFORMATION_MESSAGE);
                            socket.close();
                            buff.close();
                            in.close();
                            printWriter.close();
                            output.close();
                            dispose();
                        }else if(!receive.equals("socket_close")&&receive.contains("socket_close")){
                            -- numberOfGroupChat;
                            String sub = receive.substring(0, 10);
                            System.out.println(sub);
                            JOptionPane.showMessageDialog(null, sub + "好友关闭TCP连接", "连接提醒", JOptionPane.INFORMATION_MESSAGE);    //显示一下
                            defaultModel.removeElement(sub);
                            if(numberOfGroupChat == 0){
                                JOptionPane.showMessageDialog(null, "所有好友已退出群聊", "窗口将自动关闭", JOptionPane.INFORMATION_MESSAGE);
                                dispose();
                                socket.close();
                                buff.close();
                                in.close();
                                printWriter.close();
                                output.close();
                                dispose();
                            }
                        }
                        else{
                            //若不是文件发送的相关信息，则作为聊天内容显示在文本域中
                            textArea1.append("Friend:"+receive+'\n');   //最常见的信息发送
                            textArea1.setFont(new Font("楷体", Font.ROMAN_BASELINE, 30));
                        }
                    }
                }
            }catch(IOException ex){
                System.err.println(ex);
            }
        }
    }

    /**
     * 文件传输类，继承父类Thread，与实行Runnable方法一样，可作为线程运行使用
     */
    private class FileSend extends Thread{
        private InetAddress inetAddress;
        private String studentIP;                //对方的IP地址

        private JFileChooser jChooser;          //用JFileChooser创建打开文件对话框
        private File file;
        private BufferedInputStream fileIn;     //文件输入流
        private Socket fileSocket;              //建立发送文件的TCP连接
        private OutputStream fileOut;           //文件输出流
        private BufferedOutputStream fileBuff;

        FileSend() {                                //对方的IP地址
            inetAddress = socket.getInetAddress();
            studentIP = inetAddress.getHostAddress();
        }

        void openFile() {                           //打开文件，将文件输入流关联到文件上
            jChooser = new JFileChooser(new File("."));
            if(jChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                file = jChooser.getSelectedFile();
                try{
                    //建立文件的输入流
                    fileIn = new BufferedInputStream(new FileInputStream(file));
                }catch(IOException ex){
                    System.out.println("Error opening" + file.getName());
                }
            }
        }

        public void run(){
            try{
                fileSocket = new Socket(studentIP,15007);    //文件发送的TCP连接建立
                System.out.println(studentIP);
                fileOut = fileSocket.getOutputStream();     //建立输出流
                fileBuff = new BufferedOutputStream(fileOut);
                openFile();
                int r;
                while((r = fileIn.read()) != -1) {  //以字节为单位发送文件
                    fileBuff.write((byte)r);
                }
                //文件发送完毕后关闭输入输出流，并断开TCP连接
                fileBuff.close();
                fileOut.close();
                fileIn.close();
                fileSocket.close();
            }catch(IOException ex){
                System.err.println(ex);
            }

            //文件发送结束后，再追加一个结束信号
            printWriter.write("file_been_sent" + '\n');
            printWriter.flush();
            JOptionPane.showMessageDialog(null, "文件成功发送", "文件发送", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 初始化群聊对话框的好友列表
     */
    private void initGroupList(){
        try{
            numberOfGroupChat = 0;
            while(true){
                String require;
                if(!((require = buff.readLine()) == null)){
                    System.out.println("Client info: " + require);
                    if(require.equals("info_over")){
                        //AsClient中，broadcast会广播info_over信息来结束
                        break;
                    }
                    else{
                        //AsClient中，会广播自己的学号，在这里把学号作为列表显示
                        defaultModel.addElement(require);
                        ++ numberOfGroupChat;
                    }
                }
            }
        }catch(IOException ignored){}
    }
}