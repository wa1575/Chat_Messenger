package messenger;

import java.io.*;
import java.net.*;
import java.util.*;


class ClientData{
	public String nickname = new String(); //�г���(Ŭ���̾�Ʈ���� �Է��ϰ� �����ϴ� �г���)
	public String id = new String(); //�������� �����ϴ� id - �ε��� �뵵
	public PrintWriter writer; //PrintWriter�� Ŭ���̾�Ʈ���� �޼����� ���� 
	
	ClientData(String _id, String nick, PrintWriter w){
		nickname = nick;
		id = _id;
		writer = w;
	}
}

//ä�� ���� Ŭ���� 
public class server {
	private int ClientsIndex = 0; // ������ Ŭ���̾�Ʈ�� ���� id�� �ֱ� ���� �ε���
	private ArrayList clients; // Ŭ���̾�Ʈ ����Ʈ

	// Ŭ���̾�Ʈ �ڵ鷯 Ŭ����
	// - Ŭ���̾�Ʈ���� �޽��� ���� ������ ����
	// - �̶� XML������ �����͸� �Ľ��Ͽ� �ʿ�� �޽��� ���� �� ����
	public class ClientHandler implements Runnable
	{
		private BufferedReader reader; // Ŭ���̾�Ʈ���� �޽����� �޴� Reader
		private Socket sock; // Ŭ���̾�Ʈ�� Socket
		private String clientID; // Ŭ���̾�Ʈ�� id

