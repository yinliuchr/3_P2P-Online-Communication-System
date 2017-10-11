import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;


/**
 * 率先发起通信的是客户端
 */
class MyClient extends JFrame implements Runnable{
    private Socket socket;
    private String studentIP;
    private String studentName;
    private String userID;
    private boolean IsP2P;                              //群聊或者p2p的标志

    private ArrayList<String> studentsIP = new ArrayList<>();      //群聊时保存所有群聊同学的ip地址
    private ArrayList<String> studentsID = new ArrayList<>();      //群聊时保存所有群聊同学的ID
    private ArrayList<String> studentsName = new ArrayList<>();    //群聊时保存所有群聊同学的姓名
    private int numberOfGroup = 50;                     //群聊好友的总人数
    private Socket[] groupSocket = new Socket[numberOfGroup];
    private int[] actualGroupNum = new int[numberOfGroup];       //同意参与群聊的人数

    private JTextArea textArea1 = new JTextArea();                     //聊天区域
    private JTextArea textArea2 = new JTextArea();                     //打字区域
    private JScrollPane scrollPane1 = new JScrollPane(textArea1);     //滚动窗口
    private JScrollPane scrollPane2 = new JScrollPane(textArea2);

    private JButton messageSendButton = new JButton("发送消息");
    private JButton fileSendButton = new JButton("传送文件");
    private JButton closeButton = new JButton("关闭窗口");

    private String directory_now = System.getProperty("user.dir");
    private ImageIcon myIcon = new ImageIcon(directory_now + File.separator + "pictures" + File.separator + "ai.jpg");
    private JLabel myLabel = new JLabel(myIcon);

    //用于显示群聊时的所有好友，参考学长代码
    private DefaultListModel<String> defaultModel = new DefaultListModel<>();
    private JList<String> jList = new JList<>(defaultModel);
    private JScrollPane groupScrollPane = new JScrollPane(jList);

    //是否断开TCP连接的标志
    private int TCPmark = 0;

    //p2p输入输出流
    private OutputStream P2POutput;
    private PrintWriter P2PPrintWriter;

    private InputStream P2PIn;
    private BufferedReader P2PBuff;

    //group输入输出流
    private OutputStream[] groupOutput = new OutputStream[numberOfGroup];
    private PrintWriter[] groupPrintWriter = new PrintWriter[numberOfGroup];
    private InputStream[] groupIn = new InputStream[numberOfGroup];
    private BufferedReader[] groupBuff = new BufferedReader[numberOfGroup];

    /**
     * 客户端构造函数， p2p_chat
     * @param str
     * @param str1
     * @param name
     * @param str2
     * @param isp2p
     */
    MyClient(String str, String str1, String name, String str2, boolean isp2p){
        //单独聊天时，好友的IP地址及学号作为参数传入
        studentIP = str;                             //好友IP
        studentName = name;                          //好友名字
        userID = str2;                              //用户的学号

        IsP2P = isp2p;                              //p2p聊天

        //窗口监听器
        MWindowListenerP2P windowListener = new MWindowListenerP2P();
        addWindowListener(windowListener);
    }

    /**
     * 构造函数重载，群聊
     * @param fip
     * @param fID
     * @param fnm
     * @param id
     * @param isp2p
     */
    MyClient(ArrayList<String> fip, ArrayList<String> fID, ArrayList<String> fnm, String id, boolean isp2p){
        //群聊时，好友列表的IP地址及学号作为参数传入
        IsP2P = isp2p;                                          //是群聊

        numberOfGroup = fip.size();                             //群聊人数
        System.out.println("numberOfGroup" + numberOfGroup);
        userID = id;                                            //用户学号
        String IP;
        String ID;
        String name;
        for(int i = 0; i < numberOfGroup; ++ i){
            IP = fip.get(i);
            ID = fID.get(i);
            name = fnm.get(i);
            studentsIP.add(IP);                                  //好友IP
            studentsID.add(ID);                                  //好友学号
            studentsName.add(name);
            defaultModel.addElement(ID);
        }
        MWindowListenerGroup windowListener = new MWindowListenerGroup();
        addWindowListener(windowListener);
    }


