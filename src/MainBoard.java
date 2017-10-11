import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.ArrayList;

class MainBoard extends JFrame {
    private String fitServerIP;                                         //服务器IP
    private int fitServerPort;                                          //服务器端口号
    private ArrayList<String> studentIP = new ArrayList<>();                   //保存列表中同学的IP地址
    private ArrayList<String> studentID = new ArrayList<>();                   //保存列表中同学的学号
    private ArrayList<String> studentName = new ArrayList<>();                 //保存列表中同学的姓名
    private String currentDir = System.getProperty("user.dir");
    private ImageIcon myIcon = new ImageIcon(currentDir + File.separator + "pictures" + File.separator + "ai.jpg");
    private ImageIcon studentIcon = new ImageIcon(currentDir + File.separator + "pictures" + File.separator + "studentIcon.jpg");
    private String myID;                            //用户的学号

    //群聊或p2p聊天的标志
    private boolean p2p = true;                                   //当p2p = true时表示单独聊天，当p2p=false时表示群聊

    //新建一个列表
    private DefaultListModel<Object[]> defaultListModel = new DefaultListModel<>();
    private JList<Object[]> list0 = new JList<>(defaultListModel);
    private ListCellRenderer renderer = new MyListCellRenderer();

    //将列表放入一个滚动窗格中
    private JScrollPane scrollPane = new JScrollPane(list0);

    //添加按键
    private JButton newFriendButton = new JButton("添加好友");        //添加好友
    private JButton deleteFriendButton = new JButton("删除好友");        //删除好友
    private JButton leaveButton = new JButton("退出");        //下线
    private JButton p2pButton = new JButton("发起会话");        //双人聊
    private JButton groupChatButton = new JButton("群聊");        //群聊


    /**
     * 主面板构造函数
     * @param ID
     * @param IP
     * @param Port
     */
    MainBoard(String ID, String IP, int Port){
        this.myID = ID;
        this.fitServerIP = IP;
        this.fitServerPort = Port;

        initialLayout(ID);         //初始化布局

        MWindowListener windowListener = new MWindowListener();
        addWindowListener(windowListener);

        //在49876号端口创建一个服务器套接字，等待好友发起会话
        try{
            ServerSocket serverSocket1 = new ServerSocket(49876);   //在49876号端口创建套接字
            //在一个线程中监听是否有TCP连接
            MyServer server = new MyServer(serverSocket1);
            new Thread(server).start();                             //开启新线程

            ServerSocket serverSocket2 = new ServerSocket(32524);   //在32524号端口创建套接字等待接收文件
            FileReceive freceive = new FileReceive(serverSocket2);  //文件接收类
            new Thread(freceive).start();                           //开启新线程
        }catch(IOException e){
            System.err.println(e);
        }

        //为添加好友按键添加监听器
        NewFriendButtonListener listener = new NewFriendButtonListener();
        newFriendButton.addActionListener(listener);

        //为删除好友按钮添加监听器
        deleteFriendButtonListener delListener = new deleteFriendButtonListener();
        deleteFriendButton.addActionListener(delListener);

        //为退出按钮添加监听器
        leaveButtonListener lvListener = new leaveButtonListener();
        leaveButton.addActionListener(lvListener);

        //为群聊按键添加监听器
        groupChatButtonListener groupListener = new groupChatButtonListener();
        groupChatButton.addActionListener(groupListener);

        //为发起聊天按键添加监听器
        P2PchatListener p2pl = new P2PchatListener();
        p2pButton.addActionListener(p2pl);
    }