		// �⺻�ڵ鷯���� ���ϰ� id�� ����
		public ClientHandler(Socket clientSocket, String _id)
		{
			clientID = _id;
			try
			{
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		// Thread�� �����ҽ� ������ �κ�
		public void run()
		{
			String message;
			try
			{
				// Ŭ���̾�Ʈ�� �޽����� �����Ҷ����� ������¿� �ִٰ�
				// Ŭ���̾�Ʈ�� �޽����� �����ϸ� ����
				while ( (message = reader.readLine()) != null )
				{
					// Ŭ���̾�Ʈ���� �����ؿ� �޽��� ���
					// - ���� �ֿܼ��� Ȯ���ϱ� ����
					System.out.println("read " + message);

					// �����ؿ� Ŭ���̾�Ʈ�� �г����� �Ľ�
					int index1 = message.indexOf("<NICKNAME>");
					int index2 = message.indexOf("</NICKNAME>");
					String curNick = message.substring(index1+10, index2);
					
					// �г��� ������ �����͸� �Ľ�
					String back_message = message.substring(index2+11, message.length());
				
					String ConnectMessage = new String();
					// <NEW_LOGIN>�̶�� ���ڿ��� �����Ѵٸ� ���ο� ����
					if( (back_message.indexOf("<NEW_LOGIN>")) != -1 )
					{
						// ���ο� ����
						ConnectMessage = "<NICKNAME>" + curNick + "</NICKNAME><CONNECT_LIST>" + curNick;

						// ���� Ŭ���̾�Ʈ����Ʈ�� �ִ� ��� �г����� Ŭ���̾�Ʈ�鿡�� �˷���
						// - Ŭ���̾�Ʈ�� ������ ����Ʈ ������ ������
						Iterator it = clients.iterator();
						while( it.hasNext() )
						{
							ClientData client = (ClientData) it.next();
							ConnectMessage = ConnectMessage + "|" + client.nickname;
						}
						ConnectMessage = ConnectMessage + "</CONNECT_LIST>";
						// ������ �޽��� ����
						tellEveryone(ConnectMessage);
					}
					// <MYID>��� ���ڿ��� �����Ѵٸ�
					// - ���������� �˷��� ID�� ���� �г����� �ǹ���
					else if( (back_message.indexOf("<MYID>")) != -1 )
					{
						// ���̵� ����
						index1 = back_message.indexOf("<MYID>");
						index2 = back_message.indexOf("</MYID>");
						if( index1 == -1 || index2 == -2 )
						{
							continue;
						}
						String id = back_message.substring(index1+6,index2);

						// Ŭ���̾�Ʈ ����Ʈ���� id�� �ش��ϴ� Ŭ���̾�Ʈ�� ã��
						// ���� �޽����� �г����� Ŭ���̾�Ʈ�� �г������� ����
						Iterator it = clients.iterator();
						while( it.hasNext() )
						{
							ClientData client = (ClientData) it.next();
							if( client.id.compareTo(id) != 0 )
							{
								continue;
							}
							client.nickname = curNick;
							break;
						}
						System.out.println("id = " + id + " / Nick = " + curNick);

						continue;
					}
					// �޽��� ����
					tellEveryone(message);
				}
			}
			// SocketException�� Ŭ���̾�Ʈ�� �������� ������ �������� �߻�
			// - ��������, - ��������, - ���ͳ� �������� ��
			catch(SocketException e)
			{
				// �ش� Ŭ���̾�Ʈ ����
				// - Ŭ���̾�Ʈ ID�� �̿��Ͽ� �ش� Ŭ���̾�Ʈ�� �г��� ����
				// - �׸��� �ش� Ŭ���̾�Ʈ�� ����Ʈ���� ����
				String exitClientNick = new String();
				for(int i=0; i<clients.size(); ++i)
				{
					ClientData client = (ClientData) clients.get(i);
					if( client.id.compareTo(clientID) != 0 )
					{
						continue;
					}
					exitClientNick = client.nickname;
					clients.remove(i);
					break;
				}

				// Ŭ���̾�Ʈ�鿡�� �α׾ƿ��� Ŭ���̾�Ʈ�� ������ ������ Ŭ���̾�Ʈ�� �г��� ����Ʈ�� ����
				String ConnectMessage = new String();
				ConnectMessage = "<NICKNAME>SERVER</NICKNAME><CONNECT_LIST>";

				Iterator it = clients.iterator();

				if( it.hasNext() )
				{
					ClientData client = (ClientData) it.next();
					ConnectMessage = ConnectMessage + client.nickname;
				}
				while( it.hasNext() )
				{
					ClientData client = (ClientData) it.next();
					ConnectMessage = ConnectMessage + "|" + client.nickname;
				}
				ConnectMessage = ConnectMessage + "</CONNECT_LIST>";
				tellEveryone(ConnectMessage);

				// �׸��� logout�� Ŭ���̾�Ʈ�� �г����� �˷���
				message = "<NICKNAME>SERVER</NICKNAME><LOGOUT>"+ exitClientNick +"</LOGOUT>";
				tellEveryone(message);
			}
			// �� �� ���� �߻��� ���� ���� ���
			catch( Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String Args[])
	{
		new server().excuteServer();
	}

	public void excuteServer()
	{
		// Ŭ���̾�Ʈ ����Ʈ�� �ʱ�ȭ
		clients = new ArrayList();
		try
		{
			// ���� ���� ( ��Ʈ��ȣ 5000 )
			ServerSocket serverSock = new ServerSocket(5000);
			while( true )
			{
				// ���ο� Ŭ���̾�Ʈ�� �����ϸ� clinetSocket�� ������ Ŭ���̾�Ʈ�� ������ ����
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream()); // PrintWriter����
				clients.add(new ClientData(new String(""+ClientsIndex), "", writer) ); // Ŭ���̾�Ʈ����Ʈ �߰�
				// Ŭ���̾�Ʈ���� ID�� �˷���
				// - �̴� Ŭ���̾�Ʈ���� �ڽ��� ���̵� �˷��ְ�
				// - Ŭ���̾�Ʈ�� �г����� �˱�������
				writer.println("<YOURID>"+ClientsIndex+"</YOURID>");
				writer.flush();

				// �ش� Ŭ���̾�Ʈ�� ������ �̿��Ͽ� Ŭ���̾�Ʈ �ڵ鷯 ������ ����
				Thread thread = new Thread(new ClientHandler(clientSocket, new String(""+ClientsIndex)));
				thread.start();

				System.out.println("got a connection");

				// Ŭ���̾�Ʈ �ε��� ����
				ClientsIndex++;
			}
		}
		// ���� �߻��� ���ܻ��� ���
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// Ŭ���̾�Ʈ�鿡�� �޽����� �����ϴ� �޼ҵ�
	public void tellEveryone( String message )
	{
		// ��� Ŭ���̾�Ʈ�鿡�� �ݺ�
		Iterator it = clients.iterator();
		while( it.hasNext() )
		{
			try
			{
				// Ŭ���̾�Ʈ����Ʈ���� �ϳ��� Ŭ���̾�Ʈ�� ������
				ClientData client = (ClientData) it.next();
				client.writer.println(message); // �ش� Ŭ���̾�Ʈ�� PrintWriter�� �ش� �޽����� ����
				client.writer.flush(); // PrintWriter�� Flush��. ����ִ� ��
			}
			// ���� �߻��� ���ܻ��� ���
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
}