    /**
     * 作为一个Runnable的类，作为线程可以运行
     */
    public void run() {
        //创建用户界面
        setLayout(null);

        /**
         * p2p聊天
         */
        if(IsP2P){                     //单独聊天
            myLabel.setBounds(10,10,100,100);
            scrollPane1.setBounds(10,120,680,200);
            scrollPane2.setBounds(10,350,420,120);
            textArea2.setFont(new Font("楷体", Font.ITALIC, 30));
            textArea1.setFont(new Font("楷体", Font.ITALIC, 30));
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
            setBackground(Color.BLUE);
            setSize(720,600);
            setTitle("P2P Chat");
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setVisible(true);

            try{
                //建立TCP连接
                socket = new Socket(studentIP, 49876);     //Socket内容是好友IP地址 + 端口号49876

                //作为客户端首先建立输出流
                P2POutput = socket.getOutputStream();
                P2PPrintWriter = new PrintWriter(P2POutput);

                //获取输入流
                P2PIn = socket.getInputStream();
                P2PBuff = new BufferedReader(new InputStreamReader(P2PIn));

                //发送通话请求
                P2PPrintWriter.write(userID + "single_p2p" + '\n');             //single_p2p是建立单独聊天请求的标志
                P2PPrintWriter.flush();

                //接收对方的许可
                String confirm = P2PBuff.readLine();
                System.out.println("confirm" + confirm);
                if(confirm.equals("p2p_cancel")){                               //如果对方拒绝此次通话
                    JOptionPane.showMessageDialog(null, "对方拒绝此次会话", "SORRY", JOptionPane.INFORMATION_MESSAGE);
                    try{
                        //关闭输入输出流，断开TCP连接
                        socket.close();
                        P2PBuff.close();
                        P2PIn.close();
                        P2PPrintWriter.close();
                        P2POutput.close();
                        dispose();
                    } catch(IOException ignored){
                    }
                }
                else{                                                           //如果对方允许此次通话
                    //开始一个新的线程始终监听输入流
                    Receive receive = new Receive();
                    new Thread(receive).start();

                    //发送消息按键添加监听器
                    messageSendButtonListener sendListener = new messageSendButtonListener();
                    messageSendButton.addActionListener(sendListener);

                    //结束通话按钮添加监听器
                    closeButtonListener closeListener = new closeButtonListener();
                    closeButton.addActionListener(closeListener);

                    //为“发送文件”按钮添加监听器
                    fileSendButtonListener fileListener = new fileSendButtonListener();
                    fileSendButton.addActionListener(fileListener);
                }
            }catch(IOException ex){
                System.err.println(ex);
            }
        }

        /**
         * 群聊
         */
        else{                                   //不是p2p,就是群聊
            //创建用户界面
            myLabel.setBounds(10,10,100,100);
            scrollPane1.setBounds(10,120,480,200);
            scrollPane2.setBounds(10,350,420,120);

            textArea2.setFont(new Font("楷体", Font.ITALIC, 40));
            textArea1.setFont(new Font("楷体", Font.ITALIC, 40));

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
            try{
                //建立TCP连接并创建输入输出流
                for(int i = 0; i < numberOfGroup; ++ i){
                    groupSocket[i] = new Socket(studentsIP.get(i),49876);

                    //outputStream
                    groupOutput[i] = groupSocket[i].getOutputStream();
                    groupPrintWriter[i] = new PrintWriter(groupOutput[i]);

                    //inputStream
                    groupIn[i] = groupSocket[i].getInputStream();
                    groupBuff[i] = new BufferedReader(new InputStreamReader(groupIn[i]));
                }

                int n = numberOfGroup;
                int j = 0;
                int[] refuse = new int[n];

                //向好友发送群聊请求
                for(int i = 0; i < n; ++ i){
                    groupPrintWriter[i].write("group_communicate" + '\n');
                    groupPrintWriter[i].flush();
                    String confirm = groupBuff[i].readLine();
                    if(confirm.equals("p2p_cancel")){  //如果对方拒绝此次通话
                        JOptionPane.showMessageDialog(null, "对方拒绝此次群聊", "SORRY", JOptionPane.INFORMATION_MESSAGE);
                        -- numberOfGroup;                //更新参与群聊的人数,其开始为n
                        defaultModel.removeElement(studentsID.get(i));
                        refuse[j] = i;
                        ++ j;
                        groupBuff[i].close();
                        groupIn[i].close();
                        groupPrintWriter[i].close();
                        groupOutput[i].close();
                        groupSocket[i].close();
                    }
                }

                //将允许本次群聊的好友编号保存在数组actualGroupNum[]中
                //此时j是拒绝的人数
                for(int i = 0, m = 0; i < n; ++ i){
                    int flag = 1;
                    for(int k = 0; k < j; ++k){
                        if(i == refuse[k]){
                            flag = 0;
                            break;
                        }
                    }
                    if(flag == 1){                  //flag==1表示第i个好友接受了群聊
                        actualGroupNum[m] = i;      //第m个同意群聊的人
                        ++ m;
                    }
                }

                //在上面的请求群聊中，如果有一个或一个以上的好友同意群聊，则开始群聊
                if(numberOfGroup >= 1){
                    informWhole();       //初始化，将每一个群聊的好友的学号发送给所有参加群聊的好友
                    GroupReceive[] groupRcv = new GroupReceive[numberOfGroup];
                    for(int i = 0; i < numberOfGroup; ++ i) {
                        groupRcv[i] = new GroupReceive(i);
                        new Thread(groupRcv[i]).start();
                    }
                }

                //若所有好友都拒绝群聊，则群聊结束，对话框回收
                else dispose();


                //发送键监听器
                JBGroupSendListener sendListener = new JBGroupSendListener();
                messageSendButton.addActionListener(sendListener);

                //关闭键监听器
                JBGroupCloseListener closeListener = new JBGroupCloseListener();
                closeButton.addActionListener(closeListener);

            }catch(IOException e){
                System.err.println(e);
            }
        }
    }