    /**
     * 初始化布局
     */
    private void initialLayout(String ID){
        Color c1 = new Color(52, 232, 13);
        Color c2 = new Color(28, 60, 242);
        Color c3 = new Color(255, 119, 15);
        Color c4 = new Color(234, 26, 211);
        Color c5 = new Color(8, 138, 246);
        Color c6 = new Color(248, 7, 15);

        JPanel panel = (JPanel) this.getContentPane();

        panel.setLayout(new BorderLayout(50,50));

        list0.setCellRenderer(renderer);

        //设置自己头像的位置
        JPanel northPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel(ID, myIcon, SwingConstants.RIGHT);
        label.setFont(new Font("宋体", Font.BOLD, 30));
        northPanel.add(label,CENTER_ALIGNMENT);
        panel.add(northPanel, BorderLayout.NORTH);



        //设置滚动窗格的位置
        panel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setFont(new Font("隶书", Font.ITALIC, 30));

        //设置按键的位置
        JPanel southPanel = new JPanel(new GridLayout(5,1,30,30));

        southPanel.add(newFriendButton);
        southPanel.add(p2pButton);
        southPanel.add(groupChatButton);
        southPanel.add(deleteFriendButton);
        southPanel.add(leaveButton);

        newFriendButton.setFont(new Font("楷体", Font.ITALIC, 30));
        deleteFriendButton.setFont(new Font("楷体", Font.ITALIC, 30));
        groupChatButton.setFont(new Font("楷体", Font.ITALIC, 30));
        p2pButton.setFont(new Font("楷体", Font.ITALIC, 30));
        leaveButton.setFont(new Font("楷体", Font.ITALIC, 30));

        newFriendButton.setForeground(c1);
        deleteFriendButton.setForeground(c2);
        groupChatButton.setForeground(c3);
        p2pButton.setForeground(c4);
        leaveButton.setForeground(c5);

        panel.add(southPanel, BorderLayout.SOUTH);

        panel.setFont(new Font("楷体", Font.ITALIC, 30));
        panel.setBackground(Color.yellow);
        setSize(720, 1200);
        setTitle(myID);
//        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * 窗口监听器
     */
    private class MWindowListener implements WindowListener{
        public void windowClosed(WindowEvent event){}
        public void windowClosing(WindowEvent event){
            try{                 //与服务器建立TCP连接退出
                Socket socket = new Socket(fitServerIP,fitServerPort);
                OutputStream toServer = socket.getOutputStream();
                String leaf = "logout" + myID;
                toServer.write(leaf.getBytes());
                toServer.flush();
                InputStream fromServer = socket.getInputStream();
                String ACK;
                while(true){
                    if(fromServer.available() != 0){
                        byte[] answer = new byte[fromServer.available()];
                        fromServer.read(answer);
                        ACK = new String(answer);
//                        System.out.print(ACK);
                        break;
                    }
                }
                toServer.close();
                fromServer.close();
                socket.close();
                if(ACK.equals("loo")){
                    System.out.println("I've logged out.");
                    System.exit(0);
                }
                else{
                    JOptionPane.showMessageDialog(null, "退出程序出现错误，请稍后重试！", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                }
            }catch(IOException ignored){}
        }
        public void windowDeiconified(WindowEvent event){}
        public void windowIconified(WindowEvent event){}
        public void windowActivated(WindowEvent event){}
        public void windowDeactivated(WindowEvent event){}
        public void windowOpened(WindowEvent event){}
    }

    /**
     * 添加好友按钮监听
     */
    private class NewFriendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            String fname;                                   //接收输入的添加人姓名
            String stuID;                                   //接收输入的添加人学号
            while(true){
                fname = JOptionPane.showInputDialog("请输入好友姓名");
                if(fname.equals("")) JOptionPane.showMessageDialog(null, "姓名不能为空,请重新输入", "ERROR", JOptionPane.INFORMATION_MESSAGE);  //添加人姓名不能为空
                else break;
            }
            while(true){
                stuID = JOptionPane.showInputDialog("请输入好友学号");
                if(stuID.equals("")) JOptionPane.showMessageDialog(null, "学号不能为空,请重新输入", "ERROR", JOptionPane.INFORMATION_MESSAGE);  //添加人学号不能为空
                else break;
            }

            //测试好友是否在线
            TestConnect connection = new TestConnect(fitServerIP, fitServerPort, stuID);
            String str = connection.connect();
            //判断服务器返回的信息：可能好友在线，可能好友离线，也可能输入的学号错误
            String studentInfo;
            if(str.equals("n")){                                                    //好友不在线
                studentInfo = fname + "(offLine)";
                defaultListModel.addElement(new Object[]{studentIcon, studentInfo});             //添加表项
                studentIP.add(str);                                              //保存好友的ip，用于发起会话
                studentID.add(stuID);                                            //保存好友的学号
                studentName.add(fname);
            }
            else if(str.charAt(0) == 'P' || str.charAt(0) == 'I'){                                      //如果添加人学号错误，则不添加
                JOptionPane.showMessageDialog(null, "学号不正确，请重新添加!", "ERROR", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                studentInfo = fname + "(online)" + str;
                defaultListModel.addElement(new Object[]{studentIcon, studentInfo});
                studentIP.add(str);
                studentName.add(fname);
                studentID.add(stuID);
            }
        }
    }

    /**
     * 测试好友是否在线
     */
    private class TestConnect {
        private String IP;
        private String stdID;
        private int port;
        TestConnect(String ip, int port, String stdID){
            this.IP = ip;
            this.port = port;
            this.stdID = stdID;
        }

        String connect(){
            String str = null;
            try{
                Socket socket = new Socket(IP,port);
                OutputStream toServer = socket.getOutputStream();
                InputStream input = socket.getInputStream();
                String request = "q" + stdID;                               //发送q + ID测试好友是否在线
                toServer.write(request.getBytes());
                toServer.flush();
                while(true){
                    if(input.available() != 0){
                        byte[] answer = new byte[input.available()];
                        input.read(answer);
                        str = new String(answer);
                        break;
                    }
                }
                socket.close();
                toServer.close();
                input.close();
            }catch(IOException ex){
                System.err.println(ex);
            }
            return str;                                                     //如果在线返回IP地址,不在线返回n
        }
    }

    /**
     * 发起会话按钮监听
     */
    private class P2PchatListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            p2p = true;                                                   //单独P2P聊天
            int index = list0.getSelectedIndex();                           //选中好友
            if(index != -1){
                //获取好友学号及IP地址
                String fID = studentID.get(index);
                String fip = studentIP.get(index);
                String fname = studentName.get(index);
                //通话前测试好友是否在线
                TestConnect connection = new TestConnect(fitServerIP,fitServerPort,fID);
                String str = connection.connect();
                //判断服务器返回的信息：可能好友在线，可能好友离线，也可能输入的学号错误

                //好友离线
                if(str.equals("n")) JOptionPane.showMessageDialog(null, "offline!", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                    //学号错误
                else if(str.charAt(0) == 'P' || str.charAt(0) == 'I') JOptionPane.showMessageDialog(null, "学号错误", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                    //好友在线
                else{
                    //开启P2P聊天的新线程
                    MyClient client = new MyClient(fip, fID, fname, myID, p2p);
                    new Thread(client).start();
                }
            }

            else JOptionPane.showMessageDialog(null, "未选中任何条目!", "ERROR", JOptionPane.INFORMATION_MESSAGE);

        }
    }

    /**
     * 群聊按钮监听
     */
    private class groupChatButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            p2p = false;                                  //表示群聊
            int[] indices = list0.getSelectedIndices();     //选中好友
            ArrayList<String> fIP = new ArrayList<>();                //选中好友的IP数组
            ArrayList<String> fID = new ArrayList<>();                //选中好友的ID数组
            ArrayList<String> fnm = new ArrayList<>();                //选中好友的姓名数组
            String stuID;
            String stuIP;
            String stuName;
            if(indices.length >= 1){
                for (int indice : indices) {
                    //获取群聊好友的学号
                    stuID = studentID.get(indice);
                    stuIP = studentIP.get(indice);
                    stuName = studentName.get(indice);
                    //测试好友是否在线
                    TestConnect connection = new TestConnect(fitServerIP, fitServerPort, stuID);
                    String str = connection.connect();
                    //判断服务器返回的信息：可能好友在线，可能好友离线，也可能输入的学号错误
                    // 好友离线
                    if (str.equals("n"))
                        JOptionPane.showMessageDialog(null, stuID + "offline!", "ERROR", JOptionPane.INFORMATION_MESSAGE);

                        //如果学号错误
                    else if (str.charAt(0) == 'P' || str.charAt(0) == 'I')
                        JOptionPane.showMessageDialog(null, "学号错误!", "ERROR", JOptionPane.INFORMATION_MESSAGE);

                        //好友在线
                    else {
                        fIP.add(stuIP);        //若好友在线则保存好友ip地址
                        fID.add(stuID);        //保存好友ID
                        fnm.add(stuName);
                    }
                }
                if(fIP.size() != 0){
                    MyClient groupClient = new MyClient(fIP, fID, fnm, myID, p2p);  //开始群聊
                    new Thread(groupClient).start();                            //开启新线程
                }
            }


            else JOptionPane.showMessageDialog(null, "未选中任何条目!", "ERROR", JOptionPane.INFORMATION_MESSAGE);

        }
    }

    /**
     * 删除按钮监听
     */
    private class deleteFriendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int index = list0.getSelectedIndex();     //选中要删除的好友
            if (index == -1){
                //如果没有选中任何条目，弹出提示信息
                JOptionPane.showMessageDialog(null, "未选中任何条目!", "ERROR", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                defaultListModel.remove(index);         //删除好友信息
                studentIP.remove(index);
                studentID.remove(index);
            }
        }
    }

    /**
     * 退出按钮监听
     */
    private class leaveButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{   //与服务器建立TCP连接退出
                Socket socket = new Socket(fitServerIP, fitServerPort);
                OutputStream toserver = socket.getOutputStream();
                String leaf = "logout" + myID;
                toserver.write(leaf.getBytes());
                toserver.flush();
                InputStream fromserver = socket.getInputStream();
                String logout;
                while(true){
                    if(fromserver.available() != 0){
                        byte[] answer = new byte[fromserver.available()];
                        fromserver.read(answer);
                        logout = new String(answer);
                        break;
                    }
                }
                toserver.close();
                fromserver.close();
                socket.close();
                if(logout.equals("loo")){
                    System.out.println("jbnjbnjbn");
                    System.exit(0);
                }
                else{
                    JOptionPane.showMessageDialog(null, "退出程序出现错误，请稍后重试！", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                }
            }catch(IOException ignored){}
        }
    }

    
    /**
     * 接收文件
     */
    private class FileReceive extends Thread{
        private ServerSocket serverSocket;
        FileReceive(ServerSocket skt){
            serverSocket = skt;
        }
        public void run(){
            try{
                while(true){
                    //监听服务器套接字
                    Socket socket = serverSocket.accept();
                    //若有新的TCP连接，则开始一个新的线程处理与好友的通话
                    HandleReceiveFile task = new HandleReceiveFile(socket);
                    new Thread(task).start();
                }
            }catch(IOException ex){
                System.err.println(ex);
            }
        }
    }

    
    /**
     * 处理接收的文件
     */
    private class HandleReceiveFile implements Runnable{
        private Socket socket;
        private JFileChooser fileChooser = new JFileChooser(new File("."));    //用JFileChooser创建保存文件对话框
        private BufferedInputStream fileIn;                                     //输入流
        private BufferedOutputStream fileOut;                                   //输出流
        private File file;                                                      //创建一个File的实例，用来保存最后的文件

        HandleReceiveFile(Socket socket){
            this.socket = socket;
        }

        void saveFile(){
            if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                file = fileChooser.getSelectedFile();                              //返回从对话框中选定的文件或者当前路径
                //将输出流与文件相关联
                try{
                    fileOut = new BufferedOutputStream(new FileOutputStream(file));
                }catch(IOException ex){
                    System.out.println("Error saving" + file.getName());
                }
            }
        }
        
        public void run(){
            try{
                //获取输入流
                InputStream in = socket.getInputStream();
                fileIn = new BufferedInputStream(in);
                saveFile();
                int r;
                JOptionPane.showMessageDialog(null, "文件正在接收中，请稍候", "文件发送", JOptionPane.INFORMATION_MESSAGE);
                while((r = fileIn.read()) != -1) {    //以字节为单位写到文件中
                    fileOut.write((byte)r);
                }
                //关闭输入输出流，断开TCP连接
                JOptionPane.showMessageDialog(null, "文件已成功接收", "文件发送", JOptionPane.INFORMATION_MESSAGE);
                fileIn.close();
                in.close();
                socket.close();
                fileOut.close();
            }
            catch(IOException ex){
                System.out.println("Error receiving" + file.getName());
            }
        }
    }



    
    /**
     * 自定义列表单元格绘制器，能够在list0列表中同时显示图标和文本（参考学长代码）
     */
    private class MyListCellRenderer implements ListCellRenderer{
        private JLabel listCell = new JLabel("", JLabel.LEFT);
        private Border lineBorder = BorderFactory.createLineBorder(Color.black, 1);     //定义一条线边界
        private Border nullBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);       //定义一个空边界
        //继承getListCellRendererComponent方法
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
            Object[] pair = (Object[])value;  //将value转换为一个数组
            listCell.setOpaque(true);
            listCell.setIcon((ImageIcon)pair[0]);
            listCell.setText(pair[1].toString());
            if(isSelected){
                listCell.setForeground(Color.MAGENTA);
                listCell.setBackground(Color.CYAN);
            }
            else{
                listCell.setForeground(Color.BLACK);
                listCell.setBackground(Color.WHITE);
            }
            listCell.setBorder(cellHasFocus? lineBorder : nullBorder);
            return listCell;
        }
    }

    

}