    /**
     * 单独聊天窗口监听器
     */
    private class MWindowListenerP2P implements WindowListener{
        public void windowClosed(WindowEvent event){}
        public void windowClosing(WindowEvent event){
            try{
                P2PPrintWriter.write("socket_close"+'\n');
                P2PPrintWriter.flush();
                socket.close();
                P2PBuff.close();
                P2PIn.close();
                P2PPrintWriter.close();
                P2POutput.close();
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
     * 群聊窗口监听器
     */
    private class MWindowListenerGroup implements WindowListener{
        public void windowClosed(WindowEvent event){}

        public void windowClosing(WindowEvent event){
            try{
                BoardCast(-1, userID + "socket_close" + '\n');
                for(int i = 0; i < numberOfGroup; ++ i){
                    groupSocket[i].close();
                    groupBuff[i].close();
                    groupIn[i].close();
                    groupPrintWriter[i].close();
                    groupOutput[i].close();
                }
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
     * 单独聊天发送键监听器
     */
    private class messageSendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            //获取需要发送的文本，并且在末尾加上换行符
            String send = textArea2.getText()+'\n';
            //写到输出流中
            P2PPrintWriter.write(send);
            P2PPrintWriter.flush();
            textArea1.append("Me: " + send);
            textArea2.setText("");
            textArea1.setFont(new Font("隶书", Font.ITALIC, 30));
            textArea2.setFont(new Font("隶书", Font.ITALIC, 30));
        }
    }


    /**
     *群聊时发送键监听器
     */
    private class JBGroupSendListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            //获取需要发送的文本，并且在末尾加上换行符
            String send = " (" + userID + "):" + textArea2.getText() + '\n';
            //写到输出流中
            BoardCast(-1, send);   //广播出去
            textArea1.append("Me:" + textArea2.getText() + '\n');
            textArea2.setText("");
            textArea1.setFont(new Font("隶书", Font.ITALIC, 30));
            textArea2.setFont(new Font("隶书", Font.ITALIC, 30));
        }
    }

    /**
     * 广播函数，广播除编号为noboard的其他所有参加群聊的好友
     */
    private void BoardCast(int noboard, String str){
        for(int i = 0; i < numberOfGroup; ++ i){
            if(actualGroupNum[i] == noboard) {}
            else{
                groupPrintWriter[actualGroupNum[i]].write(str);
                groupPrintWriter[actualGroupNum[i]].flush();
            }
        }
    }

    /**
     * p2p聊天结束通话监听器
     */
    private class closeButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                TCPmark = 1;                                    //mark = 1表示断开TCP连接
                P2PPrintWriter.write("socket_close" + '\n');
                P2PPrintWriter.flush();

                System.out.println(TCPmark);

                socket.close();
                P2PBuff.close();
                P2PIn.close();
                P2PPrintWriter.close();
                P2POutput.close();
                dispose();
            }catch(IOException ignored){}
        }
    }

    /**
     * 群聊关闭监听器
     */
    private class JBGroupCloseListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                TCPmark = 1;                                    //mark = 1表示断开TCP连接
                                //向群聊的每个输入输出流都关闭，socket也关闭
                BoardCast(-1, userID + "socket_close" + '\n');
                for(int i = 0; i < numberOfGroup; ++ i){
                    groupSocket[i].close();
                    groupBuff[i].close();
                    groupIn[i].close();
                    groupPrintWriter[i].close();
                    groupOutput[i].close();
                }
                dispose();
            }catch(IOException ignored){}
        }
    }

    
    /**
     * p2p聊天发送文件监听器
     */
    private class fileSendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            //首先向对方提出发送文件的请求
            P2PPrintWriter.write("I_want_send_file"+'\n');
            P2PPrintWriter.flush();
            JOptionPane.showMessageDialog(null, "正在等待对方确认，单击OK继续等待", "WAIT", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    
    

    /*************************************************************************
     * 
     * 下面是处理接收信息的各个内部类
     * 
     *************************************************************************/


    /**
     * 接收p2p输入流信息的内部类
     */
    private class Receive implements Runnable{
        //默认的构造函数
        Receive(){}
        public void run(){
            try{
                while(true){
                    String receive;
                    if(!((receive = P2PBuff.readLine()) == null)){              //读进了东西

                        //首先判断是否是发送文件的请求，如果不是，则视为聊天内容显示在文本域中
                        if(receive.equals("I_want_send_file")){                 //作为文件的接收方
                            int confirm_file = JOptionPane.showConfirmDialog(null, "好友请求发送文件，是否接受？", "文件发送", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if(confirm_file == JOptionPane.YES_OPTION){    //允许好友发送文件
                                P2PPrintWriter.write("I_agree"+'\n');
                                P2PPrintWriter.flush();
                            }
                            else{   //拒绝好友发送文件
                                P2PPrintWriter.write("let_down"+'\n');
                                P2PPrintWriter.flush();
                            }
                        }
                        else if(receive.equals("I_agree")) {                //作为文件的发送方,对方允许发送文件

                            //好友已经允许用户发送文件的情况下，用户是否继续发送文件
                            int confirm = JOptionPane.showConfirmDialog(null, "好友已接收您的请求，是否继续？", "文件发送", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if(confirm == JOptionPane.YES_OPTION) {
                                //若用户继续发送文件,开始一个新的线程发送文件(File_send线程)
                                FileSend fileSend = new FileSend();
                                new Thread(fileSend).start();                   //把filesend转化为线程,启动
                            }
                        }
                        else if(receive.equals("let_down"))  {
                            //作为文件的发送方,对方拒绝文件
                            JOptionPane.showMessageDialog(null, "好友拒绝了您的发送请求", "文件发送", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else if(receive.contains("socket_close"))   {
                            JOptionPane.showMessageDialog(null, "好友关闭TCP连接", "连接提醒", JOptionPane.INFORMATION_MESSAGE);
                            try{
                                socket.close();
                                P2PBuff.close();
                                P2PIn.close();
                                P2PPrintWriter.close();
                                P2POutput.close();
                                dispose();
                            }catch(IOException ignored){}
                        }
                        //若不是文件发送的相关信息，则作为聊天内容显示在文本域中
                        else {
                            textArea1.append(studentName + ":" + receive + '\n');
                            textArea1.setFont(new Font("隶书", Font.ITALIC, 30));
                        }

                    }
                }
            } catch(IOException e)  {
                System.err.println(e);
            }
        }
    }

    /**
     * 接收群聊输入流信息的内部类,群聊无文件传输
     */
    private class GroupReceive implements Runnable{
        private int num;
        GroupReceive(int i){num = i;}
        public void run(){
            try{
                while(true){
                    String gComment;
                    if(!((gComment = groupBuff[num].readLine()) == null)){
                        //若群聊中有人退出
                        if(gComment.contains("socket_close")){
                            System.out.println(gComment);
                            JOptionPane.showMessageDialog(null, studentsID.get(num) + "好友关闭TCP连接", "连接提醒", JOptionPane.INFORMATION_MESSAGE);
                            BoardCast(num, studentsID.get(num) + "socket_close" + '\n');
                            defaultModel.removeElement(studentsID.get(num));

                            if(defaultModel.isEmpty()){
                                JOptionPane.showMessageDialog(null, "所有好友已退出群聊", "窗口将自动关闭", JOptionPane.INFORMATION_MESSAGE);
                                dispose();
                            }
                            groupBuff[num].close();
                            groupIn[num].close();
                            groupPrintWriter[num].close();
                            groupOutput[num].close();
                            groupSocket[num].close();
                        }
                        else{
                            // 作为聊天内容显示在文本域中
                            textArea1.append(studentsName.get(num) + ":" + gComment+'\n');
                            String str = "(" + studentsID.get(num) + "):" + gComment;
                            System.out.println(str);
                            BoardCast(num, str + '\n');
                            textArea1.setFont(new Font("隶书", Font.ITALIC, 30));
                        }
                    }
                }
            }catch(IOException e){
                System.err.println(e);
            }
        }
    }

    
    /**
     * 文件传输类，与上面一样，实行Runnable方法，可以作为线程使用
     */
    private class FileSend implements Runnable  {
        private JFileChooser chooser;
        private File file;
        private BufferedInputStream fileIn;             //文件输入流
        private Socket fileSocket;                      //建立发送文件的TCP连接
        OutputStream fileOut;      //文件输出流
        BufferedOutputStream fileBuff;
        FileSend(){}

        void openFile(){
            //打开文件，将文件输入流关联到文件上
            chooser = new JFileChooser(new File("."));                      //用JFileChooser创建打开文件对话框
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                file = chooser.getSelectedFile();    //选中要打开的文件
                try{                            //建立文件的输入流
                    fileIn = new BufferedInputStream(new FileInputStream(file));
                }catch(IOException ex){
                    System.out.println("Error opening" + file.getName());
                }
            }
        }

        public void run(){
            try{
                fileSocket = new Socket(studentIP,32524);        //建立文件发送TCP连接
                fileOut = fileSocket.getOutputStream();         //建立输出流
                fileBuff = new BufferedOutputStream(fileOut);
                openFile();                                     //打开文件
                int r;
                while((r = fileIn.read()) != -1)  {             //以字节为单位发送文件
                    fileBuff.write((byte)r);                    //写入文件
                }

                //文件发送完毕后关闭输入输出流，并断开TCP连接
                fileBuff.close();
                fileOut.close();
                fileIn.close();
                fileSocket.close();
            }catch(IOException e){
                System.err.println(e);
            }
            //文件发送结束后，再追加一个结束信号
            P2PPrintWriter.write("file_been_sent"+'\n');
            P2PPrintWriter.flush();
            JOptionPane.showMessageDialog(null, "文件成功发送", "文件发送", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 将群聊好友的学号发送给每一个参与群聊的好友
     */
    private void informWhole(){
        for(int i = 0; i < numberOfGroup; ++ i){     //广播好友ID
            String info = studentsID.get(i) + '\n';
            BoardCast(i,info);
        }
        BoardCast(-1, userID + '\n');           //广播用户自己的ID
        BoardCast(-1, "info_over" + '\n');      //广播结束信息
    }
}